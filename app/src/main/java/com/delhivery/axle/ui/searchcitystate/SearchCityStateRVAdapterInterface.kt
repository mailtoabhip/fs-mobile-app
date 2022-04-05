package com.delhivery.axle.ui.searchcitystate

import com.delhivery.axle.data.CitySelected
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter

interface SearchCityStateRVAdapterInterface : BaseDataRVAdapter.ItemClickListener<BaseCityStateRVAdapterItem<*>> {
    override fun onItemClicked(
        item: BaseCityStateRVAdapterItem<*>
    ) {
        if (item.type == SearchCityStateRvAdapterItemType.CityItem) {
            handleAction(CitySelected, item)
        }
    }

    /**
     * Handle specific action
     */
    fun handleAction(
        actionId: String,
        item: BaseCityStateRVAdapterItem<*>
    )
}