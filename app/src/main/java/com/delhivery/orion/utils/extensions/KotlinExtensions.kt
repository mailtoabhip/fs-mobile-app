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