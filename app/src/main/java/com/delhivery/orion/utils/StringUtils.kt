package com.delhivery.orion.utils

import com.delhivery.orion.injection.scope.ActivityScope

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
  fun capitalize(string: String?): String {
    if (string == null) {
      return ""
    }

    if (string.isEmpty()) {
      return string
    }
    val ch = string[0]
    return if (Character.isTitleCase(ch)) {
      string
    } else Character.toTitleCase(ch) + string.substring(1)
  }
}