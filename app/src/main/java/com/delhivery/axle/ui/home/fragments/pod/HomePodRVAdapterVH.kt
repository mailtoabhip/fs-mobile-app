package com.delhivery.axle.ui.home.fragments.pod

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.home.bids.HomeBidsSearchAction_Search
import com.delhivery.axle.data.home.pod.HomePodParentAction
import com.delhivery.axle.databinding.ViewHomeBidsHeaderItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeSearchItemBinding
import com.delhivery.axle.databinding.ViewPodChildItemBinding
import com.delhivery.axle.databinding.ViewPodParentItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder

/**
 * Base Home Pod RV adapter view holder
 */
abstract class BaseHomePodsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseHomePodRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: HomePodRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    position: Int,
    _interface: HomePodRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, position, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    position: Int,
    _interface: HomePodRVAdapterInterface
  ) = post { _interface.handleAction(actionId, position, item) }
}

/**
 * Pod Header item view holder
 */
internal class HomePodHeaderItemVH(binding: ViewHomeBidsHeaderItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewHomeBidsHeaderItemBinding, HomePodHeaderItem>(binding) {
  override fun bind(
    item: HomePodHeaderItem,
    _interface: HomePodRVAdapterInterface
  ) {
    binding.myBids = when (item.data.myBids) {
      -1 -> ""
      else -> item.data.myBids.toString() + " Bids"
    }
    binding.confirmedBids = when (item.data.confirmedBid) {
      -1 -> ""
      else -> item.data.confirmedBid.toString() + " Bids"
    }
    binding.lostBids = when (item.data.lostBids) {
      -1 -> ""
      else -> item.data.lostBids.toString() + " Bids"
    }
  }
}

/**
 * Pod item view holder
 */
internal class HomePodSearchItemVH(binding: ViewHomeSearchItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewHomeSearchItemBinding, HomePodSearchItem>(binding) {
  override fun bind(
    item: HomePodSearchItem,
    _interface: HomePodRVAdapterInterface
  ) {
    binding.editQuery.hint = "Origin / Destination"
    binding.editQuery.clickToAction(HomeBidsSearchAction_Search, item, adapterPosition, _interface)
  }
}

/**
 * Pod parent item view holder
 */
class HomePodParentItemVH(binding: ViewPodParentItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewPodParentItemBinding, HomePodParentItem>(binding) {
  override fun bind(
    item: HomePodParentItem,
    _interface: HomePodRVAdapterInterface
  ) {
    binding.trip = item.data
    binding.root.clickToAction(HomePodParentAction, item, adapterPosition, _interface)
  }
}

/**
 * Pod child item view holder
 */
class HomePodChildItemVH(binding: ViewPodChildItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewPodChildItemBinding, HomePodChildItem>(binding) {
  override fun bind(
    item: HomePodChildItem,
    _interface: HomePodRVAdapterInterface
  ) {
    binding.textTruckDetails.text = item.data.name
    binding.btnEpod.setOnClickListener {
      //upload epod for this trip
    }
    binding.btnCourier.setOnClickListener {
      
    }
  }
}

/**
 * Pod warning item view holder
 */
internal class HomePodWarningItemVH(binding: ViewWarningItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewWarningItemBinding, HomePodWarningItem>(binding) {
  override fun bind(
    item: HomePodWarningItem,
    _interface: HomePodRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, adapterPosition, _interface)
  }
}

/**
 * Pod timeout view holder
 */
internal class HomePodTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewTimeOutItemBinding, HomePodTimeoutItem>(binding) {
  override fun bind(
    item: HomePodTimeoutItem,
    _interface: HomePodRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, adapterPosition, _interface)
  }
}

/**
 * Progress inline viewholder
 */
internal class HomePodProgressItemVH(binding: ViewHomeBidsProgressItemBinding) :
    BaseHomePodsRVAdapterViewHolder<ViewHomeBidsProgressItemBinding, HomePodProgressItem>(
        binding
    ) {
  override fun bind(
    item: HomePodProgressItem,
    _interface: HomePodRVAdapterInterface
  ) {

  }
}