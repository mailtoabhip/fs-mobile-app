package com.dfd.delfin.ui.searchongoingtrip

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import com.dfd.delfin.R
import com.dfd.delfin.api.repository.UserSearchLimit
import com.dfd.delfin.data.home.trips.HomeTripsItemData
import com.dfd.delfin.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.dfd.delfin.data.search.SearchTimeOutAction
import com.dfd.delfin.data.search.SearchWarningAction_NoResult
import com.dfd.delfin.databinding.ActivitySearchOngoingTripBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.tripdetails.tripDetailsIntent
import com.dfd.delfin.utils.*
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 12/5/21
 */

class SearchOngoingTripActivity : BaseActivity<ActivitySearchOngoingTripBinding,
    SearchOngoingTripViewModel>(), SearchOngoingTripRVAdapterInterface {

  override fun getViewModelClass() = SearchOngoingTripViewModel::class.java

  override fun layoutId() = R.layout.activity_search_ongoing_trip

  override fun requireConnection() = true

  var isLoadingData = true

  @Inject lateinit var userPrefs: UserPrefs
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true

  init {
    hasInlineProgress = true
  }

  private val adapter by lazy { SearchOngoingTripRVAdapter(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("SearchOngoingTripActivity_SetupTime")
    activitySetupTrace?.start()
  }
  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.parentCl)
    }
    title = "Search Ongoing Trips"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
      override fun handleOnBackPressed() {
        userPrefs.setPreviousScreen(this.javaClass.name)
        finish()
      }
    })
    binding.rvOngoingTripSearch.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
      adapter = this@SearchOngoingTripActivity.adapter
      addOnScrollListener(PaginationInterface())
    }

    viewModel.searchLiveData.observe(this, Observer {
      if(viewModel.searchProgress) {
        analyticsUtil.moEngageTrackEvent(
                EVENT_SEARCH_TRIPS,
                mutableListOf(PROPERTY_USER_ID, PROPERTY_ENTERED_VALUE, PROPERTY_RECD_TRIP_IDS),
                mutableListOf(userPrefs.userId(), binding.editQuery.text.toString(), viewModel.tripIdsRecd.joinToString(separator = ","))
        )
        viewModel.searchProgress=false
      }
      adapter.resetStaticData()
      if (it != null) {
        adapter.operation(it)
      }
    })

    binding.editQuery.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) = Unit
      override fun beforeTextChanged(
        s: CharSequence?,
        start: Int,
        count: Int,
        after: Int
      ) = Unit

      override fun onTextChanged(
        s: CharSequence?,
        start: Int,
        before: Int,
        count: Int
      ) {
        if (s != null) {
          try {
            viewModel.searchText = s.trim().toString()
            Log.d("prefix", s.trim().toString())

            if (viewModel.searchText.length in 3..10) {
              refreshData()
            } else {
              adapter.resetStaticData()
            }
          }  catch (e: Exception) {
            binding.editQuery.error = e.message
          }
        }
      }
    })

    if (viewModel.searchText.length in 3..10) {
      refreshData()
    }
  }

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }
  /**
   * refresh data
   */
  fun refreshData() {
    adapter.resetStaticData()
    viewModel.searchTrips(false)
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(
      UserSearchLimit
  ) {
    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }

  override fun handleAction(
    actionId: String,
    item: BaseSearchOngoingTripRVAdapterItem<*>
  ) {
    when (actionId) {
      HomeTripsRequestAction_ViewDetails -> {
        userPrefs.setPreviousScreen(this.javaClass.name)
        val data = item.data as HomeTripsItemData
        startActivity(tripDetailsIntent(data.key(), this, viewModel.tripType.typeText))
      }

       SearchWarningAction_NoResult -> {
        refreshData()
      }

      SearchTimeOutAction -> {
        refreshData()
      }
    }
  }

  /*override fun onBackPressed() {
    super.onBackPressed()
    userPrefs.setPreviousScreen(this.javaClass.name)
  }*/

}

/**
 * Trip details intent
 */
fun searchOngoingTripIntent(
  context: Context
) = Intent(context, SearchOngoingTripActivity::class.java).apply {
}