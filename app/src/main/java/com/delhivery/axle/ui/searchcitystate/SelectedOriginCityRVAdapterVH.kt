package com.delhivery.axle.ui.searchcitystate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.CitySelected
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType

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
