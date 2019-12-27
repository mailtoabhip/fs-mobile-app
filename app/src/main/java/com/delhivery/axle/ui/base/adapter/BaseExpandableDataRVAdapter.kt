package com.delhivery.axle.ui.base.adapter

import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.pod.HomePodChildItemData
import com.delhivery.axle.data.home.pod.HomePodParentItemData
import com.delhivery.axle.ui.base.BaseViewHolder

abstract class BaseExpandableDataRVAdapter<
    D : BaseKeyTypeModel<out Any>,
    B : ViewDataBinding,
    VH : BaseViewHolder<*>>(clickListener: ItemClickListener<D>) : BaseFilterableDataRVAdapter<D, B, VH>(
    clickListener
) {

  fun toggle(
    position: Int,
    data: HomePodParentItemData
  ) {
    if (data.expanded) {
      data.expanded = false
      onGroupCollapse(position, data.podDatas)
    } else {
      data.expanded = true
      onGroupExpand(position, data.podDatas)
    }
  }

  abstract fun onGroupExpand(
    position: Int,
    podDatas: List<HomePodChildItemData>
  )

  abstract fun onGroupCollapse(
    position: Int,
    podDatas: List<HomePodChildItemData>
  )
}