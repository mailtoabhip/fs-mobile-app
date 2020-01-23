package com.delhivery.axle.utils.extensions

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

/**
 * Check if string is not null and not blank either
 *
 * @return [Boolean]
 */
fun String?.isNotNullOrEmpty(): Boolean = this != null && isNotEmpty()

/**
 * Operator extension for ! or not()
 * if boolean then ! equals variable is false
 * else true if null
 *
 * @return Boolean for not
 */
operator fun Any?.not() = when (this) {
  is Boolean -> this == false
  else -> this == null
}

/**
 * Any variable safe comparison
 *
 * @return Boolean
 */
fun Any?.safeEquals(another: Any?): Boolean {
  if (this != null && another != null) {
    return this == another
  } else if (this == null && another == null) {
    return true
  }
  return false
}

/**
 * Safe substring for indexes
 */
fun String?.safeSubstring(
  start: Int,
  end: Int
) = this?.apply {
  return substring(Math.min(start, length), Math.min(end, length))
} ?: "$this"

/**
 * Extract file name from uri
 */
fun ContentResolver.getFileName(uri: Uri): String {
  var name = ""
  val returnCursor = this.query(uri, null, null, null, null)
  if (returnCursor != null) {
    val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor.moveToFirst()
    name = returnCursor.getString(nameIndex)
    returnCursor.close()
  }
  return name
}