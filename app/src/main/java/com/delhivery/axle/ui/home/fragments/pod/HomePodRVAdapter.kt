package com.delhivery.axle.ui.home.fragments.pod

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.home.pod.HomePodChildItemData
import com.delhivery.axle.data.home.pod.HomePodHeaderItemData
import com.delhivery.axle.databinding.ViewHomeBidsHeaderItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeSearchItemBinding
import com.delhivery.axle.databinding.ViewPodChildItemBinding
import com.delhivery.axle.databinding.ViewPodParentItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseExpandableDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Update
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Child
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Header
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Parent
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Search
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Warning

/**
 * RV adapter for [HomePodsFragment]
 */
class HomePodRVAdapter(private val _interface: HomePodRVAdapterInterface) :
    BaseExpandableDataRVAdapter<BaseHomePodRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (HomePodRVAdapterItemType.byTypeId(viewType)) {
    Header -> ViewHomeBidsHeaderItemBinding.inflate(inflater, parent, false)
    Search -> ViewHomeSearchItemBinding.inflate(inflater, parent, false)
    Parent -> ViewPodParentItemBinding.inflate(inflater, parent, false)
    Child -> ViewPodChildItemBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Progress -> ViewHomeBidsProgressItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    else -> ViewHomeBidsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewHomeBidsHeaderItemBinding -> HomePodHeaderItemVH(binding)
    is ViewHomeSearchItemBinding -> HomePodSearchItemVH(binding)
    is ViewPodParentItemBinding -> HomePodParentItemVH(binding)
    is ViewPodChildItemBinding -> HomePodChildItemVH(binding)
    is ViewWarningItemBinding -> HomePodWarningItemVH(binding)
    is ViewTimeOutItemBinding -> HomePodTimeOutItemVH(binding)
    is ViewHomeBidsProgressItemBinding -> HomePodProgressItemVH(binding)
    else -> HomePodParentItemVH(binding as ViewPodParentItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseHomePodRVAdapterItem<*>
  ) {
    when (holder) {
      is HomePodHeaderItemVH -> holder.bind(item as HomePodHeaderItem, _interface)
      is HomePodSearchItemVH -> holder.bind(item as HomePodSearchItem, _interface)
      is HomePodParentItemVH -> holder.bind(item as HomePodParentItem, _interface)
      is HomePodChildItemVH -> holder.bind(item as HomePodChildItem, _interface)
      is HomePodWarningItemVH -> holder.bind(item as HomePodWarningItem, _interface)
      is HomePodTimeOutItemVH -> holder.bind(item as HomePodTimeoutItem, _interface)
    }
  }

  override fun filterList(query: String) =
    items.filter { it.type == Search || it.data.filter(query) }

  override fun onGroupExpand(
    position: Int,
    podDatas: List<HomePodChildItemData>
  ) {
    items.addAll(position + 1, mutableListOf<BaseHomePodRVAdapterItem<*>>().apply {
      for (podData in podDatas) {
        add(HomePodChildItem(podData))
      }
    })
    notifyItemChanged(position)
    notifyItemRangeInserted(position + 1, podDatas.size)
    notifyItemRangeChanged(position + podDatas.size, items.size - 1)
  }

  override fun onGroupCollapse(
    position: Int,
    podDatas: List<HomePodChildItemData>
  ) {
    for (podData in podDatas) {
      items.removeAt(position + 1)
    }
    notifyItemChanged(position)
    notifyItemRangeRemoved(position + 1, podDatas.size)
    notifyItemRangeChanged(position + 1, items.size - 1)
  }

  /**
   * Reset all data, remove all errors/transactions
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(HomePodHeaderItem(HomePodHeaderItemData()), Update))
      add(Pair(HomePodProgressItem(), AddUpdate))
      items.filter { it.type == Parent || it.type == Warning || it.type == Timeout || it.type == Search }
          .map { Pair(it, Remove) }
          .let {
            addAll(it)
          }
    }
        .let {
          operation(it)
        }
  }

  override fun enableFilter() {
    super.enableFilter()
    isFiltering = true
  }

}