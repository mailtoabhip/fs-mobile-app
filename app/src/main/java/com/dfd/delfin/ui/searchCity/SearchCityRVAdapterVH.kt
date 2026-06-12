package com.dfd.delfin.ui.searchCity

import android.view.View
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.data.CitySelected
import com.dfd.delfin.databinding.*
import com.dfd.delfin.ui.base.BaseViewHolder


abstract class BaseSearchCityRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseCityRVAdapterItem<*>>(
    binding: B
) :
    BaseViewHolder<B>(binding) {
    abstract fun bind(
        item: IT,
        _interface: SearchCityRVAdapterInterface
    )

    /**
     * Add on click listener for action
     */
    protected fun View.clickToAction(
        actionId: String,
        item: IT,
        _interface: SearchCityRVAdapterInterface
    ) = setOnClickListener { action(actionId, item, _interface) }

    /**
     * Post action to UI
     */
    protected fun View.action(
        actionId: String,
        item: IT,
        _interface: SearchCityRVAdapterInterface
    ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Search data item view holder
 */
class SearchDataItemVH(binding: ViewSearchCityItemBinding) :
    BaseSearchCityRVAdapterViewHolder<ViewSearchCityItemBinding, SearchDataItem>(binding) {
    override fun bind(
        item: SearchDataItem,
        _interface: SearchCityRVAdapterInterface
    ) {
        binding.request = item.data

        binding.cityLayout.clickToAction(CitySelected ,item, _interface)

    }
}

/**
 * Search warning item view holder
 */
internal class SearchWarningItemVH(binding: ViewWarningItemBinding) :
    BaseSearchCityRVAdapterViewHolder<ViewWarningItemBinding, SearchWarningItem>(binding) {
    override fun bind(
        item: SearchWarningItem,
        _interface: SearchCityRVAdapterInterface
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
    BaseSearchCityRVAdapterViewHolder<ViewTimeOutItemBinding, SearchTimeoutItem>(binding) {
    override fun bind(
        item: SearchTimeoutItem,
        _interface: SearchCityRVAdapterInterface
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
    BaseSearchCityRVAdapterViewHolder<ViewSearchCityProgressItemBinding, SearchProgressItem>(
        binding
    ) {
    override fun bind(
        item: SearchProgressItem,
        _interface: SearchCityRVAdapterInterface
    ) {

    }
}