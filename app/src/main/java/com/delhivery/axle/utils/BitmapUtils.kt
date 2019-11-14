package com.delhivery.axle.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * Created by saurabhdhillon
 * for Delhivery Private Limited
 **
 *
 * Perform bitmap functions on image
 *
 **
 */
class BitmapUtils {

  fun loadBitmap(path: String?): Bitmap? {
    if (path.isNullOrEmpty()) {
      return null
    }

    val imgFile = File(path)
    if (imgFile.exists()) {
      return BitmapFactory.decodeFile(imgFile.absolutePath)
    }
    return null
  }
}