package com.delhivery.orion.ui.home.fragments.trips

import android.databinding.ViewDataBinding
import com.delhivery.orion.databinding.ViewHomeTripsDetailsItemBinding
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
internal class HomeTripsItemVH(binding: ViewHomeTripsDetailsItemBinding) :
    BaseHomeTripsRVAdapterViewHolder<ViewHomeTripsDetailsItemBinding, HomeTripsItem>(binding) {
  override fun bind(item: HomeTripsItem) {
    binding.trip = item.data
  }
}