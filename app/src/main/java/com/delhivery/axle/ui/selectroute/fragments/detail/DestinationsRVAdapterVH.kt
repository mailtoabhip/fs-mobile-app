package com.delhivery.axle.ui.selectroute.fragments.detail

import androidx.databinding.ViewDataBinding
import android.view.View
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.StateModel
import com.delhivery.axle.databinding.ViewSelectRouteDestinationItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

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