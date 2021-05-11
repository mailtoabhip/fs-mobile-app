package com.delhivery.axle.ui.base.adapter

import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryDetailItemData
import com.delhivery.axle.data.tripdetail.TripPaymentSummaryItemData
import com.delhivery.axle.ui.base.BaseViewHolder

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 10/5/21
 */

abstract class BaseSummaryExpandableDataRVAdapter<
    D : BaseKeyTypeModel<out Any>,
    B: ViewDataBinding,
    VH: BaseViewHolder<*>>(clickListener: ItemClickListener<D>): BaseSummaryDataRVAdapter<D, B, VH>(clickListener){
  fun toggle(
    position: Int,
    data: TripPaymentSummaryItemData
  ){
    if(data.expanded){
      data.expanded = false
      onGroupCollapse(position,data.itemSummary)
    }else{
      data.expanded = true
      onGroupExpand(position,data.itemSummary)
    }
  }

  abstract fun onGroupExpand(
    position: Int,
    summary: List<TripPaymentSummaryDetailItemData>
  )

  abstract fun onGroupCollapse(
    position: Int,
    summary: List<TripPaymentSummaryDetailItemData>
  )
}