package com.delhivery.axle.ui.profile.raterewards.fragments.sharerate

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.data.sharerates.ShareRatesItemDataAction_ViewDetails
import com.delhivery.axle.databinding.ViewRouteProgressItemBinding
import com.delhivery.axle.databinding.ViewShareRateItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder

/**
 * Base share rates rv adapter view holder
 */
abstract class BaseShareRatesRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseShareRateRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {

  /**
   * Binds item to adapter
   */
  abstract fun bind(
    item: IT,
    _interface: ShareRateAdapterInterface
  )

  /**
   * Add on click listener for action with position
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: ShareRateAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI with position
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: ShareRateAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}
/**
 * Share Rates item view holder
 */
class ShareRatesItemVH(binding: ViewShareRateItemBinding) :
    BaseShareRatesRVAdapterViewHolder<ViewShareRateItemBinding, ShareRatesItem>(
        binding
    ) {
  override fun bind(
    item: ShareRatesItem,
    _interface: ShareRateAdapterInterface
  ) {
    binding.request = item.data

    binding.root.clickToAction(
      ShareRatesItemDataAction_ViewDetails, item, _interface
    )
  }
}

/**
 * Share Rates Progress view holder
 */
internal class ShareRatesProgressItemVH(binding: ViewRouteProgressItemBinding) :
  BaseShareRatesRVAdapterViewHolder<ViewRouteProgressItemBinding, ShareRatesProgressItem>(
        binding
    ) {
  override fun bind(
    item: ShareRatesProgressItem,
    _interface: ShareRateAdapterInterface
  ) {
    //Do nothing
  }
}

/**
 * Share Rates warning item view holder
 */
internal class ShareRatesWarningItemVH(binding: ViewWarningItemBinding) :
  BaseShareRatesRVAdapterViewHolder<ViewWarningItemBinding, ShareRatesWarningItem>(
        binding
    ) {
  override fun bind(
    item: ShareRatesWarningItem,
    _interface: ShareRateAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.img.setImageResource(R.drawable.ic_no_trips)
    binding.btnAction.visibility = View.GONE
  }
}

/**
 * Share Rates timeout view holder
 */
internal class ShareRatesTimeOutItemVH(binding: ViewTimeOutItemBinding) :
  BaseShareRatesRVAdapterViewHolder<ViewTimeOutItemBinding, ShareRatesTimeoutItem>(
        binding
    ) {
  override fun bind(
    item: ShareRatesTimeoutItem,
    _interface: ShareRateAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}