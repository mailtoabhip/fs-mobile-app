package com.delhivery.orion.ui.selectroute.fragments.detail

import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.orion.data.StateModel
import com.delhivery.orion.databinding.ViewSelectRouteDestinationItemBinding
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * Recycler view adapter for Destination States
 *
 **
 */
class DestinationsRVAdapter(private val _clickListener: ItemClickListener<StateModel>) :
    BaseDataRVAdapter<StateModel, ViewDataBinding,
        BaseDestinationsRVAdapterViewHolder<*, *>>(
        _clickListener
    ) {

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ): ViewDataBinding =
    ViewSelectRouteDestinationItemBinding.inflate(inflater, parent, false)

  override fun createVH(binding: ViewDataBinding): BaseDestinationsRVAdapterViewHolder<*, *> {
    return DestinationsItemVH(binding as ViewSelectRouteDestinationItemBinding)
  }

  override fun bindVH(
    holder: BaseDestinationsRVAdapterViewHolder<*, *>,
    item: StateModel
  ) {
    (holder as DestinationsItemVH).bind(item, _clickListener)
  }

}