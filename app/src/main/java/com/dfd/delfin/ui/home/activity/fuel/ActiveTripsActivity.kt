package com.dfd.delfin.ui.home.activity.fuel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dfd.delfin.R
import com.dfd.delfin.data.home.trips.HomeTripsItemData
import com.dfd.delfin.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.dfd.delfin.data.transactions.TransactionTimeOutAction
import com.dfd.delfin.data.transactions.TransactionWarningAction_NoTransactions
import com.dfd.delfin.databinding.ActivityActiveTripsBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.home.activity.fuelcard.createFuelCardIntent
import com.dfd.delfin.utils.DatePatterns
import com.dfd.delfin.utils.DateUtils
import com.dfd.delfin.utils.PaginationScrollListener
import com.dfd.delfin.utils.REQCODE_CREATE_FUELCARD
import com.dfd.delfin.utils.WindowInsetsUtils
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

/**
 * Displays active trips and their fuel card mapping
 */
class ActiveTripsActivity : BaseActivity<ActivityActiveTripsBinding, ActiveTripsViewModel>(),
    ActiveTripsRVAdapterInterface {

  override fun getViewModelClass() = ActiveTripsViewModel::class.java

  override fun layoutId() = R.layout.activity_active_trips

  override fun requireConnection() = true

  var isLoadingData = true
  var cardCreated = false
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true

  @Inject lateinit var userPrefs : UserPrefs

  private val adapter: ActiveTripsRVAdapter by lazy {
    ActiveTripsRVAdapter(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("ActiveTripsActivity_SetupTime")
    activitySetupTrace?.start()
    /* validate intent */
    if (intent == null || !intent.hasExtra(ARGS_OPTIN_DATE)) {
      throw IllegalArgumentException("Required data $ARGS_OPTIN_DATE not found")
    }

    viewModel.optinDate = intent?.getStringExtra(ARGS_OPTIN_DATE) ?: ""
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
    title = "Select Active Trip for fuel"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        userPrefs.setPreviousScreen(this.javaClass.name)
        finish()
      }
    })
    viewModel.dataLoadingLiveData.observe(this, Observer {
      isLoadingData = it ?: false
    })

    viewModel.tripsLiveData.observe(this, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })

    binding.refreshLayout.setOnRefreshListener {
      refreshData()
    }

    binding.rvTrips.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@ActiveTripsActivity.adapter
      addOnScrollListener(PaginationInterface())
    }

    refreshData()
  }

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }

  private fun refreshData() {
    binding.refreshLayout.isRefreshing = false
    adapter.resetStaticData()
    viewModel.fetchFuelCards()
  }

  override fun handleAction(
    actionId: String,
    item: BaseActiveTripsRVAdapterItem<*>
  ) {
    when (actionId) {
      HomeTripsRequestAction_ViewDetails -> {
        val data = item.data as? HomeTripsItemData
        if (data != null && DateUtils.parseDate(
                data.arrivalTime ?: "", DatePatterns.OrionDateFormat
            ).after(
                DateUtils.parseDate(
                    DateUtils.formatISODateToUTC(viewModel.optinDate, DatePatterns.OrionDateFormat),
                    DatePatterns.OrionDateFormat
                )
            )
        ) {
          navigationUtils.navigateForActivityResult(
              createFuelCardIntent(this, data, viewModel.getActiveNumbers(data.fuelCard?.mobile)),
              false, REQCODE_CREATE_FUELCARD
          )
        } else {
          uiUtils.showToast("Cant create fuel for this trip")
        }
      }

      TransactionTimeOutAction -> {
        refreshData()
      }

      TransactionWarningAction_NoTransactions -> {
        setResult(Activity.RESULT_FIRST_USER)
        finish()
      }
    }
  }

  override fun finish() {
    if (cardCreated) {
      setResult(Activity.RESULT_OK)
    }
    super.finish()
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQCODE_CREATE_FUELCARD && resultCode == Activity.RESULT_OK) {
      cardCreated = true
      refreshData()
    }
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(10) {
    override fun hasMore() = viewModel.offset < viewModel.total

    override fun isLoading() = isLoadingData
  }
}

/* intent keys */
private const val ARGS_OPTIN_DATE = "args_optin_date"

/**
 * Trips Fuel Credit intent
 */
fun tripsFuelCreditIntent(
  context: Context,
  optinDate: String
) = Intent(context, ActiveTripsActivity::class.java).apply {
  putExtra(ARGS_OPTIN_DATE, optinDate)
}