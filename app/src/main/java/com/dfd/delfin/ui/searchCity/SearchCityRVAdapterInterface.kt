package com.dfd.delfin.ui.searchCity

import com.dfd.delfin.data.CitySelected
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter

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