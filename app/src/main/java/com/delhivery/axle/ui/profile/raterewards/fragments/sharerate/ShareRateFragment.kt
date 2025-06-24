package com.delhivery.axle.ui.profile.raterewards.fragments.sharerate

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.data.sharerates.ShareRateRoutesItemData
import com.delhivery.axle.data.sharerates.ShareRatesItemDataAction_ViewDetails
import com.delhivery.axle.data.sharerates.ShareRatesTimeOutAction
import com.delhivery.axle.databinding.FragmentShareRateBinding
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsBaseFragment
import com.delhivery.axle.ui.sharerate.ShareRateActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.util.Calendar
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

  var launch : Boolean =true
   private var fragmentSetupTrace: Trace? = null
   private var isFirstResume = true

  @Inject
  lateinit var userPrefs: UserPrefs

  @Inject
  lateinit var navigationUtils:NavigationUtils

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("ShareRateFragment_SetupTime")
    fragmentSetupTrace?.start()
  }
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.shareRv.apply {
      layoutManager = LinearLayoutManager(context)
     adapter = this@ShareRateFragment.adapter
    }
    binding.refreshLayout.setOnRefreshListener { refreshData()}
    viewModel.userOfferRoutesData.reobserve(viewLifecycleOwner,
      Observer { it?.let { _items -> adapter.operation(_items)
      userPrefs.rateOfferCount=_items.size

        if(launch) {
          val c = Calendar.getInstance()
          val date = c.get(Calendar.DATE).toString()
          analyticsUtil.moEngageTrackEvent(
              EVENT_VIEW_SHARE_RATE_OFFERS,
              mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_NUMBER_OF_OFFERS,
                  PROPERTY_DATE),
              mutableListOf(userPrefs.userId() , userPrefs.phoneNumber!!, userPrefs.rateOfferCount.toString(),date)
          )
          launch=false
        }
      }})

    viewModel.dataLoadingLiveData.reobserve(viewLifecycleOwner, Observer {
      binding.refreshLayout.isRefreshing = false
    })

    refreshData()
  }

    override fun onResume() {
        super.onResume()
        if (fragmentSetupTrace != null && isFirstResume) {
            fragmentSetupTrace?.stop()
            isFirstResume = false
        }
    }

  override fun refreshData() {
    viewModel.count =0
    adapter.resetStaticData()
    viewModel.getFrequentLanes()
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
        bundle.putString("offerid",  data.offerId)
        bundle.putString("amt",  data.amount)


        analyticsUtil.moEngageTrackEvent(
                  EVENT_CLICKED_OFFER,
                  mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_SOURCE, PROPERTY_OFFER_ID),
                  mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy", "profile_screen",  data.offerId?:"")
          )
        navigationUtils.navigate(ShareRateActivity::class.java, false, bundle)
      }
      ShareRatesTimeOutAction -> {
        refreshData()
      }
    }
  }

}