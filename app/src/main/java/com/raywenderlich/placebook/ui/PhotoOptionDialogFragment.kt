package com.raywenderlich.placebook.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.DialogFragment

const val TAG = "controlling"

class PhotoOptionDialogFragment:DialogFragment() {

    interface PhotoOptionDialogListener{
        fun onCaptureClick()
        fun onPickClick()

    }

    private lateinit var photoOptionDialogListener: PhotoOptionDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        photoOptionDialogListener = activity as PhotoOptionDialogListener

        var captureSelectedIdx = -1

        var pickSelectedIdx = -1

        val options  = ArrayList<String>()

        val context = activity as Context
        Log.d(TAG, "8")

        if (canCapture(context)){
            Log.d(TAG, "1")
            options.add("Camera")
            captureSelectedIdx = 0
        }

        if(canPick(context)){
            Log.d(TAG, "2")
            options.add("Gallery")
            pickSelectedIdx = if(captureSelectedIdx == 0) 1 else 0
        }

        return AlertDialog.Builder(context)
            .setTitle("photo options")
            .setItems(options.toTypedArray<CharSequence>()){_,which ->
                if (which == captureSelectedIdx) {
                    Log.d(TAG, "3")
                    photoOptionDialogListener.onCaptureClick()
                } else if (which == pickSelectedIdx) {
                    Log.d(TAG, "4")
                    photoOptionDialogListener.onPickClick()
                }


            }.setNegativeButton("Cancel",null)
            .create()


    }

    companion object{
        fun canCapture(context: Context): Boolean {
            Log.d(TAG, "6")
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            return(captureIntent.resolveActivity(context.packageManager) != null)
        }

        fun canPick(context: Context): Boolean {
            Log.d(TAG, "5")
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            return (pickIntent.resolveActivity(context.packageManager ) != null)
        }



        fun newInstance(context: Context) = if(canCapture(context) || canPick(context)){
            Log.d(TAG, "7")
            PhotoOptionDialogFragment()
        }else{
            null
        }
    }

}