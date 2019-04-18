package com.delhivery.orion.ui.home.fragments.bids

import android.databinding.ViewDataBinding
import android.view.View
import com.delhivery.orion.data.home.HomeBidsHeaderAction_ConfirmedBids
import com.delhivery.orion.data.home.HomeBidsHeaderAction_MyBids
import com.delhivery.orion.data.home.HomeBidsSearchAction_Search
import com.delhivery.orion.databinding.ViewHomeBidsHeaderItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsSearchItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsSearchSpinnerItemBinding
import com.delhivery.orion.databinding.ViewHomeBidsWarningItemBinding
import com.delhivery.orion.ui.base.BaseViewHolder

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseHomeBidsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseHomeBidsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: HomeBidsRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: HomeBidsRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: HomeBidsRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}

/**
 * Header item view holder
 */
internal class HomeBidsHeaderItemVH(binding: ViewHomeBidsHeaderItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewHomeBidsHeaderItemBinding, HomeBidsHeaderItem>(binding) {
  override fun bind(
    item: HomeBidsHeaderItem,
    _interface: HomeBidsRVAdapterInterface
  ) {
    binding.myBids = item.data.myBids.toString()
    binding.confirmedBids = item.data.confirmedBids.toString()
    binding.viewMyBids.clickToAction(HomeBidsHeaderAction_MyBids, item, _interface)
    binding.viewConfirmedBids.clickToAction(HomeBidsHeaderAction_ConfirmedBids, item, _interface)
  }
}

/**
 * Search item view holder
 */
internal class HomeBidsSearchItemVH(binding: ViewHomeBidsSearchItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewHomeBidsSearchItemBinding, HomeBidsSearchItem>(binding) {
  override fun bind(
    item: HomeBidsSearchItem,
    _interface: HomeBidsRVAdapterInterface
  ) {
    binding.loadRequests = item.data.loadRequests
    binding.editSearch.clickToAction(HomeBidsSearchAction_Search, item, _interface)
  }
}

/**
 * Bid request item view holder
 */
class HomeBidsRequestItemVH(binding: ViewHomeBidsRequestItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewHomeBidsRequestItemBinding, HomeBidsRequestItem>(binding) {
  override fun bind(
    item: HomeBidsRequestItem,
    _interface: HomeBidsRVAdapterInterface
  ) {
    binding.request = item.data
  }
}

/**
 * Bids warning item view holder
 */
internal class HomeBidsWarningItemVH(binding: ViewHomeBidsWarningItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewHomeBidsWarningItemBinding, HomeBidsWarningItem>(binding) {
  override fun bind(
    item: HomeBidsWarningItem,
    _interface: HomeBidsRVAdapterInterface
  ) {
    binding.data = item.data
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Search load dummy header
 */
internal class HomeBidsSearchSpinnerItemVH(binding: ViewHomeBidsSearchSpinnerItemBinding) :
    BaseHomeBidsRVAdapterViewHolder<ViewHomeBidsSearchSpinnerItemBinding, HomeBidsSearchSpinnerItem>(
        binding
    ) {
  override fun bind(
    item: HomeBidsSearchSpinnerItem,
    _interface: HomeBidsRVAdapterInterface
  ) {

  }
}