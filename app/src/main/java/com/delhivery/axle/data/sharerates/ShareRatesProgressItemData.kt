package com.delhivery.axle.data.sharerates

import com.delhivery.axle.data.BaseKeyTypeModel

/**
 * Progress item data for Share Rates list
 */
data class ShareRatesProgressItemData(
  val showing: Boolean = true
) : BaseKeyTypeModel<String>() {
  override fun key() = ShareRatesProgressItemDataKey
}

private const val ShareRatesProgressItemDataKey = "progress"