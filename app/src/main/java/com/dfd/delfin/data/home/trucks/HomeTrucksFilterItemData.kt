package com.dfd.delfin.data.home.trucks

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeTrucksFilterItemData(
    var actionLabel: Boolean
) : BaseKeyTypeModel<String>() {
    override fun key() = HomeTrucksFilterItemDataKeyPrefix
}

/* unique key for filter */
const val HomeTrucksFilterItemDataKeyPrefix = "filter_"

/* actions */
const val HomeTrucksFilterAction = "filter"

const val HomeTrucksVehicleFilterAction = "filter_vehicle"

const val HomeTrucksAvailabilityFilterAction = "filter_availability"

const val HomeTrucksSizeFilterAction = "filter_size"

