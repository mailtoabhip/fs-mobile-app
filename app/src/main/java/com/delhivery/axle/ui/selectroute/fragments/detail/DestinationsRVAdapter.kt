package com.delhivery.axle.ui.selectroute.fragments.detail

import androidx.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.delhivery.axle.data.StateModel
import com.delhivery.axle.databinding.ViewSelectRouteDestinationItemBinding
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter

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