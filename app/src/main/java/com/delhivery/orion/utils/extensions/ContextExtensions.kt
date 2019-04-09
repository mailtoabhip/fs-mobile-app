package com.delhivery.orion.utils.extensions

import android.content.Context
import android.content.res.Resources

/**
 * Check if resource exist
 */
fun Context.hasResource(resId: Int) = try {
  resources.getResourceName(resId) != null
} catch (e: Resources.NotFoundException) {
  false
}
