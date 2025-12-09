package com.delhivery.axle.ui.home.fragments.pod

import android.Manifest
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.data.home.pod.HomePodHeaderAction_Dispactched
import com.delhivery.axle.data.home.pod.HomePodHeaderAction_Epod
import com.delhivery.axle.data.home.pod.HomePodHeaderAction_Physical
import com.delhivery.axle.data.home.pod.HomePodSearchAction_Search
import com.delhivery.axle.data.home.pod.HomePodWarningAction_NoTrips
import com.delhivery.axle.data.home.pod.HomePodWarningAction_TimeOut
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadEpod
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadTracking
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.data.home.trips.PODStatus
import com.delhivery.axle.databinding.FragmentSubmittedPodTabBinding
import com.delhivery.axle.data.home.trips.TripStatus.EPodUploaded
import com.delhivery.axle.data.home.trips.TripStatus.TruckUnloaded
import com.delhivery.axle.ui.home.activity.docket.docketUpdateIntent
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Pod
import com.delhivery.axle.ui.home.fragments.pod.PendingPodTabFragment.PodType
import com.delhivery.axle.ui.searchtrip.searchIntent
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.ui.tripdetails.uploadImageIntent
import com.delhivery.axle.utils.REQCODE_UPLOAD_DOCKET
import com.delhivery.axle.utils.REQCODE_UPLOAD_POD
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign

/**
 * Fragment for Submitted tab showing Dispatched items
 */
class SubmittedPodTabFragment : HomeBaseFragment<FragmentSubmittedPodTabBinding, SubmittedPodViewModel>(),HomePodRVAdapterInterface {

    private val adapter: HomePodRVAdapter by lazy { 
        HomePodRVAdapter(this)
    }

    companion object {
        fun newInstance() = SubmittedPodTabFragment()
    }

    override fun getViewModelClass() = SubmittedPodViewModel::class.java

    override fun layoutId() = R.layout.fragment_submitted_pod_tab

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvSubmittedPod.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SubmittedPodTabFragment.adapter
        }

        // Setup pull-to-refresh
        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            refreshData()
        }

        // Observe loading state to enable/disable refresh layout
        viewModel.dataLoadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                binding.refreshLayout.isEnabled = !it
                isLoadingData = it
            }
        })

        binding.editStickySearch.setOnClickListener {
            handleAction(HomePodSearchAction_Search, 1, HomePodSearchItem())
        }
        // Observe data from own view model
        viewModel.userPodsData.observe(viewLifecycleOwner, Observer { items ->
            items?.let { _items ->
                filterAndUpdateItems(_items)
            }
            // Stop refreshing when data is received
            binding.refreshLayout.isRefreshing = false
        })

        // Initial data load
        refreshData()
    }

    private fun filterAndUpdateItems(items: List<Pair<BaseHomePodRVAdapterItem<*>, com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType>>) {
        val filteredItems = items.filter { pair ->
            val item = pair.first
            if (item.type == HomePodRVAdapterItemType.Pod) {
                val trip = (item as HomePodTripItem).data
                // Show dispatched items (EPodUploaded status with POD tracking)
                trip.tripStatus == EPodUploaded.statusKey && trip.hasPODTracking()
            } else {
                true // Keep non-pod items
            }
        }
        adapter.operation(filteredItems)
    }

    private fun refreshData() {
        // Cancel any ongoing requests before starting a new one
        viewModel.cancelOngoingRequests()
        
        adapter.resetStaticData()
        viewModel.dispatch = true
        // Reset pagination when switching pod types
        viewModel.offset = 0
        viewModel.hasMoreData = true
        // SubmittedPodViewModel already has dispatch = true by default
        viewModel.fetchTrips()
    }

    override fun handleAction(
        actionId: String,
        position: Int,
        item: BaseHomePodRVAdapterItem<*>
    ) {
        when (actionId) {
            HomePodHeaderAction_Epod -> {
                if (!isLoadingData) {
                    viewModel.status = TruckUnloaded
                    viewModel.dispatch = false
                    refreshData()
                }
            }

            HomePodHeaderAction_Physical -> {
                if (!isLoadingData) {
                    viewModel.status = EPodUploaded
                    viewModel.dispatch = false
                    refreshData()
                }
            }

            HomePodHeaderAction_Dispactched -> {
                if (!isLoadingData) {
                    viewModel.status = EPodUploaded
                    viewModel.dispatch = true
                    refreshData()
                }
            }

            HomePodSearchAction_Search -> {
               // userPrefs.setPreviousScreen(this.javaClass.name)
                context?.let { startActivity(searchIntent(it)) }
            }

            HomePodWarningAction_NoTrips -> {
                action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
            }

            HomePodWarningAction_TimeOut -> {
                refreshData()
            }

            HomeTripsRequestAction_UploadEpod -> {
                val data = item.data as HomeTripsItemData
                viewModel.transactionId = data.transactionId
                viewModel.podUrl = data.podUrl ?: ""
                context?.let {
                    startActivityForResult(
                        docketUpdateIntent(context = it, trip = data), REQCODE_UPLOAD_DOCKET
                    )
                }

            }

            HomeTripsRequestAction_UploadTracking -> {
                val data = item.data as HomeTripsItemData
                 context?.let {
                        startActivityForResult(
                            docketUpdateIntent(context = it, trip = data), REQCODE_UPLOAD_DOCKET
                        )
                    }
            }

            HomeTripsRequestAction_ViewDetails -> {
                context?.let {
                    val data = item.data as HomeTripsItemData
                    startActivity(tripDetailsIntent(data.key(), it))
                }
            }
        }
    }
}
