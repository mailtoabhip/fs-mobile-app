package com.delhivery.orion.ui.bids

import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.orion.databinding.ViewHomeBidsRequestItemBinding
import com.delhivery.orion.ui.base.adapter.BaseFilterableDataRVAdapter
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRequestItem
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRequestItemVH

class BidsRVAdapter(clickListener: ItemClickListener<HomeBidsRequestItem>) :
    BaseFilterableDataRVAdapter<HomeBidsRequestItem, ViewHomeBidsRequestItemBinding, HomeBidsRequestItemVH>(
        clickListener
    ) {
  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = ViewHomeBidsRequestItemBinding.inflate(inflater, parent, false)

  override fun createVH(binding: ViewHomeBidsRequestItemBinding) = HomeBidsRequestItemVH(binding)

  override fun bindVH(
    holder: HomeBidsRequestItemVH,
    item: HomeBidsRequestItem
  ) {
    holder.binding.request = item.data
  }

  override fun filterList(query: String) = items.filter { it.data.filter(query) }
}