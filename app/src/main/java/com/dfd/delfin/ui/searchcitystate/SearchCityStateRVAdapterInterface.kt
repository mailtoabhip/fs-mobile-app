package com.dfd.delfin.ui.searchcitystate

import com.dfd.delfin.data.CitySelected
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter

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