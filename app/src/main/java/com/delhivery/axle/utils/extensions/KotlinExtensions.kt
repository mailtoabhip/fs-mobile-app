package com.delhivery.axle.utils.extensions

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.io.Serializable

/**
 * Check if string is not null and not blank either
 *
 * @return [Boolean]
 */
fun String?.isNotNullOrEmpty(): Boolean = this != null && isNotEmpty()

/**
 * Check if collection is not null and not blank either
 *
 * @return [Boolean]
 */
fun Collection<Any>?.isNotEmpty(): Boolean = this != null && !isEmpty()

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
  fun androidx.appcompat.widget.SearchView.getQueryTextChangeObservable(): Observable<String> {
    val subject = PublishSubject.create<String>()

    setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String?): Boolean {
        subject.onComplete()
        return true
      }

      override fun onQueryTextChange(newText: String): Boolean {
        subject.onNext(newText)
        return true
      }
    })

    return subject
  }

fun <T : Serializable?> Intent.getSerializable(key: String, m_class: Class<T>): T? {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    getSerializableExtra(key, m_class)
  else
    getSerializableExtra(key) as T
}

fun <T: Serializable?> Bundle.getSerializableExtra(key: String, m_class: Class<T>): T?{
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    this.getSerializable(key,m_class)
  else getSerializable(key) as T
}

