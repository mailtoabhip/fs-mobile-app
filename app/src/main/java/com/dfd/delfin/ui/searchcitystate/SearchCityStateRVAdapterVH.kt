package com.dfd.delfin.ui.searchcitystate

import android.view.View
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.data.CitySelected
import com.dfd.delfin.databinding.*
import com.dfd.delfin.ui.base.BaseViewHolder

abstract class BaseSearchCityStateRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseCityStateRVAdapterItem<*>>(
    binding: B
) :
    BaseViewHolder<B>(binding) {
    abstract fun bind(
        item: IT,
        _interface: SearchCityStateRVAdapterInterface
    )

    /**
     * Add on click listener for action
     */
    protected fun View.clickToAction(
        actionId: String,
        item: IT,
        _interface: SearchCityStateRVAdapterInterface
    ) = setOnClickListener { action(actionId, item, _interface) }

    /**
     * Post action to UI
     */
    protected fun View.action(
        actionId: String,
        item: IT,
        _interface: SearchCityStateRVAdapterInterface
    ) = post { _interface.handleAction(actionId, item) }

}

/**
 * Search data item view holder
 */
class SearchDataItemVH(binding: ViewSearchCityStateItemBinding) :
    BaseSearchCityStateRVAdapterViewHolder<ViewSearchCityStateItemBinding, SearchDataItem>(binding) {
    override fun bind(
        item: SearchDataItem,
        _interface: SearchCityStateRVAdapterInterface
    ) {
        binding.request = item.data
        binding.checkboxCityState.clickToAction(CitySelected,item, _interface)
        if(selectedCityStates.any{ it.orionDbCityCode.equals(item.data.orionDbCityCode) && it.cityName() == item.data.cityName()}){
            binding.checkboxCityState.isChecked = true
        }else{
            binding.checkboxCityState.isChecked = false
        }

    }
}

/**
 * Search warning item view holder
 */
internal class SearchWarningItemVH(binding: ViewWarningItemBinding) :
    BaseSearchCityStateRVAdapterViewHolder<ViewWarningItemBinding, SearchWarningItem>(binding) {
    override fun bind(
        item: SearchWarningItem,
        _interface: SearchCityStateRVAdapterInterface
    ) {
        binding.title = item.data.title
        binding.subTitle = item.data.subtitle
        binding.actionLabel = item.data.actionLabel
        binding.btnAction.clickToAction(item.data.actionId, item, _interface)
    }
}

/**
 * Search timeout view holder
 */
internal class SearchTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseSearchCityStateRVAdapterViewHolder<ViewTimeOutItemBinding, SearchTimeoutItem>(binding) {
    override fun bind(
        item: SearchTimeoutItem,
        _interface: SearchCityStateRVAdapterInterface
    ) {
        binding.title = item.data.title
        binding.subTitle = item.data.subtitle
        binding.actionLabel = item.data.actionLabel
        binding.btnAction.clickToAction(item.data.actionId, item, _interface)
    }
}

/**
 * Progress inline viewholder
 */
internal class SearchProgressItemVH(binding: ViewSearchCityProgressItemBinding) :
    BaseSearchCityStateRVAdapterViewHolder<ViewSearchCityProgressItemBinding, SearchProgressItem>(
        binding
    ) {
    override fun bind(
        item: SearchProgressItem,
        _interface: SearchCityStateRVAdapterInterface
    ) {

    }
}