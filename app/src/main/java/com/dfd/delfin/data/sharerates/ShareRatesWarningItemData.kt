package com.dfd.delfin.data.sharerates

import com.dfd.delfin.data.BaseKeyTypeModel

/**
 * Warning item data for Share Rates list
 */
data class ShareRatesWarningItemData(
  val title: String,
  val subtitle: String,
  val actionLabel: String,
  val actionId: String
) : BaseKeyTypeModel<String>() {
  override fun key() = ShareRatesWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val ShareRatesWarningItemDataKeyPrefix = "warning_"

/* actions */
const val ShareRatesWarningAction_NoRates = "no_Rates"