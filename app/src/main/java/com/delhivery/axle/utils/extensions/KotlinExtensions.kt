package com.delhivery.axle.utils.extensions

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.widget.AutoCompleteTextView
import android.widget.EditText
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject




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

fun EditText.addRxTextWatcher(): Observable<String?> {

  return Observable.create {
    addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) {
      }

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        s?.toString()?.let { it1 -> it.onNext(it1) }
      }

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
      }
    })
  }
}


