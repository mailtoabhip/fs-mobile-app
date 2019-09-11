package com.delhivery.axle.utils.extensions

import java.util.Calendar
import java.util.Date

/**
 * Convert [Date] to [Calendar]
 */
fun Date.toCalendar() = Calendar.getInstance().apply {
  time = this@toCalendar
}

/**
 * Convert [Calendar] to [Date]
 */
fun Calendar.toDate() = time