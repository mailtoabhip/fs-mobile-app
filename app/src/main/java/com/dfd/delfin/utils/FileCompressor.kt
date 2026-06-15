package com.dfd.delfin.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import dagger.android.support.DaggerAppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

/**
 * Created by saurabhdhillon
 * for Delhivery Private Limited
 **
 * Perform compress options to image
 **
 */

class FileCompressor @Inject constructor(activity: DaggerAppCompatActivity) {
  //max width and height values of the compressed image is taken as 612x816
  private var maxWidth = 612
  private var maxHeight = 816
  private var compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
  private var quality = 80
  private var destinationDirectoryPath: String? = null

  init {
    destinationDirectoryPath = activity.cacheDir.path + File.separator + "images"
  }

  @Throws(IOException::class)
  @JvmOverloads fun compressToFile(
    imageFile: File,
    compressedFileName: String = imageFile.name
  ): File {
    return compressImage(
        imageFile, maxWidth, maxHeight, compressFormat, quality,
        destinationDirectoryPath + File.separator + compressedFileName
    )
  }

  private fun compressImage(
    imageFile: File,
    reqWidth: Int,
    reqHeight: Int,
    compressFormat: Bitmap.CompressFormat,
    quality: Int,
    destinationPath: String
  ): File {
    var fileOutputStream: FileOutputStream? = null
    val file = File(destinationPath).parentFile
    if (file != null && !file.exists()) {
      file.mkdirs()
    }
    try {
      fileOutputStream = FileOutputStream(destinationPath)
      // write the compressed bitmap at the destination specified by destinationPath.
      decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight).compress(
          compressFormat, quality,
          fileOutputStream
      )
    } finally {
      if (fileOutputStream != null) {
        fileOutputStream.flush()
        fileOutputStream.close()
      }
    }

    return File(destinationPath)
  }

  private fun decodeSampledBitmapFromFile(
    imageFile: File,
    reqWidth: Int,
    reqHeight: Int
  ): Bitmap {
    // First decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(imageFile.absolutePath, options)

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false

    var scaledBitmap = BitmapFactory.decodeFile(imageFile.absolutePath, options)
        ?: throw IOException("Unable to decode image. The file may be corrupted or unsupported.")

    //check the rotation of the image and display it properly
    val exif = ExifInterface(imageFile.absolutePath)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
    val matrix = Matrix()
    when (orientation) {
      6 -> matrix.postRotate(90.0f)
      3 -> matrix.postRotate(180.0f)
      8 -> matrix.postRotate(270.0f)
    }
    scaledBitmap =
      Bitmap.createBitmap(
          scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height,
          matrix, true
      )
    return scaledBitmap
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
      while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
        inSampleSize *= 2
      }
    }

    return inSampleSize
  }

}