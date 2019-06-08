package com.delhivery.orion.ui.selectroute.fragments.detail

import android.databinding.ViewDataBinding
import android.view.View
import com.delhivery.orion.data.BaseKeyTypeModel
import com.delhivery.orion.data.StateModel
import com.delhivery.orion.databinding.ViewSelectRouteDestinationItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseDestinationsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseKeyTypeModel<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _clickListener: ItemClickListener<IT>
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    item: IT,
    _clickListener: ItemClickListener<IT>
  ) = setOnClickListener { action(item, _clickListener) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    item: IT,
    _clickListener: ItemClickListener<IT>
  ) = post { _clickListener.onItemClicked(item) }
}

/**
 * Destination item view holder
 */
class DestinationsItemVH(
  binding: ViewSelectRouteDestinationItemBinding
) : BaseDestinationsRVAdapterViewHolder<ViewSelectRouteDestinationItemBinding, StateModel>(
    binding
) {
  override fun bind(
    item: StateModel,
    _clickListener: ItemClickListener<StateModel>
  ) {
    binding.state = item
    binding.check.isChecked = item.checked
    binding.check.isClickable = false
    binding.root.clickToAction(item, _clickListener)
  }

}