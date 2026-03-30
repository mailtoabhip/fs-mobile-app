package com.delhivery.axle.ui.home.fragments.pod

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.ui.common.UiEvent
import com.delhivery.axle.ui.common.UiState
import com.delhivery.axle.ui.common.UserIntent
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import kotlinx.coroutines.launch
import com.delhivery.axle.data.home.pod.HomePodHeaderAction_Dispactched
import com.delhivery.axle.data.home.pod.HomePodSearchAction_Search
import com.delhivery.axle.data.home.pod.HomePodWarningAction_NoTrips
import com.delhivery.axle.data.home.pod.HomePodWarningAction_TimeOut
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadEpod
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadTracking
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ShowLRList
import com.delhivery.axle.databinding.FragmentSubmittedPodTabBinding
import com.delhivery.axle.ui.home.activity.docket.docketUpdateIntent
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.searchtrip.searchIntent
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.ui.tripdetails.uploadImageIntent
import android.app.Activity
import android.content.Intent
import com.delhivery.axle.utils.REQCODE_UPLOAD_DOCKET
import com.delhivery.axle.utils.REQCODE_UPLOAD_POD
import javax.inject.Inject

/**
 * Fragment for Submitted tab showing Dispatched items.
 * 
 * This fragment uses Flow-based MVI architecture with SubmittedPodViewModelFlow.
 * It follows the pure intent-based approach where all user actions are sent as intents.
 */
class SubmittedPodTabFragment : HomeBaseFragment<FragmentSubmittedPodTabBinding, SubmittedPodViewModel>(),HomePodRVAdapterInterface {

    private val adapter: HomePodRVAdapter by lazy { 
        HomePodRVAdapter(this)
    }

    @Inject lateinit var viewModelFlow: SubmittedPodViewModelFlow

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

        // Collect UI state using lifecycle-aware coroutines
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelFlow.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
        
        // Collect one-time events
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelFlow.events.collect { event ->
                    handleEvent(event)
                }
            }
        }
        
        // Collect pod counts
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelFlow.podCounts.collect { podCounts ->
                    podCounts?.let {
                        // Get parent fragment's viewModel and update it
                        (parentFragment as? HomeNewPodFragment)?.viewModel?.podCountsLiveData?.postValue(it)
                    }
                }
            }
        }

        // Setup pull-to-refresh
        binding.refreshLayout.setOnRefreshListener {
            // Send Refresh intent to ViewModel (pure MVI approach)
            viewModelFlow.processIntent(UserIntent.Refresh)
        }

        // Observe loading state to enable/disable refresh layout (backward compatibility)
        viewModel.dataLoadingLiveData.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                binding.refreshLayout.isEnabled = !it
                isLoadingData = it
            }
        })

        binding.editStickySearch.setOnClickListener {
            handleAction(HomePodSearchAction_Search, 1, HomePodSearchItem())
        }
        
        // Observe data from old view model (backward compatibility)
        viewModel.userPodsData.observe(viewLifecycleOwner, Observer { items ->
            items?.let { _items ->
                // This is for backward compatibility only
            }
            binding.refreshLayout.isRefreshing = false
        })
        
        // Observe pod counts from old ViewModel (backward compatibility)
        viewModel.podCountsLiveData.observe(viewLifecycleOwner, Observer { podCounts ->
            podCounts?.let {
                // Handled by Flow ViewModel now
            }
        })

        // Initial data load - send Search intent
        viewModelFlow.processIntent(UserIntent.Refresh)
    }

    /**
     * Renders UI based on current UiState.
     * This function handles all possible states exhaustively.
     */
    private fun renderState(state: UiState<List<HomeTripsItemData>>) {
        when (state) {
            is UiState.Idle -> {
                // Initial state
            }
            
            is UiState.Loading -> {
                if (state.isRefreshing) {
                    binding.refreshLayout.isRefreshing = true
                } else {
                    isLoadingData = true
                }
            }
            
            is UiState.Success -> {
                binding.refreshLayout.isRefreshing = false
                isLoadingData = false
                
                // Build adapter items
                val items = mutableListOf<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>()
                
                // Add trip items
                for (trip in state.data) {
                    items.add(Pair(HomePodTripItem(trip), DataRVAdapterOperationType.Add))
                }
                
                adapter.operation(items)
                
                // Show/hide load more footer
                if (state.isLoadingMore) {
                    adapter.operation(listOf(
                        Pair(HomePodProgressItem(), DataRVAdapterOperationType.AddUpdate)
                    ))
                }
            }
            
            is UiState.Empty -> {
                binding.refreshLayout.isRefreshing = false
                isLoadingData = false
                
                val items = mutableListOf<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>()
                items.add(Pair(HomePodWarningItem_NoLoads, DataRVAdapterOperationType.AddUpdate))
                
                adapter.operation(items)
            }
            
            is UiState.Error -> {
                binding.refreshLayout.isRefreshing = false
                isLoadingData = false
                
                val items = mutableListOf<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>()
                items.add(Pair(HomePodWarningItem_TimeOut, DataRVAdapterOperationType.AddUpdate))
                
                adapter.operation(items)
                
                uiUtils.showSnackbar(state.message)
            }
        }
    }

    /**
     * Handles one-time UI events.
     */
    private fun handleEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowToast -> {
                uiUtils.showToast(event.message)
            }
            
            is UiEvent.ShowSnackbar -> {
                if (event.action != null && event.onActionClick != null) {
                    uiUtils.showSnackbarWithAction(
                        message = event.message,
                        actionText = event.action,
                        action = { event.onActionClick.invoke() }
                    )
                } else {
                    uiUtils.showSnackbar(event.message)
                }
            }
            
            is UiEvent.Navigate -> {
                // Handle navigation if needed
            }
        }
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
                    // Already showing dispatched items, just refresh
                    viewModelFlow.processIntent(UserIntent.Refresh)
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
                // Send Retry intent to ViewModel
                viewModelFlow.processIntent(UserIntent.Retry)
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
                    // Refresh data when POD upload is successful - send Refresh intent
                    viewModelFlow.processIntent(UserIntent.Refresh)
                }
            }
            REQCODE_UPLOAD_DOCKET -> {
                // Refresh data when docket update is successful - send Refresh intent
                if (resultCode == Activity.RESULT_OK) {
                    viewModelFlow.processIntent(UserIntent.Refresh)
                }
            }
        }
    }
}
