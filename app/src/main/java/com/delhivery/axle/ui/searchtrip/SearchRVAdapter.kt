package com.delhivery.axle.ui.searchtrip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.api.request.SearchRequest
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewPodItemBinding
import com.delhivery.axle.databinding.ViewSearchQueryItemBinding
import com.delhivery.axle.databinding.ViewSearchedQueryItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.searchtrip.SearchRVAdapterItemType.Progress
import com.delhivery.axle.ui.searchtrip.SearchRVAdapterItemType.Search
import com.delhivery.axle.ui.searchtrip.SearchRVAdapterItemType.Searched
import com.delhivery.axle.ui.searchtrip.SearchRVAdapterItemType.Timeout
import com.delhivery.axle.ui.searchtrip.SearchRVAdapterItemType.TripItem
import com.delhivery.axle.ui.searchtrip.SearchRVAdapterItemType.Warning

/**
 * RV adapter for [SearchActivity]
 */
class SearchRVAdapter(private val _interface: SearchRVAdapterInterface) :
    BaseDataRVAdapter<BaseSearchRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (SearchRVAdapterItemType.byTypeId(viewType)) {
    Search -> ViewSearchQueryItemBinding.inflate(inflater, parent, false)
    Searched -> ViewSearchedQueryItemBinding.inflate(inflater, parent, false)
    TripItem -> ViewPodItemBinding.inflate(inflater, parent, false)
    Warning -> ViewWarningItemBinding.inflate(inflater, parent, false)
    Progress -> ViewHomeBidsProgressItemBinding.inflate(inflater, parent, false)
    Timeout -> ViewTimeOutItemBinding.inflate(inflater, parent, false)
    else -> ViewPodItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewSearchQueryItemBinding -> SearchQueryItemVH(binding)
    is ViewSearchedQueryItemBinding -> SearchedQueryItemVH(binding)
    is ViewPodItemBinding -> SearchDataItemVH(binding)
    is ViewWarningItemBinding -> SearchWarningItemVH(binding)
    is ViewTimeOutItemBinding -> SearchTimeOutItemVH(binding)
    is ViewHomeBidsProgressItemBinding -> SearchProgressItemVH(binding)
    else -> SearchDataItemVH(binding as ViewPodItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseSearchRVAdapterItem<*>
  ) {
    when (holder) {
      is SearchQueryItemVH -> holder.bind(item as SearchQueryItem, _interface)
      is SearchedQueryItemVH -> holder.bind(item as SearchedQueryItem, _interface)
      is SearchDataItemVH -> holder.bind(item as SearchDataItem, _interface)
      is SearchWarningItemVH -> holder.bind(item as SearchWarningItem, _interface)
      is SearchTimeOutItemVH -> holder.bind(item as SearchTimeoutItem, _interface)
    }
  }

  /**
   * Reset all data, remove all errors/transactions
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseSearchRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(SearchQueryItem(SearchRequest()), AddUpdate))
      items.filter {
        it.type == TripItem || it.type == Warning || it.type == Timeout || it.type == Searched
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

  fun refresh() {
    mutableListOf<Pair<BaseSearchRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(SearchProgressItem(), AddUpdate))
      items.filter {
        it.type == TripItem || it.type == Warning ||
            it.type == Timeout || it.type == Searched || it.type == Search
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
}