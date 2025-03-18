package com.delhivery.axle.ui.profile.raterewards.fragments.rewards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewRewardsProgressItemBinding
import com.delhivery.axle.databinding.ViewYourRewardsItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.YourRewardsRVAdapterItemType.Progress
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.YourRewardsRVAdapterItemType.Timeout
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.YourRewardsRVAdapterItemType.Warning
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.YourRewardsRVAdapterItemType.YourRewards

/**
 * RV Adapter for [YourRewards fragment]
 */
class YourRewardsRVAdapter(private val _interface: YourRewardsAdapterInterface) :
  BaseDataRVAdapter<BaseYourRewardsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
    _interface
  ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (YourRewardsRVAdapterItemType.byTypeId(viewType)) {
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    Progress -> ViewRewardsProgressItemBinding.inflate(inflater, parent, false)
    else -> ViewYourRewardsItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewRewardsProgressItemBinding -> YourRewardsProgressItemVH(binding)
    is ViewWarningItemBinding -> YourRewardsWarningItemVH(binding)
    is ViewTimeOutItemBinding -> YourRewardsTimeOutItemVH(binding)
    else -> YourRewardsItemVH(binding as ViewYourRewardsItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseYourRewardsRVAdapterItem<*>
  ) {
    when (holder) {
      is YourRewardsProgressItemVH -> holder.bind(item as YourRewardsProgressItem, _interface)
      is YourRewardsWarningItemVH -> holder.bind(item as YourRewardsWarningItem, _interface)
      is YourRewardsTimeOutItemVH -> holder.bind(item as YourRewardsTimeoutItem, _interface)
      is YourRewardsItemVH -> holder.bind(item as YourRewardsItem, _interface)
    }
  }

  /**
   *
   * Reset to empty state with progress bar
   *
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseYourRewardsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(YourRewardsProgressItem(), AddUpdate))
      items.filter { it.type == YourRewards || it.type == Warning || it.type == Timeout }
        .map { Pair(it, Remove) }
        .let {
          addAll(it)
        }
    }
      .let {
        operation(it)
      }
  }

}