package com.delhivery.orion.utils.extensions

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