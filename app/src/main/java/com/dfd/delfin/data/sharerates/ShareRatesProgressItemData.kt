package com.dfd.delfin.data.sharerates

import com.dfd.delfin.data.BaseKeyTypeModel

/**
 * Progress item data for Share Rates list
 */
data class ShareRatesProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = ShareRatesProgressItemDataKey
}

private const val ShareRatesProgressItemDataKey = "progress"