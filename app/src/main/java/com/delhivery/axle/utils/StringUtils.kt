package com.delhivery.axle.utils

import com.delhivery.axle.injection.scope.ActivityScope
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * Util functions for [String] types values
 *
 **
 */
@ActivityScope
object StringUtils {

  /**
   * Return Sentence case string
   */
  fun capitalize(string: String?): String? {
    if (string == null) {
      return null
    }

    if (string.isEmpty()) {
      return null
    }
    val ch = string[0]
    return if (Character.isTitleCase(ch)) {
      string
    } else Character.toTitleCase(ch) + string.substring(1)
  }

  fun formatAmount(num: Double): String = DecimalFormat("##,##,##,###").format(num) ?: ""

  fun formatDecimalAmount(num: Double): String = DecimalFormat("##,##,##,###.##").format(num) ?: ""

  fun getCurrency(amount: Double): String{
    val nf: NumberFormat = NumberFormat.getCurrencyInstance(Locale("hi", "IN"))
    val currency: String = nf.format(amount)
    return currency.substring(0,currency.length-3)
  }

}