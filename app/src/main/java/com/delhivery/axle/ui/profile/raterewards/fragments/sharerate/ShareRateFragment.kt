package com.delhivery.axle.ui.profile.raterewards.fragments.sharerate

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.data.sharerates.ShareRateRoutesItemData
import com.delhivery.axle.data.sharerates.ShareRatesItemDataAction_ViewDetails
import com.delhivery.axle.data.sharerates.ShareRatesTimeOutAction
import com.delhivery.axle.data.yourrewards.YourRewardsItemDataAction_ViewDetails
import com.delhivery.axle.databinding.FragmentShareRateBinding
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsBaseFragment
import com.delhivery.axle.utils.NavigationUtils
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class ShareRateFragment: ShareRateGetRewardsBaseFragment<FragmentShareRateBinding, ShareRateFragmentViewModel>(),ShareRateAdapterInterface {


  companion object {
    /* singleton instance */
    val _instance: ShareRateFragment by lazy { ShareRateFragment() }
  }

  override fun getViewModelClass() = ShareRateFragmentViewModel::class.java

  override fun layoutId() = R.layout.fragment_share_rate

  private val adapter: ShareRateRVAdapter by lazy {
    ShareRateRVAdapter(this)
  }


  @Inject
  lateinit var userPrefs: UserPrefs

  @Inject
  lateinit var navigationUtils:NavigationUtils

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.shareRv.apply {
      layoutManager = LinearLayoutManager(context)
     adapter = this@ShareRateFragment.adapter
    }
    binding.refreshLayout.setOnRefreshListener { refreshData()}
    viewModel.userOfferRoutesData.reobserve(viewLifecycleOwner,
      Observer { it?.let { _items -> adapter.operation(_items) }})

    viewModel.dataLoadingLiveData.reobserve(viewLifecycleOwner, Observer {
      binding.refreshLayout.isRefreshing = false
    })

    refreshData()
  }

  override fun refreshData() {
    adapter.resetStaticData()
   /* viewModel.fetchDatabaseOffers().observe(viewLifecycleOwner, Observer {
      if (!it.isNullOrEmpty()) {
        adapter.resetStaticData()
        viewModel.getFrequentLanes(it)
      }
    })*/
  }

  override fun handleAction(actionId: String, item: BaseShareRateRVAdapterItem<*>) {
    when (actionId) {
      ShareRatesItemDataAction_ViewDetails -> {
       val data = item.data as ShareRateRoutesItemData
        val bundle = Bundle()
        bundle.putString("originname", data?.originCity)
        bundle.putString("destname", data?.destinationCity)
        bundle.putString("occ", data?.originCityCode)
        bundle.putString("dcc", data?.destinationCityCode)
        bundle.putString("truckNumber", null)
        bundle.putString("truckType", data.truckDisplayName)
        bundle.putString("truckCapacity",null)
        bundle.putString("itemTD",  data.truckDisplayName)
        bundle.putString("offerTD",  data.truckDisplayName)
      //  navigationUtils.navigate(ShareRateActivity::class.java, false, bundle)
      }
      ShareRatesTimeOutAction -> {
        refreshData()
      }
    }
  }

}