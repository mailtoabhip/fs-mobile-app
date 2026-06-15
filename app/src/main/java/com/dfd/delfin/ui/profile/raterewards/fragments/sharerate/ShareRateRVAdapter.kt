package com.dfd.delfin.ui.profile.raterewards.fragments.sharerate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.databinding.ViewRouteProgressItemBinding
import com.dfd.delfin.databinding.ViewShareRateItemBinding
import com.dfd.delfin.databinding.ViewTimeOutItemBinding
import com.dfd.delfin.databinding.ViewWarningItemBinding
import com.dfd.delfin.ui.base.BaseViewHolder
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.dfd.delfin.ui.profile.raterewards.fragments.sharerate.ShareRateRVAdapterItemType.Progress
import com.dfd.delfin.ui.profile.raterewards.fragments.sharerate.ShareRateRVAdapterItemType.ShareRate
import com.dfd.delfin.ui.profile.raterewards.fragments.sharerate.ShareRateRVAdapterItemType.Timeout
import com.dfd.delfin.ui.profile.raterewards.fragments.sharerate.ShareRateRVAdapterItemType.Warning

/**
 * RV Adapter for [ShareRates fragment]
 */
class ShareRateRVAdapter(private val _interface: ShareRateAdapterInterface) :
  BaseDataRVAdapter<BaseShareRateRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
    _interface
  ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (ShareRateRVAdapterItemType.byTypeId(viewType)) {
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    Progress -> ViewRouteProgressItemBinding.inflate(inflater, parent, false)
    else -> ViewShareRateItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewRouteProgressItemBinding -> ShareRatesProgressItemVH(binding)
    is ViewWarningItemBinding -> ShareRatesWarningItemVH(binding)
    is ViewTimeOutItemBinding -> ShareRatesTimeOutItemVH(binding)
    else -> ShareRatesItemVH(binding as ViewShareRateItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseShareRateRVAdapterItem<*>
  ) {
    when (holder) {
      is ShareRatesProgressItemVH -> holder.bind(item as ShareRatesProgressItem, _interface)
      is ShareRatesWarningItemVH -> holder.bind(item as ShareRatesWarningItem, _interface)
      is ShareRatesTimeOutItemVH -> holder.bind(item as ShareRatesTimeoutItem, _interface)
      is ShareRatesItemVH -> holder.bind(item as ShareRatesItem, _interface)
    }
  }

  /**
   *
   * Reset to empty state with progress bar
   *
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseShareRateRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(ShareRatesProgressItem(), AddUpdate))
      items.filter { it.type == ShareRate || it.type == Warning || it.type == Timeout }
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