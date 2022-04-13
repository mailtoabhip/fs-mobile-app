package com.delhivery.axle.ui.searchcitystate

import com.delhivery.axle.data.CitySelected
import com.delhivery.axle.databinding.ViewSearchCityStateItemBinding

class PopularCitiesRVAdapterVH (binding: ViewSearchCityStateItemBinding) :
    BaseSearchCityStateRVAdapterViewHolder<ViewSearchCityStateItemBinding, SearchDataItem>(binding) {
    override fun bind(
        item: SearchDataItem,
        _interface: SearchCityStateRVAdapterInterface
    ) {
        binding.request = item.data
        binding.checkboxCityState.clickToAction(CitySelected,item,_interface)

    }
}