package com.dfd.delfin.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.dfd.delfin.injection.scope.ActivityScope
import dagger.android.support.DaggerAppCompatActivity
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

  /**
   * @return decoded sampled bitmap
   */
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

}