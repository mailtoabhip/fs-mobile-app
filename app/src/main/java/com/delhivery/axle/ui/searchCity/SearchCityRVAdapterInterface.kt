package com.delhivery.axle.ui.searchCity

import com.delhivery.axle.data.CitySelected
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter

interface SearchCityRVAdapterInterface : BaseDataRVAdapter.ItemClickListener<BaseCityRVAdapterItem<*>> {
    override fun onItemClicked(
        item: BaseCityRVAdapterItem<*>
    ) {
        if (item.type == SearchCityRvAdapterItemType.CityItem) {
            handleAction(CitySelected, item)
        }
    }

    /**
     * Handle specific action
     */
    fun handleAction(
        actionId: String,
        item: BaseCityRVAdapterItem<*>
    )
}