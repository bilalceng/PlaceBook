package com.raywenderlich.placebook.util

import android.content.Context
import java.io.File

object FileUtils {

  fun deleteFile(context: Context, fileName: String){
     val filDir = context.filesDir
     val file = File(filDir,fileName)
     file.delete()
  }
}