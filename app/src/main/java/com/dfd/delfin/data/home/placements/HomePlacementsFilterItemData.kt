package com.dfd.delfin.data.home.placements

import com.dfd.delfin.data.BaseKeyTypeModel


data class HomePlacementsFilterItemData(
    var filterType: String, var delayedCount:String, var missingDetailsCount:String, var expectedCount:String
) : BaseKeyTypeModel<String>() {
    override fun key() = HomePlacementsFilterItemDataKeyPrefix
}

/* unique key for filter */
const val HomePlacementsFilterItemDataKeyPrefix = "filter_"

/* actions */
const val HomePlacementsFilterDelay = "filter_delay"
const val HomePlacementsFilterMissing = "filter_missing"
const val HomePlacementsFilterExpected = "filter_expected"
const val HomePlacementsCallDriver = "call_driver"
const val HomePlacementsShareOnWhatsapp = "share_whatsapp"


