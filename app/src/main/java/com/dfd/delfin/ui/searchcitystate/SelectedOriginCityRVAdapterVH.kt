package com.dfd.delfin.ui.searchcitystate

import com.dfd.delfin.data.CitySelected
import com.dfd.delfin.databinding.*

/**
 * Search data item view holder
 */
class SearchOriginCityDataItemVH(binding: ViewSearchCityItemBinding) :
    BaseSearchCityStateRVAdapterViewHolder<ViewSearchCityItemBinding, SearchDataItem>(binding) {
    override fun bind(
        item: SearchDataItem,
        _interface: SearchCityStateRVAdapterInterface
    ) {
        binding.request = item.data
        binding.cityLayout.clickToAction(CitySelected,item,_interface)

    }
}
