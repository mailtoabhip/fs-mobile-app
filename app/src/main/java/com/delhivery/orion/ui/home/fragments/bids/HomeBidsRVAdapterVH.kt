package com.delhivery.orion.ui.home.fragments.bids

import android.databinding.ViewDataBinding
import com.delhivery.orion.databinding.ViewHomeBidsHeaderItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsSearchItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseHomeBidsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseHomeBidsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(item: IT)
}

/**
 * Header item view holder
 */
internal class HomeBidsHeaderItemVH(binding: ViewHomeBidsHeaderItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewHomeBidsHeaderItemBinding, HomeBidsHeaderItem>(binding) {
  override fun bind(item: HomeBidsHeaderItem) {
    binding.myBids = item.data.myBids.toString()
    binding.confirmedBids = item.data.confirmedBids.toString()
  }
}

/**
 * Search item view holder
 */
internal class HomeBidsSearchItemVH(binding: ViewHomeBidsSearchItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewHomeBidsSearchItemBinding, HomeBidsSearchItem>(binding) {
  override fun bind(item: HomeBidsSearchItem) {
    binding.loadRequests = item.data.loadRequests
  }
}

/**
 * Bid request item view holder
 */
internal class HomeBidsRequestItemVH(binding: ViewHomeBidsRequestItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewHomeBidsRequestItemBinding, HomeBidsRequestItem>(binding) {
  override fun bind(item: HomeBidsRequestItem) {
    binding.request = item.data
  }
}