package com.delhivery.axle.ui.home.fragments.pod

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.home.pod.HomePodHeaderItemData
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomePodsHeaderItemBinding
import com.delhivery.axle.databinding.ViewHomeSearchItemBinding
import com.delhivery.axle.databinding.ViewNewPodItemBinding
import com.delhivery.axle.databinding.ViewPodItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Update
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Header
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Pod
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Search
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Warning

/**
 * RV adapter for [HomePodsFragment]
 */
class HomePodRVAdapter(private val _interface: HomePodRVAdapterInterface) :
    BaseDataRVAdapter<BaseHomePodRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {
  
  /**
   * Track if HPOD section is currently selected
   */
  var isHPODSection: Boolean = false

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomePodRVAdapterItemType.byTypeId(viewType)) {
    Header -> ViewHomePodsHeaderItemBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Progress -> ViewHomeBidsProgressItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    Search -> ViewHomeSearchItemBinding.inflate(inflater, parent, false)
    else -> ViewNewPodItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomePodsHeaderItemBinding -> HomePodHeaderItemVH(binding)
    is ViewNewPodItemBinding -> HomePodItemVH(binding)
    is ViewWarningItemBinding -> HomePodWarningItemVH(binding)
    is ViewTimeOutItemBinding -> HomePodTimeOutItemVH(binding)
    is ViewHomeBidsProgressItemBinding -> HomePodProgressItemVH(binding)
    is ViewHomeSearchItemBinding -> HomePodSearchItemVH(binding)
    else -> HomePodItemVH(binding as ViewNewPodItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomePodRVAdapterItem<*>
  ) {
    when (holder) {
      is HomePodHeaderItemVH -> holder.bind(item as HomePodHeaderItem, _interface)
      is HomePodItemVH -> {
        holder.bind(item as HomePodTripItem, _interface)
        // Set isHPODSection flag in the binding
        (holder.binding as? com.delhivery.axle.databinding.ViewNewPodItemBinding)?.isHPODSection = isHPODSection
      }
      is HomePodWarningItemVH -> holder.bind(item as HomePodWarningItem, _interface)
      is HomePodTimeOutItemVH -> holder.bind(item as HomePodTimeoutItem, _interface)
      is HomePodSearchItemVH -> holder.bind(item as HomePodSearchItem, _interface)
    }
  }

  /**
   * Reset all data, remove all errors/transactions
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(HomePodHeaderItem(HomePodHeaderItemData()), Update))
      add(Pair(HomePodProgressItem(), AddUpdate))
      items.filter { it.type == Warning || it.type == Timeout || it.type == Pod || it.type == Search }
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