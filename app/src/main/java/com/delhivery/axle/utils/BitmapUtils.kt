package com.delhivery.axle.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.delhivery.axle.injection.scope.ActivityScope
import dagger.android.support.DaggerAppCompatActivity
import java.io.File
import javax.inject.Inject

/**
 * Created by saurabhdhillon
 * for Delhivery Private Limited
 **
 *
 * Perform bitmap functions on image
 *
 **
 */
@ActivityScope
class BitmapUtils @Inject constructor(
  private val activity: DaggerAppCompatActivity
) {

  fun loadBitmap(
    path: String?
  ): Bitmap? {
    if (path.isNullOrEmpty()) {
      return null
    }

    val imgFile = File(path)
    if (imgFile.exists()) {
      return getResizedBitmap(BitmapFactory.decodeFile(imgFile.absolutePath))
    }
    return null
  }

  private fun getResizedBitmap(
    bitmap: Bitmap
  ): Bitmap {
    return Bitmap.createScaledBitmap(bitmap, bitmap.width / 5, bitmap.height / 5, true)
  }

//  //I added this to have a good approximation of the screen size:
//  fun decodeSampledBitmap(pathName: String): Bitmap {
//    val display = activity.windowManager.defaultDisplay
//    val size = Point()
//    display.getSize(size)
//    val width = size.x
//    val height = size.y
//    return decodeSampledBitmap(pathName, width, height)
//  }

  private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
  ): Int {
    // Raw height and width of image
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

      val halfHeight = height / 2
      val halfWidth = width / 2

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
        inSampleSize *= 2
      }
    }

    return inSampleSize
  }

  fun decodeSampledBitmap(
    pathName: String,
    reqWidth: Int,
    reqHeight: Int
  ): Bitmap? {

    // First decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(pathName, options)

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeFile(pathName, options)
  }

}