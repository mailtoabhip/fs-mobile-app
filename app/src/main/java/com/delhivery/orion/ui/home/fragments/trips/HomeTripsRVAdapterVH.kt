package com.delhivery.orion.ui.home.fragments.trips

import android.databinding.ViewDataBinding
import com.delhivery.orion.data.home.HomeBidsRequestItemData
import com.delhivery.orion.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.orion.databinding.ViewHomeTripsSearchItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder

/**
 * Base home trips rv adapter view holder
 */
abstract class BaseHomeTripsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseHomeTripsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT
  )
}

/**
 * Search item view holder
 */
internal class HomeTripsSearchItemVH(binding: ViewHomeTripsSearchItemBinding) :
    BaseHomeTripsRVAdapterViewHolder<ViewHomeTripsSearchItemBinding, HomeTripsSearchItem>(binding) {
  override fun bind(item: HomeTripsSearchItem) {
    /* useless */
  }
}

/**
 * Trip item view holder
 */
internal class HomeTripsItemVH(binding: ViewHomeBidsRequestItemBinding) :
    BaseHomeTripsRVAdapterViewHolder<ViewHomeBidsRequestItemBinding, HomeTripsItem>(binding) {
  override fun bind(item: HomeTripsItem) {
    /* todo fix this with trips item instead of placeholder from bids */
    binding.request = HomeBidsRequestItemData()
  }
}