package com.dfd.delfin.ui.home.fragments.pod

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.data.home.pod.HomePodHeaderItemData
import com.dfd.delfin.databinding.ViewHomeBidsProgressItemBinding
import com.dfd.delfin.databinding.ViewHomePodsHeaderItemBinding
import com.dfd.delfin.databinding.ViewHomeSearchItemBinding
import com.dfd.delfin.databinding.ViewNewPodItemBinding
import com.dfd.delfin.databinding.ViewTimeOutItemBinding
import com.dfd.delfin.databinding.ViewWarningItemBinding
import com.dfd.delfin.ui.base.BaseViewHolder
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter
import com.dfd.delfin.ui.home.fragments.pod.HomePodRVAdapterItemType.Header
import com.dfd.delfin.ui.home.fragments.pod.HomePodRVAdapterItemType.Progress
import com.dfd.delfin.ui.home.fragments.pod.HomePodRVAdapterItemType.Search
import com.dfd.delfin.ui.home.fragments.pod.HomePodRVAdapterItemType.Timeout
import com.dfd.delfin.ui.home.fragments.pod.HomePodRVAdapterItemType.Warning

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
        (holder.binding as? com.dfd.delfin.databinding.ViewNewPodItemBinding)?.isHPODSection = isHPODSection
      }
      is HomePodWarningItemVH -> holder.bind(item as HomePodWarningItem, _interface)
      is HomePodTimeOutItemVH -> holder.bind(item as HomePodTimeoutItem, _interface)
      is HomePodSearchItemVH -> holder.bind(item as HomePodSearchItem, _interface)
    }
  }

  /**
   * Reset all data and show shimmer for initial/refresh loading.
   *
   * Uses setItems() for an atomic replacement so the shimmer progress item
   * always lands at position 1 (right after the header) with a single
   * notifyDataSetChanged(), avoiding index-drift from piecemeal removes.
   */
  fun resetStaticData() {
    setItems(listOf(HomePodHeaderItem(HomePodHeaderItemData()), HomePodProgressItem()))
  }
}