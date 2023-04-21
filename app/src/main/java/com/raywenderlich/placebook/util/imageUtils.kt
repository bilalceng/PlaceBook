package com.raywenderlich.placebook.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.Date

object ImageUtils {

    fun saveBitmapToFile(context:Context, bitmap:Bitmap, fileName: String){

        val stream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream)

        val bytes  = stream.toByteArray()

        saveBytesToFile(context, bytes, fileName)

    }

    private fun saveBytesToFile(context: Context, byte: ByteArray, fileName: String){

        var outputStream: FileOutputStream

        try {
            outputStream = context.openFileOutput(fileName,Context.MODE_PRIVATE)
            outputStream.write(byte)
            outputStream.close()

        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    fun loadBitmapFromFile(context: Context, fileName: String): Bitmap?{
        val filePath = File(context.filesDir,fileName).absolutePath
        return BitmapFactory.decodeFile(filePath)
    }


    @Throws(IOException::class)
    fun createImageUniqueFile(context: Context): File{
        val timeStamp = SimpleDateFormat("yyyyMMDDHHmmss".format(Date()))
        val fileName = "PlaceBook_" + timeStamp + "_"
        val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val file = File.createTempFile(fileName,".jpg",fileDir)
        Log.d("bilbil", "$file")
        return file
    }

    private fun calculateInSampleSize(
        width:Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int{
        Log.d("porn", "$width $height $reqHeight $reqWidth")
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth){
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }


        }
        return inSampleSize
    }

    fun decodeFileToSize(
        filePath: String,
        width: Int,
        height: Int
    ): Bitmap{

        Log.d("porn", "3")

        val options = BitmapFactory.Options()

        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath,options)
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight,width,height)
        Log.d("porn", "${options.inSampleSize}")
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(filePath,options)
    }

    private fun rotateImage(img:Bitmap, degree: Float): Bitmap?{
        Log.d("porn", "6")
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImage = Bitmap.createBitmap(img,0,0,img.width,img.height,matrix,true)
        img.recycle()
        return rotatedImage
    }

    @Throws(IOException::class)
    fun rotateImageIfRequired(context: Context, img: Bitmap,
                              selectedImage: Uri
    ): Bitmap {

        Log.d("porn", "5")
        val input: InputStream? =
            context.contentResolver.openInputStream(selectedImage)
        val path = selectedImage.path
        val ei: ExifInterface = when {
            Build.VERSION.SDK_INT > 23 && input != null ->
                ExifInterface(input)
            path != null -> ExifInterface(path)
            else -> null
        } ?: return img
        return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img,
                90.0f) ?: img
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img,
                180.0f) ?: img
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img,
                270.0f) ?: img
            else -> img
        }
    }

    fun decodeUriStreamToSize(
        uri: Uri,
        width: Int,
        height: Int,
        context: Context
    ): Bitmap?{

        var inputStream: InputStream? = null

        try {
            val option: BitmapFactory.Options

            inputStream = context.contentResolver.openInputStream(uri)
            Log.d("porn", "$uri")
            if(inputStream != null){
                option = BitmapFactory.Options()
                option.inJustDecodeBounds = false
                BitmapFactory.decodeStream(inputStream,null,option)
                inputStream.close()

                inputStream = context.contentResolver.openInputStream(uri)

                if(inputStream != null){

                    option.inSampleSize = calculateInSampleSize(option.outWidth,option.outHeight,
                    width, height)

                    option.inJustDecodeBounds = false

                    val bitmap = BitmapFactory.decodeStream(inputStream,null,option)

                    inputStream.close()
                    return bitmap
                    println()
                }
            }
            return null

        }catch (e: Exception){
            return null
        }
        finally {
            inputStream?.close()
        }
    }
}