package com.delhivery.axle.ui.home.fragments.pod

import android.Manifest
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
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
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ShowLRList
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
import android.app.Activity
import android.content.Intent
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
                adapter.operation(_items)
            }
            // Stop refreshing when data is received
            binding.refreshLayout.isRefreshing = false
        })

        // Initial data load
        refreshData()
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
                        docketUpdateIntent(context = it, trip = data,  podStatus = data.podAction()), REQCODE_UPLOAD_DOCKET
                    )
                }

            }

            HomeTripsRequestAction_UploadTracking -> {
                val data = item.data as HomeTripsItemData
                 context?.let {
                        startActivityForResult(
                            docketUpdateIntent(context = it, trip = data,  podStatus = data.podAction()), REQCODE_UPLOAD_DOCKET
                        )
                    }
            }

            HomeTripsRequestAction_ViewDetails -> {
                context?.let {
                    val data = item.data as HomeTripsItemData
                    startActivity(tripDetailsIntent(data.key(), it))
                }
            }
            
            HomeTripsRequestAction_ShowLRList -> {
                val data = item.data as HomeTripsItemData
                showLRListDialog(data)
            }
        }
    }
    
    private fun showLRListDialog(trip: HomeTripsItemData) {
        val lrNumbers = trip.getAllLRNumbersList()
        if (lrNumbers.isEmpty()) return
        
        val dialog = android.app.Dialog(requireContext())
        val binding = com.delhivery.axle.databinding.DialogLrListBinding.inflate(layoutInflater)
        
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        
        // Setup RecyclerView
        binding.rvLRList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvLRList.adapter = LRListAdapter(lrNumbers)
        // Add divider between items
        val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(
            requireContext(),
            androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
        )
        binding.rvLRList.addItemDecoration(dividerItemDecoration)
        
        // Close button
        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        // Set dialog window properties with margins
        val displayMetrics = resources.displayMetrics
        val marginDp = 16f
        val marginPx = (marginDp * displayMetrics.density).toInt()
        val width = displayMetrics.widthPixels - (2 * marginPx)
        
        dialog.window?.setLayout(
            width,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        // Center the dialog horizontally
        val layoutParams = dialog.window?.attributes
        layoutParams?.gravity = android.view.Gravity.CENTER
        dialog.window?.attributes = layoutParams
        
        dialog.show()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQCODE_UPLOAD_POD -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Refresh data when POD upload is successful
                    refreshData()
                }
            }
            REQCODE_UPLOAD_DOCKET -> {
                // Refresh data when docket update is successful
                if (resultCode == Activity.RESULT_OK) {
                    refreshData()
                }
            }
        }
    }
}
