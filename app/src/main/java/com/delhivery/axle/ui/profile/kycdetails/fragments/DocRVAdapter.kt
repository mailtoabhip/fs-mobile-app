package com.delhivery.axle.ui.profile.kycdetails.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.gst.GstDetailData
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove

/**
 * RV adapter for [DocActivity]
 */
class DocRVAdapter(private val _interface: DocRVAdapterInterface) :
        BaseDataRVAdapter<BaseDocRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
                _interface
        ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
          inflater: LayoutInflater,
          parent: ViewGroup,
          viewType: Int
  ) = when (DocRVAdapterItemType.byTypeId(viewType)) {
    DocRVAdapterItemType.DocItem -> ViewProfileKycDocumentItemBinding.inflate(inflater, parent, false)
    DocRVAdapterItemType.Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    DocRVAdapterItemType.Progress -> ViewGstProgressItemBinding.inflate(inflater, parent, false)
    DocRVAdapterItemType.Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    else -> ViewGstRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewProfileKycDocumentItemBinding -> DocDataItemVH(binding)
    is ViewGstProgressItemBinding -> DocProgressItemVH(binding)
    is ViewWarningItemBinding -> DocWarningItemVH(binding)
    is ViewTimeOutItemBinding -> DocTimeOutItemVH(binding)
    else -> DocDataItemVH(binding as ViewProfileKycDocumentItemBinding)
  }

  override fun bindVH(
          holder: BaseViewHolder<*>,
          item: BaseDocRVAdapterItem<*>
  ) {
    when (holder) {
      is DocDataItemVH -> holder.bind(item as DocDataItem, _interface)
      is DocWarningItemVH -> holder.bind(item as DocWarningItem, _interface)
      is DocTimeOutItemVH -> holder.bind(item as GstTimeoutItem, _interface)
    }
  }


  /**
   * Reset to empty state
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseDocRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(DocProgressItem(), AddUpdate))
      items.filter { it.type == DocRVAdapterItemType.DocItem || it.type == DocRVAdapterItemType.Warning || it.type == DocRVAdapterItemType.Timeout}
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