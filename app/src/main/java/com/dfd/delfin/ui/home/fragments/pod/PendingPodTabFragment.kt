package com.dfd.delfin.ui.home.fragments.pod

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dfd.delfin.R
import com.dfd.delfin.data.home.pod.HomePodHeaderAction_Dispactched
import com.dfd.delfin.data.home.pod.HomePodHeaderAction_Epod
import com.dfd.delfin.data.home.pod.HomePodHeaderAction_Physical
import com.dfd.delfin.data.home.pod.HomePodSearchAction_Search
import com.dfd.delfin.data.home.pod.HomePodWarningAction_NoTrips
import com.dfd.delfin.data.home.pod.HomePodWarningAction_TimeOut
import com.dfd.delfin.data.home.trips.HomeTripsItemData
import com.dfd.delfin.data.home.trips.HomeTripsRequestAction_UploadEpod
import com.dfd.delfin.data.home.trips.HomeTripsRequestAction_UploadTracking
import com.dfd.delfin.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.dfd.delfin.data.home.trips.HomeTripsRequestAction_ShowLRList
import com.dfd.delfin.data.home.trips.PODStatus
import com.dfd.delfin.databinding.FragmentPendingPodTabBinding
import com.dfd.delfin.ui.home.fragments.pod.HomePodRVAdapterItemType.Pod
import com.dfd.delfin.data.home.trips.TripStatus.EPodUploaded
import com.dfd.delfin.data.home.trips.TripStatus.TruckUnloaded
import com.dfd.delfin.ui.home.activity.docket.docketUpdateIntent
import com.dfd.delfin.ui.home.fragments.HomeBaseFragment
import com.dfd.delfin.ui.home.fragments.HomeFragmentType
import com.dfd.delfin.ui.home.fragments.NavigateHomeFragmentAction
import com.dfd.delfin.ui.searchtrip.searchIntent
import com.dfd.delfin.ui.tripdetails.tripDetailsIntent
import com.dfd.delfin.ui.tripdetails.uploadImageIntent
import android.app.Activity
import android.content.Intent
import com.dfd.delfin.utils.REQCODE_UPLOAD_DOCKET
import com.dfd.delfin.utils.REQCODE_UPLOAD_POD
import com.dfd.delfin.utils.EVENT_EPOD_LIST_SHOWN
import com.dfd.delfin.utils.EVENT_HPOD_LIST_SHOWN
import com.dfd.delfin.utils.PROPERTY_USER_ID
import com.dfd.delfin.utils.PROPERTY_PAGE_NAME
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * Fragment for Pending POD tab showing EPOD pending and Physical POD pending items
 */
class PendingPodTabFragment : HomeBaseFragment<FragmentPendingPodTabBinding, PendingPodViewModel>(),HomePodRVAdapterInterface {

    private val adapter: HomePodRVAdapter by lazy {
        HomePodRVAdapter(this)
    }

    @Inject lateinit var userPrefs: UserPrefs

    private var selectedPodType: PodType = PodType.EPOD // Default to EPOD
    private var epodCount: Int = 0
    private var hpodCount: Int = 0

    // Track if events have been fired for current data load to avoid duplicates
    private var epodListShownTracked = false
    private var hpodListShownTracked = false

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

        // Observe pod counts and pass to parent
        viewModel.podCountsLiveData.observe(viewLifecycleOwner, Observer { podCounts ->
            podCounts?.let {
                // Update local counts
                epodCount = it.epod_pending_count
                hpodCount = it.hpod_pending_count

                // Update ePOD and hPOD tag text with counts
                updatePodTypeCounts()

                // Get parent fragment's viewModel and update it
                (parentFragment as? HomeNewPodFragment)?.viewModel?.podCountsLiveData?.postValue(it)
            }
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

        // Reset tracking flags when switching pod types
        epodListShownTracked = false
        hpodListShownTracked = false

        // Reload data with selected filter
        refreshData()
    }

    private fun filterAndUpdateItems(items: List<Pair<com.dfd.delfin.ui.home.fragments.pod.BaseHomePodRVAdapterItem<*>, com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType>>) {
        val filteredItems = items.filter { pair ->
            val item = pair.first
            if (item.type == Pod) {
                val trip = (item as com.dfd.delfin.ui.home.fragments.pod.HomePodTripItem).data
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

        // Count POD items (excluding header, search, progress, warning items)
        val podItemsCount = filteredItems.count { it.first.type == Pod }

        // Track analytics events only when list is not empty
        if (podItemsCount > 0) {
            when (selectedPodType) {
                PodType.EPOD -> {
                    // Track epod_list_shown only once per data load
                    if (!epodListShownTracked) {
                        analyticsUtil.moEngageTrackEvent(
                            EVENT_EPOD_LIST_SHOWN,
                            mutableListOf(PROPERTY_USER_ID, PROPERTY_PAGE_NAME),
                            mutableListOf(userPrefs.userId(), "my_pods")
                        )
                        epodListShownTracked = true
                    }
                }
                PodType.HPOD -> {
                    // Track hpod_list_shown only once per data load
                    if (!hpodListShownTracked) {
                        analyticsUtil.moEngageTrackEvent(
                            EVENT_HPOD_LIST_SHOWN,
                            mutableListOf(PROPERTY_USER_ID, PROPERTY_PAGE_NAME),
                            mutableListOf(userPrefs.userId(), "my_pods")
                        )
                        hpodListShownTracked = true
                    }
                }
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

        // Reset tracking flags when refreshing data
        epodListShownTracked = false
        hpodListShownTracked = false

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
        // Update tag text with counts from API
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
                                // Pass source based on which section user is in
                                val source = if (selectedPodType == PodType.EPOD) "epod" else "hpod"
                                startActivityForResult(
                                    uploadImageIntent(
                                        it,
                                        data.transactionId,
                                        data.reachedTime!!,
                                        data.unloadingTime!!,
                                        data.podAction(),
                                        data.intermittentPayableAmount,
                                        source
                                    ), REQCODE_UPLOAD_POD
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
                            // In HPOD section, source is "hpod"
                            startActivityForResult(
                                docketUpdateIntent(context = it, trip = data, podStatus = data.podAction(), source = "hpod"), REQCODE_UPLOAD_DOCKET
                            )
                        }
                    } else {
                        context?.let {
                            // In EPOD section, source is "epod"
                            startActivityForResult(
                                uploadImageIntent(
                                    it,
                                    data.transactionId,
                                    data.reachedTime!!,
                                    data.unloadingTime!!,
                                    podStatus = data.podAction(),
                                    intermittentPayableAmount = data.intermittentPayableAmount,
                                    source = "epod"
                                ), REQCODE_UPLOAD_POD
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
        val binding = com.dfd.delfin.databinding.DialogLrListBinding.inflate(layoutInflater)

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
