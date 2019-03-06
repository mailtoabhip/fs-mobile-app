package com.delhivery.orion.ui.home.fragments.bids

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.databinding.FragmentHomeBidsBinding
import com.delhivery.orion.ui.base.BaseFragment
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

class HomeBidsFragment : BaseFragment<FragmentHomeBidsBinding, HomeBidsViewModel>(),
    ItemClickListener<BaseHomeBidsRVAdapterItem<*>> {

  companion object {
    /* singleton instance */
    val _instance: HomeBidsFragment by lazy { HomeBidsFragment() }
  }

  override fun getViewModelClass() = HomeBidsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_bids

  /* RV adapter */
  private val adapter: HomeBidsRVAdapter by lazy {
    HomeBidsRVAdapter(this)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    /* setup recycler view */
    binding.rvBids.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@HomeBidsFragment.adapter
    }

    adapter.setItems(getDummyData())
  }

  private fun getDummyData() = mutableListOf<BaseHomeBidsRVAdapterItem<*>>().apply {
    add(0, HomeBidsHeaderItem())
    add(1, HomeBidsSearchItem())
  }

  override fun onItemClicked(item: BaseHomeBidsRVAdapterItem<*>) {

  }
}