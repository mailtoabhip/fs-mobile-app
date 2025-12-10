package com.delhivery.axle.ui.home.fragments.pod

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
import com.delhivery.axle.databinding.FragmentPendingPodTabBinding
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Pod
import com.delhivery.axle.data.home.trips.TripStatus.EPodUploaded
import com.delhivery.axle.data.home.trips.TripStatus.TruckUnloaded
import com.delhivery.axle.ui.home.activity.docket.docketUpdateIntent
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.searchtrip.searchIntent
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.ui.tripdetails.uploadImageIntent
import com.delhivery.axle.utils.REQCODE_UPLOAD_DOCKET
import com.delhivery.axle.utils.REQCODE_UPLOAD_POD

/**
 * Fragment for Pending POD tab showing EPOD pending and Physical POD pending items
 */
class PendingPodTabFragment : HomeBaseFragment<FragmentPendingPodTabBinding, PendingPodViewModel>(),HomePodRVAdapterInterface {

    private val adapter: HomePodRVAdapter by lazy { 
        HomePodRVAdapter(this)
    }

    private var selectedPodType: PodType = PodType.EPOD // Default to EPOD
    private var epodCount: Int = 0
    private var hpodCount: Int = 0

    enum class PodType {
        EPOD,
        HPOD
    }

    companion object {
        fun newInstance() = PendingPodTabFragment()
    }

    override fun getViewModelClass() = PendingPodViewModel::class.java

    override fun layoutId() = R.layout.fragment_pending_pod_tab

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPendingPod.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@PendingPodTabFragment.adapter
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
        // Setup ePOD and HPOD tag selection
        setupPodTypeTags()

        // Observe data from own view model
        viewModel.userPodsData.observe(viewLifecycleOwner, Observer { items ->
            items?.let { _items ->
                filterAndUpdateItems(_items)
            }
            // Stop refreshing when data is received
            binding.refreshLayout.isRefreshing = false
        })

        viewModel.tripsCountLiveData.observe(viewLifecycleOwner, Observer { count ->
           // updatePodTypeCounts()
        })

        // Set initial adapter state
        adapter.isHPODSection = (selectedPodType == PodType.HPOD)
        
        // Initial data load
        refreshData()
    }

    private fun setupPodTypeTags() {
        binding.tagEpod.setOnClickListener {
            selectPodType(PodType.EPOD)
        }

        binding.tagHpod.setOnClickListener {
            selectPodType(PodType.HPOD)
        }

        // Set initial selection
        selectPodType(PodType.EPOD)
    }

    private fun selectPodType(type: PodType) {
        selectedPodType = type

        // Update adapter with selected section
        adapter.isHPODSection = (type == PodType.HPOD)
        // Notify adapter to rebind items with new section state
        adapter.notifyDataSetChanged()

        // Update tag UI
        when (type) {
            PodType.EPOD -> {
                binding.tagEpod.setBackgroundResource(R.drawable.bg_pod_tag_selected)
                binding.tagEpod.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDelhiveryRed))
                binding.tagHpod.setBackgroundResource(R.drawable.bg_pod_tag_unselected)
                binding.tagHpod.setTextColor(ContextCompat.getColor(requireContext(), R.color.font_labels))
            }
            PodType.HPOD -> {
                binding.tagHpod.setBackgroundResource(R.drawable.bg_pod_tag_selected)
                binding.tagHpod.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDelhiveryRed))
                binding.tagEpod.setBackgroundResource(R.drawable.bg_pod_tag_unselected)
                binding.tagEpod.setTextColor(ContextCompat.getColor(requireContext(), R.color.font_labels))
            }
        }

        // Reload data with selected filter
        refreshData()
    }

    private fun filterAndUpdateItems(items: List<Pair<com.delhivery.axle.ui.home.fragments.pod.BaseHomePodRVAdapterItem<*>, com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType>>) {
        val filteredItems = items.filter { pair ->
            val item = pair.first
            if (item.type == Pod) {
                val trip = (item as com.delhivery.axle.ui.home.fragments.pod.HomePodTripItem).data
                when (selectedPodType) {
                    PodType.EPOD -> {
                        // Show EPOD pending items (TruckUnloaded status without POD tracking)
                        trip.tripStatus == TruckUnloaded.statusKey && !trip.hasPODTracking()
                    }
                    PodType.HPOD -> {
                        // Show HPOD pending items: both EPOD (TruckUnloaded) and HPOD (EPodUploaded) items without POD tracking
                        !trip.hasPODTracking() && 
                        (trip.tripStatus == EPodUploaded.statusKey || trip.tripStatus == TruckUnloaded.statusKey)
                    }
                }
            } else {
                true // Keep non-pod items (header, search, etc.)
            }
        }
        adapter.operation(filteredItems)
     //   updatePodTypeCounts()
    }

    private fun refreshData() {
        // Cancel any ongoing requests before starting a new one
        viewModel.cancelOngoingRequests()
        
        // Clear adapter before loading new data
        adapter.resetStaticData()
        
        viewModel.status = when (selectedPodType) {
            PodType.EPOD -> TruckUnloaded
            PodType.HPOD -> EPodUploaded
        }
        viewModel.dispatch = false
        // Reset pagination when switching pod types
        viewModel.offset = 0
        viewModel.hasMoreData = true
        viewModel.fetchTrips()
    }

    private fun updatePodTypeCounts() {
        // Count EPOD and HPOD items from adapter
        val items = adapter.itemsList()
        epodCount = items.count { item ->
            if (item.type == Pod) {
                val trip = (item as HomePodTripItem).data
                trip.tripStatus == TruckUnloaded.statusKey && !trip.hasPODTracking()
            } else false
        }
        hpodCount = items.count { item ->
            if (item.type == Pod) {
                val trip = (item as HomePodTripItem).data
                trip.tripStatus == EPodUploaded.statusKey && !trip.hasPODTracking()
            } else false
        }

        binding.tagEpod.text = "ePOD ($epodCount)"
        binding.tagHpod.text = "HPOD ($hpodCount)"
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
                Log.i("HomePodWarningAction","mpml")
                action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
            }

            HomePodWarningAction_TimeOut -> {
                refreshData()
            }

            HomeTripsRequestAction_UploadEpod -> {
                val data = item.data as HomeTripsItemData
                viewModel.transactionId = data.transactionId
                viewModel.podUrl = data.podUrl ?: ""
                    when (data.podAction()) {
                        PODStatus.UPLOAD, PODStatus.REJECT -> {
                            context?.let {
                                startActivityForResult(
                                    uploadImageIntent(it, data.transactionId, data.reachedTime!!, data.unloadingTime!!, data.podAction()), REQCODE_UPLOAD_POD
                                )
                            }
                        }
                        else -> {

                        }
                    }


            }

            HomeTripsRequestAction_UploadTracking -> {
                val data = item.data as HomeTripsItemData

                    if (selectedPodType == PodType.HPOD) {
                          context?.let {
                            startActivityForResult(
                                docketUpdateIntent(context = it, trip = data, podStatus = data.podAction()), REQCODE_UPLOAD_DOCKET
                            )
                        }
                    } else {
                        context?.let {
                            startActivityForResult(
                                uploadImageIntent(it, data.transactionId, data.reachedTime!!, data.unloadingTime!!,  podStatus = data.podAction()), REQCODE_UPLOAD_POD
                            )
                        }
                }
            }

            HomeTripsRequestAction_ViewDetails -> {
                Log.i("HomeTripsRequestAction", "Details")
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
}
