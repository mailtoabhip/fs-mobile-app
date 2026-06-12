package com.dfd.delfin.ui.profile.raterewards.fragments.rewards

import android.graphics.Typeface
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.R
import com.dfd.delfin.data.yourrewards.YourRewardsItemDataAction_DownloadProof
import com.dfd.delfin.data.yourrewards.YourRewardsItemDataAction_ViewDetails
import com.dfd.delfin.databinding.ViewRewardsProgressItemBinding
import com.dfd.delfin.databinding.ViewTimeOutItemBinding
import com.dfd.delfin.databinding.ViewWarningItemBinding
import com.dfd.delfin.databinding.ViewYourRewardsItemBinding
import com.dfd.delfin.ui.base.BaseViewHolder

/**
 * Base rv adapter view holder
 */
abstract class BaseYourRewardsRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseYourRewardsRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {

  /**
   * Binds item to adapter
   */
  abstract fun bind(
    item: IT,
    _interface: YourRewardsAdapterInterface
  )

  /**
   * Add on click listener for action with position
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    position: Int,
    _interface: YourRewardsAdapterInterface
  ) = setOnClickListener { action(actionId, item, position, _interface) }

  /**
   * Post action to UI with position
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    position: Int,
    _interface: YourRewardsAdapterInterface
  ) = post { _interface.handleAction(actionId, item,position) }
}

/**
 * item view holder
 */
class YourRewardsItemVH(binding: ViewYourRewardsItemBinding) :
    BaseYourRewardsRVAdapterViewHolder<ViewYourRewardsItemBinding, YourRewardsItem>(
        binding
    ) {
  override fun bind(
    item: YourRewardsItem,
    _interface: YourRewardsAdapterInterface
  ) {
    binding.request = item.data
    binding.fullViewLl.clickToAction(
      YourRewardsItemDataAction_ViewDetails,item, bindingAdapterPosition, _interface
    )
    if(item.data.verificationState?.lowercase().equals("pending")){
      binding.rewardsValue.setTypeface(null, Typeface.NORMAL)
      binding.statusText.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_all_round_corner_light_orange))
    }else if(item.data.verificationState?.lowercase().equals("verified")){
      binding.statusText.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_all_round_corner_light_green_12))
      binding.rewardsValue.setTypeface(null, Typeface.BOLD)
    }else {
      binding.statusText.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_all_round_corner_light_pink_12))
      binding.rewardsValue.setTypeface(null, Typeface.NORMAL)
    }
    binding.downloadIcon.clickToAction(
      YourRewardsItemDataAction_DownloadProof,item, bindingAdapterPosition, _interface
    )

  }
}

/**
 * Progress view holder
 */
internal class YourRewardsProgressItemVH(binding: ViewRewardsProgressItemBinding) :
  BaseYourRewardsRVAdapterViewHolder<ViewRewardsProgressItemBinding, YourRewardsProgressItem>(
        binding
    ) {
  override fun bind(
    item: YourRewardsProgressItem,
    _interface: YourRewardsAdapterInterface
  ) {
    //Do nothing
  }
}

/**
 * warning item view holder
 */
internal class YourRewardsWarningItemVH(binding: ViewWarningItemBinding) :
  BaseYourRewardsRVAdapterViewHolder<ViewWarningItemBinding, YourRewardsWarningItem>(
        binding
    ) {
  override fun bind(
    item: YourRewardsWarningItem,
    _interface: YourRewardsAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.img.setImageResource(R.drawable.ic_no_trips)
    binding.btnAction.visibility = View.GONE
  }
}

/**
 * timeout view holder
 */
internal class YourRewardsTimeOutItemVH(binding: ViewTimeOutItemBinding) :
  BaseYourRewardsRVAdapterViewHolder<ViewTimeOutItemBinding, YourRewardsTimeoutItem>(
        binding
    ) {
  override fun bind(
    item: YourRewardsTimeoutItem,
    _interface: YourRewardsAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, bindingAdapterPosition, _interface)
  }
}