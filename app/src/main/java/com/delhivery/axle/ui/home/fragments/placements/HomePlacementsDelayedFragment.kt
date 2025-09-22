package com.delhivery.axle.ui.home.fragments.placements

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentHomePlacementsDelayedBinding
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.data.home.placements.HomePlacementRequested_ViewDetails
import com.delhivery.axle.data.home.placements.HomePlacementsCallDriver
import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.delhivery.axle.data.home.placements.HomePlacementsTimeoutItemAction
import com.delhivery.axle.ui.placementdetails.placementDetailsIntent
import com.delhivery.axle.ui.home.fragments.placements.HomePlacementsRVAdapterInterface
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_DELAYED_TAB
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_DEMAND_CARD_CLICKED
import com.delhivery.axle.utils.PROPERTY_DEMAND_TYPE
import com.delhivery.axle.utils.PROPERTY_EXPECTED_TIME
import com.delhivery.axle.utils.PROPERTY_MISSING_FLAG
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

/**
 * Delayed placements fragment
 */
class HomePlacementsDelayedFragment : HomeBaseFragment<FragmentHomePlacementsDelayedBinding, HomePlacementsViewModel>(),
    HomePlacementsRVAdapterInterface {

    @Inject lateinit var userPrefs: UserPrefs
    private var fragmentSetupTrace: Trace? = null
    private var isFirstResume = true

    companion object {
        /* singleton instance */
        val _instance: HomePlacementsDelayedFragment by lazy { HomePlacementsDelayedFragment() }
    }

    override fun getViewModelClass() = HomePlacementsViewModel::class.java

    override fun layoutId() = R.layout.fragment_home_placements_delayed

    /* RV adapter */
    private val adapter: HomePlacementsRVAdapter by lazy {
        HomePlacementsRVAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomePlacementsDelayedFragment_SetupTime")
        fragmentSetupTrace?.start()

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            refreshData()
        }

        /* setup recycler view */
        binding.rvLoads.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomePlacementsDelayedFragment.adapter
        }
        binding.rvLoads.itemAnimator = null

        viewModel.dataLoadingLiveData.reobserve(viewLifecycleOwner, Observer {
            isLoadingData = it ?: false
        })

        viewModel.userLoadsData.reobserve(viewLifecycleOwner, Observer {
            it?.let { _items -> adapter.operation(_items) }
        })

        viewModel.userLoadsDataFetch.reobserve(viewLifecycleOwner, Observer {
            it?.let { _items -> adapter.operation(_items) }
        })

        // Track analytics for delayed tab
        analyticsUtil.moEngageTrackEvent(
            EVENT_HOME_PLACEMENT_DELAYED_TAB,
            mutableListOf(
                PROPERTY_USER_ID,
                PROPERTY_PHONE_NO
            ),
            mutableListOf(
                userPrefs.userId(),
                userPrefs.phoneNumber ?: ""
            )
        )

        refreshData()
    }

    override fun onResume() {
        super.onResume()
        if (fragmentSetupTrace != null && isFirstResume) {
            fragmentSetupTrace?.stop()
            isFirstResume = false
        }
    }

    private fun refreshData() {
        adapter.resetStaticData()
        viewModel.fetchPlacementLoads(PlacementTypes.Delayed.name)
    }

    override fun handleAction(actionId: String, item: BaseHomePlacementsRVAdapterItem<*>) {
        when (actionId) {
            HomePlacementRequested_ViewDetails -> {
                val data = item.data as HomePlacementsItemData
                val missingDetails = data.vehicleNumber == null || data.driverName == null || data.driverPhone == null
                analyticsUtil.moEngageTrackEvent(
                    EVENT_HOME_PLACEMENT_DEMAND_CARD_CLICKED,
                    mutableListOf(
                        PROPERTY_USER_ID,
                        PROPERTY_PHONE_NO,
                        PROPERTY_DEMAND_TYPE,
                        PROPERTY_EXPECTED_TIME,
                        PROPERTY_MISSING_FLAG
                    ),
                    mutableListOf(
                        userPrefs.userId(),
                        userPrefs.phoneNumber ?: "",
                        data.loadType ?: "",
                        data.reportingTime ?: "",
                        missingDetails.toString()
                    )
                )
                context?.let {
                    userPrefs.setPreviousScreen(this.javaClass.name)
                    startActivity(placementDetailsIntent(data, it))
                }
            }
            HomePlacementsCallDriver ->{
                val data = item.data as HomePlacementsItemData
                Log.i("callDriver", data.driverPhone?:"")
                callDriver(data.driverPhone?:"")

            }
            HomePlacementsTimeoutItemAction -> {
                refreshData()
            }
        }
    }

    private fun callDriver(phoneNumber:String?){
        compositeDisposable += requestPermission(Manifest.permission.CALL_PHONE)
            .onBackground()
            .subscribe { granted, error ->
                if (error == null && granted) {
                    when (phoneNumber?.let { it1 ->
                        contactUtils.callDriver(
                            it1
                        )
                    }) {
                        false -> {
                            uiUtils.showSnackbar("Unable to place call")
                        }
                        else -> {
                        }
                    }
                } else {
                    uiUtils.showSnackbar(getString(R.string.msg_call_permission))
                }
            }
    }
    override fun handleAction(
        actionId: String,
        item: BaseHomePlacementsRVAdapterItem<*>,
        position: Int
    ) {
        // Not implemented for this fragment
    }
} 