package com.delhivery.axle.ui.searchload.fragments.searchresults

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.CardCommonTripsBidsBinding
import com.delhivery.axle.databinding.LoadDelhiveryIntercityBinding
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.axle.databinding.ViewHomeBidsSearchSpinnerItemBinding
import com.delhivery.axle.databinding.ViewHomeContractsProgressItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsRequestItemVH
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.ContractProgress
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Contracts
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.LoadProgress
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Request
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.SearchSpinner
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Warning

/**
 * RV adapter for [SearchLoadsFragment]
 */
class SearchLoadsRVAdapter(private val _interface: SearchLoadsRVAdapterInterface) :
    BaseDataRVAdapter<BaseSearchLoadsRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = items[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (SearchResultsRVAdapterItemType.byTypeId(viewType)) {
    Request -> LoadDelhiveryIntercityBinding.inflate(inflater, parent, false)
    Contracts -> CardCommonTripsBidsBinding.inflate(inflater,parent,false)
    SearchSpinner -> ViewHomeBidsSearchSpinnerItemBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    ContractProgress -> ViewHomeContractsProgressItemBinding.inflate(inflater,parent,false)
    LoadProgress -> ViewHomeBidsProgressItemBinding.inflate(inflater,parent,false)
    else -> ViewHomeBidsRequestItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is LoadDelhiveryIntercityBinding -> SearchLoadsRequestItemVH(binding)
    is CardCommonTripsBidsBinding -> SearchContractsRequestItemVH(binding)
    is ViewHomeBidsSearchSpinnerItemBinding -> SearchLoadsSearchSpinnerItemVH(binding)
    is ViewWarningItemBinding -> SearchLoadsWarningItemVH(binding)
    is ViewHomeContractsProgressItemBinding -> SearchContractsProgressItemVH(binding)
    is ViewHomeBidsProgressItemBinding -> SearchLoadsProgressItemVH(binding)
    else -> SearchLoadsRequestItemVH(binding as LoadDelhiveryIntercityBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseSearchLoadsRVAdapterItem<*>
  ) {
    when (holder) {
      is SearchLoadsRequestItemVH -> holder.bind(item as SearchLoadsRequestItem, _interface)
      is SearchContractsRequestItemVH -> holder.bind(item as SearchContractsRequestItem, _interface)
      is SearchLoadsSearchSpinnerItemVH -> holder.bind(
          item as SearchLoadsSearchSpinnerItem, _interface
      )
      is SearchContractsProgressItemVH -> holder.bind(item as SearchContractsProgressItem, _interface)
      is SearchLoadsWarningItemVH -> holder.bind(item as SearchLoadsWarningItem, _interface)

    }
  }

  /**
   * Reset all data, remove all errors/transactions
   */
  fun resetStaticData(requestType: String) {
    mutableListOf<Pair<BaseSearchLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      if(requestType=="load")
        add(Pair(SearchLoadsProgressItem(), AddUpdate))
      else
        add(Pair(SearchContractsProgressItem(), AddUpdate))
      items.filter {
        it.type == Contracts || it.type == Warning || it.type==Request || it.type== LoadProgress|| it.type==ContractProgress
      }
        .map { Pair(it, Remove) }
        .let {
          addAll(it)
        }

    }
      .let {
        operation(it)
      }
  }

  override fun onViewRecycled(holder: BaseViewHolder<*>) {
    super.onViewRecycled(holder)
//    if(holder is SearchContractsRequestItemVH)
//      holder.stopCounter()
  }
}