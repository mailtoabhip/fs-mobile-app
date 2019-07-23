package com.delhivery.axle.utils.extensions

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

/**
 * Check if resource exist
 */
fun Context.hasResource(resId: Int) = try {
  resources.getResourceName(resId) != null
} catch (e: Resources.NotFoundException) {
  false
}

/**
 * Get bitmap from res id
 */
fun Context.getBitmapRes(resId: Int): Bitmap {
  var drawable = ContextCompat.getDrawable(this, resId)
  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
    drawable = DrawableCompat.wrap(drawable!!)
        .mutate()
  }

  val bitmap = Bitmap.createBitmap(
      drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
  )

  val canvas = Canvas(bitmap)
  drawable.setBounds(0, 0, canvas.width, canvas.height)
  drawable.draw(canvas)
  return bitmap
}