package com.dfd.delfin.ui.selectroute.fragments.detail

import androidx.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dfd.delfin.data.StateModel
import com.dfd.delfin.databinding.ViewSelectRouteDestinationItemBinding
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter

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