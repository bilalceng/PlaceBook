package com.raywenderlich.placebook.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.databinding.ActivityDetailBinding
import com.raywenderlich.placebook.util.ImageUtils
import com.raywenderlich.placebook.viewmodel.DetailViewModel
import java.io.File


class DetailActivity : AppCompatActivity() , PhotoOptionDialogFragment.PhotoOptionDialogListener{
    private var photoFile: File?  = null
    private val detailViewModel by  viewModels<DetailViewModel>()
    private var detailView: DetailViewModel.DetailView? = null
    private lateinit var dataBinding: ActivityDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.activity_detail)
       setupToolbar()

        getIntentData()
    }

    private fun setupToolbar(){
        setSupportActionBar(dataBinding.toolbar)
    }

    private fun populateImageView(){
        detailView?.let {
            val placeImage = it.getImage(this)
            placeImage.let {
                dataBinding.imageViewPlace.setImageBitmap(placeImage)
            }

        }

        dataBinding.imageViewPlace.let {
            it.setOnClickListener {
                replaceImage()

            }
        }
    }


    private fun getIntentData(){
        val bookmarkId = intent.getLongExtra(MapsActivity.EXTRA_BOOKMARK_ID,0)

        detailViewModel.getDetailView(bookmarkId)?.observe(this){
            it?.let {
                detailView = it
                dataBinding.detailView = it
                populateImageView()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
      menuInflater.inflate(R.menu.deatail_menu,menu)
        return true
    }
private fun saveChanges(){
    val name = dataBinding.editTextName.text.toString()
    if(name.isEmpty()){
        return
    }
    detailView?.let {
        it.name = dataBinding.editTextName.text.toString()
        it.notes = dataBinding.editTextNotes.text.toString()
        it.address = dataBinding.editTextAddress.text.toString()
        it.phone = dataBinding.editTextPhone.text.toString()
        detailViewModel.updateBookmark(it)
    }
    finish()
}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.save){
            saveChanges()
            Toast.makeText(this, "changes recorded successfully.", Toast.LENGTH_SHORT).show()
            true
        }
            return super.onOptionsItemSelected(item)
    }

    override fun onCaptureClick() {
        Log.d("porn", "1")
        Toast.makeText(this, "Camera Capture",
            Toast.LENGTH_SHORT).show()

        photoFile = null

        try {
            photoFile = ImageUtils.createImageUniqueFile(this)

        }catch (ex: java.io.IOException){
            return
        }

        photoFile?.let {

            val photoUri = FileProvider.getUriForFile(this,
                "com.raywenderlich.placeBook.fileprovider", it)

            val captureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)

            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,photoUri)

            val intentActivities = packageManager.queryIntentActivities(
                captureIntent,PackageManager.MATCH_DEFAULT_ONLY)

            intentActivities.map {resolve ->
                resolve.activityInfo.packageName
            }.forEach {
                grantUriPermission(it,photoUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)

        }


    }

    override fun onPickClick() {
        Toast.makeText(this, "Gallery Pick",
            Toast.LENGTH_SHORT).show()

        val pickIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
    }


    private fun replaceImage(){
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager,"PhotoOptionDialog")
    }

    private fun updateImage(image: Bitmap) {
        detailView?.let {
            Log.d("porn", "7")
            dataBinding.imageViewPlace.setImageBitmap(image)
            it.setImage(this, image)
        }
    }

    private fun getImageWithPath(filePath: String) =
        ImageUtils.decodeFileToSize(
            filePath,
            resources.getDimensionPixelSize(R.dimen.default_image_width),
            resources.getDimensionPixelSize(R.dimen.default_image_height)
        )

    private fun getImageWithAuthority(uri: Uri) =
        ImageUtils.decodeUriStreamToSize(
            uri,
            resources.getDimensionPixelSize(R.dimen.default_image_width),
            resources.getDimensionPixelSize(R.dimen.default_image_height),
            this
        )

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        Log.d("porn", "2")

        if (resultCode == android.app.Activity.RESULT_OK) {
            when (requestCode) {

                REQUEST_CAPTURE_IMAGE -> {

                    val photoFile = photoFile ?: return

                    val uri = FileProvider.getUriForFile(
                        this,
                        "com.raywenderlich.placeBook.fileprovider",
                        photoFile
                    )
                    revokeUriPermission(
                        uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )

                    val image = getImageWithPath(photoFile.absolutePath)

                    val bitmap = ImageUtils.rotateImageIfRequired(
                        this,
                        image, uri
                    )
                    updateImage(bitmap)
                }
                REQUEST_GALLERY_IMAGE -> if (data != null && data.data != null)
                {
                    val imageUri = data.data as Uri

                    val image = getImageWithAuthority(imageUri)
                    image?.let {
                        val bitmap = ImageUtils.rotateImageIfRequired(this, it,
                            imageUri)
                        updateImage(bitmap)
                    }

                }
            }
        }
    }


    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 1
        private const val REQUEST_GALLERY_IMAGE = 2
    }

}