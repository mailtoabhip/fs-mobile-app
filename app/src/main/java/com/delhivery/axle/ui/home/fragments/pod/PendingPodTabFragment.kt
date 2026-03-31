package com.delhivery.axle.ui.home.fragments.pod

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import com.delhivery.axle.data.home.trips.PODStatus
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.data.home.pod.HomePodHeaderAction_Dispactched
import com.delhivery.axle.data.home.pod.HomePodHeaderAction_Epod
import com.delhivery.axle.data.home.pod.HomePodHeaderAction_Physical
import com.delhivery.axle.data.home.pod.HomePodSearchAction_Search
import com.delhivery.axle.data.home.pod.HomePodWarningAction_NoTrips
import com.delhivery.axle.data.home.pod.HomePodWarningAction_TimeOut
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ShowLRList
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadEpod
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadTracking
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.databinding.DialogLrListBinding
import com.delhivery.axle.databinding.FragmentPendingPodTabBinding
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.common.UiEvent
import com.delhivery.axle.ui.common.UiState
import com.delhivery.axle.ui.common.UserIntent
import com.delhivery.axle.ui.home.activity.docket.docketUpdateIntent
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsProgressItem
import com.delhivery.axle.ui.searchtrip.searchIntent
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.ui.tripdetails.uploadImageIntent
import com.delhivery.axle.utils.EVENT_EPOD_LIST_SHOWN
import com.delhivery.axle.utils.EVENT_HPOD_LIST_SHOWN
import com.delhivery.axle.utils.PROPERTY_PAGE_NAME
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.REQCODE_UPLOAD_DOCKET
import com.delhivery.axle.utils.REQCODE_UPLOAD_POD
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fragment for the Pending POD tab (EPOD + HPOD).
 *
 * Observation pattern:
 * [observeState] is the single setup call in [onViewCreated]. It collects the ViewModel's
 * [PendingPodViewModelFlow.state] and dispatches to a dedicated handler for every field:
 *
 *   onLoading(isLoading)       — shows/hides the full-screen shimmer
 *   onRefreshing(isRefreshing) — shows/hides the SwipeRefreshLayout spinner
 *   onLoadingMore(isLoadingMore) — shows/hides the pagination footer spinner
 *   onUiState(uiState)         — renders Success / Empty / Error / Idle data states
 *   onPodCounts(podCounts)     — updates EPOD/HPOD badge labels
 *   onPodType(currentPodType)  — updates tab selection UI
 *
 * Each handler has exactly one job. None of them knows about the others.
 */
class PendingPodTabFragment :
    HomeBaseFragment<FragmentPendingPodTabBinding, PendingPodViewModelFlow>(),
    HomePodRVAdapterInterface {

    // ────────────────────────── Dependencies ──────────────────────────

    @Inject lateinit var userPrefs: UserPrefs
    @Inject lateinit var viewModelFlow: PendingPodViewModelFlow

    // ────────────────────────── Adapter ──────────────────────────────

    private val adapter: HomePodRVAdapter by lazy { HomePodRVAdapter(this) }

    // ────────────────────────── Analytics dedup flags ─────────────────

    private var epodListShownTracked = false
    private var hpodListShownTracked = false

    // ────────────────────────── Pod type render guard ─────────────────

    /**
     * Tracks the last pod type we applied to the adapter via notifyDataSetChanged().
     * Initialized to null so the very first emission always triggers a refresh.
     * Without this guard, onPodType() calls notifyDataSetChanged() on EVERY state emission,
     * which recycles the shimmer ViewHolders immediately after setItems() adds them,
     * causing the shimmer to never visually appear.
     */
    private var lastRenderedPodType: PendingPodViewModelFlow.PodType? = null

    // ────────────────────────── Local pod counts ──────────────────────

    private var epodCount: Int = 0
    private var hpodCount: Int = 0

    // ────────────────────────── Fragment boilerplate ──────────────────

    companion object {
        fun newInstance() = PendingPodTabFragment()
    }

    override fun getViewModelClass() = PendingPodViewModelFlow::class.java
    override fun layoutId() = R.layout.fragment_pending_pod_tab

    // ────────────────────────── Lifecycle ────────────────────────────

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setup recyclerview
        setupRecyclerView()
        setupPodTypeTags()
        setupPullToRefresh()
        setupSearchButton()

        // Wire up all state + event collectors.
        observeState()

        // Kick off the first load.
        viewModelFlow.processIntent(UserIntent.InitialLoad)
    }

    // ────────────────────────── Setup helpers ────────────────────────

    private fun setupRecyclerView() {
        binding.rvPendingPod.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@PendingPodTabFragment.adapter
            addOnScrollListener(createPaginationScrollListener())
        }
        adapter.isHPODSection = false // default to EPOD
    }

    /**
     * Creates a scroll listener that triggers pagination when the user scrolls near the bottom.
     * Loads more data when:
     * - Not currently loading (initial load, refresh, or pagination)
     * - User has scrolled to the last visible item
     * - There's more data available (hasMore from UiState.Success)
     */
    private fun createPaginationScrollListener() = object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            
            // Only trigger pagination when scrolling down
            if (dy <= 0) return
            
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            
            val state = viewModelFlow.state.value
            
            // Check if we should load more:
            // 1. Not currently loading (any type of loading)
            // 2. Reached the last item (or close to it)
            // 3. Has more data available
            val isNotLoading = !state.isLoading && !state.isRefreshing && !state.isLoadingMore
            val hasReachedEnd = (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
            val hasMoreData = (state.uiState as? UiState.Success)?.hasMore ?: false
            //
            if (isNotLoading && hasReachedEnd && hasMoreData) {
                viewModelFlow.processIntent(UserIntent.LoadMore)
            }
        }
    }

    private fun setupPodTypeTags() {
        binding.tagEpod.setOnClickListener { selectPodType(PendingPodViewModelFlow.PodType.EPOD) }
        binding.tagHpod.setOnClickListener { selectPodType(PendingPodViewModelFlow.PodType.HPOD) }
    }

    private fun setupPullToRefresh() {
        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false  // dismiss spinner; shimmer takes over
            viewModelFlow.processIntent(UserIntent.SwipeToRefresh)
        }
    }

    private fun setupSearchButton() {
        binding.editStickySearch.setOnClickListener {
            handleAction(HomePodSearchAction_Search, 1, HomePodSearchItem())
        }
    }




    // ────────────────────────── State observation (single entry point) ──

    /**
     * Collects [PendingPodViewModelFlow.state] once and dispatches each field
     * to its own dedicated handler. Events are collected in a parallel coroutine
     * since they live in a separate SharedFlow.
     *
     * Called once from [onViewCreated].
     */
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // ── Primary state: one collector, one handler per field ──
                launch {
                    viewModelFlow.state.collect { state ->
                        onLoading(state.isLoading)
                        onRefreshing(state.isRefreshing)
                        onLoadingMore(state.isLoadingMore)
                        if (!state.isLoading) onUiState(state.uiState)
                        onPodCounts(state.podCounts)
                        onPodType(state.currentPodType)
                    }
                }

                // ── One-time events: separate channel, not state ──
                launch {
                    viewModelFlow.events.collect { handleEvent(it) }
                }
            }
        }
    }




    // ────────────────────────── Per-field state handlers ─────────────

    /**
     * Full-screen shimmer: shown during initial load, retry, and tab switches.
     */
    private fun onLoading(isLoading: Boolean) {
        isLoadingData = isLoading
        if (isLoading) {
            adapter.setItems(listOf(HomePodProgressItem()))
        } else {
            // Safe to omit: when isLoading transitions to false, the new data is concurrently available
            // via onUiState (which calls setItems or clearItems), so the list is entirely replaced
            // wiping out all the shimmers anyway. If we attempt Remove here, it only removes the first
            // matching item by key, leaving behind duplicates due to shared keys.
        }
    }

    /**
     * Pull-to-refresh spinner: driven directly by the SwipeRefreshLayout.
     * When refreshing, old data stays visible in the list underneath.
     */
    private fun onRefreshing(isRefreshing: Boolean) {
        binding.refreshLayout.isRefreshing = isRefreshing
    }

    /**
     * Pagination footer spinner: appears at the bottom of the existing list
     * while the next page is being fetched.
     * 
     * Posts adapter changes to the next frame to avoid IllegalStateException
     * when called during RecyclerView scroll/layout pass.
     */
    private fun onLoadingMore(isLoadingMore: Boolean) {
        if (isLoadingData) return // Let onLoading handle the progress items; don't interfere

        /**
         * Post to next frame to avoid modifying adapter during scroll callback
         *
         * It "defers" the adapter modification to the next Looper iteration —
         * by that time RecyclerView has completed its scroll computation and
         * it's safe to mutate the adapter
         */
        binding.rvPendingPod.post {
            val op = if (isLoadingMore) DataRVAdapterOperationType.Add
            else               DataRVAdapterOperationType.Remove
            adapter.operation(listOf(Pair(HomePodProgressItem(), op)))
        }
    }

    /**
     * Data state renderer. Handles only Idle / Success / Empty / Error —
     * Loading is now handled entirely by [onLoading] and [onRefreshing].
     */
    private fun onUiState(uiState: UiState<List<HomeTripsItemData>>) {
        when (uiState) {
            is UiState.Idle -> {
                // Waiting for first load — shimmer is already shown by onLoading.
            }

            is UiState.Loading -> {
                // Should not occur: this ViewModel never emits UiState.Loading.
                // Kept for exhaustive when-expression safety.
            }

            is UiState.Success -> {
                // We use setItems here instead of looping with AddUpdate to safely bulk-insert
                // everything without triggering the notifyItemInserted out-of-bounds bug.
                val trips = uiState.data.map { HomePodTripItem(it) }
                adapter.setItems(trips)
                
                trackAnalyticsForCurrentPodType(uiState.data.size)
            }

            is UiState.Empty -> {
                adapter.clearItems()
                adapter.operation(listOf(
                    Pair(HomePodWarningItem_NoLoads, DataRVAdapterOperationType.AddUpdate)
                ))
            }

            is UiState.Error -> {
                adapter.clearItems()
                adapter.operation(listOf(
                    Pair(HomePodWarningItem_TimeOut, DataRVAdapterOperationType.AddUpdate)
                ))
                uiUtils.showSnackbar(uiState.message)
            }
        }
    }

    /**
     * Updates EPOD / HPOD badge counts on the tab labels and notifies the parent fragment.
     * No-op when counts are null (before first successful response).
     */
    private fun onPodCounts(podCounts: com.delhivery.axle.data.home.trips.PodCounts?) {
        podCounts ?: return
        epodCount = podCounts.epod_pending_count
        hpodCount = podCounts.hpod_pending_count
        binding.tagEpod.text = "ePOD ($epodCount)"
        binding.tagHpod.text = "HPOD ($hpodCount)"
        (parentFragment as? HomeNewPodFragment)
            ?.viewModel?.podCountsLiveData?.postValue(podCounts)
    }

    /**
     * Updates tab selection UI and tells the adapter which section is active
     * so it can render the correct action buttons on each trip card.
     */
    private fun onPodType(podType: PendingPodViewModelFlow.PodType) {
        updatePodTypeTagUI(podType)
        // Only update the adapter when the pod type actually changes.
        // Without this guard, every state emission (including the one that injects shimmer items
        // via setItems) triggers notifyDataSetChanged(), which immediately recycles the
        // freshly-added shimmer ViewHolders before the RV can lay them out and animate them.
        if (podType != lastRenderedPodType) {
            lastRenderedPodType = podType
            adapter.isHPODSection = (podType == PendingPodViewModelFlow.PodType.HPOD)
            adapter.notifyDataSetChanged()
        }
    }

    // ────────────────────────── One-time events ──────────────────────

    private fun handleEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowToast -> uiUtils.showToast(event.message)

            is UiEvent.ShowSnackbar -> {
                if (event.action != null && event.onActionClick != null) {
                    uiUtils.showSnackbarWithAction(
                        message    = event.message,
                        actionText = event.action,
                        action     = { event.onActionClick.invoke() }
                    )
                } else {
                    uiUtils.showSnackbar(event.message)
                }
            }

            is UiEvent.Navigate -> { /* handle if needed */ }
        }
    }

    // ────────────────────────── Pod type UI helpers ───────────────────

    private fun selectPodType(type: PendingPodViewModelFlow.PodType) {
        // Reset dedup flags so analytics re-fire for the newly selected type.
        epodListShownTracked = false
        hpodListShownTracked = false
        viewModelFlow.switchPodType(type)
    }

    private fun updatePodTypeTagUI(podType: PendingPodViewModelFlow.PodType) {
        when (podType) {
            PendingPodViewModelFlow.PodType.EPOD -> {
                binding.tagEpod.setBackgroundResource(R.drawable.bg_pod_tag_selected)
                binding.tagEpod.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDelhiveryRed))
                binding.tagHpod.setBackgroundResource(R.drawable.bg_pod_tag_unselected)
                binding.tagHpod.setTextColor(ContextCompat.getColor(requireContext(), R.color.font_labels))
            }
            PendingPodViewModelFlow.PodType.HPOD -> {
                binding.tagHpod.setBackgroundResource(R.drawable.bg_pod_tag_selected)
                binding.tagHpod.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDelhiveryRed))
                binding.tagEpod.setBackgroundResource(R.drawable.bg_pod_tag_unselected)
                binding.tagEpod.setTextColor(ContextCompat.getColor(requireContext(), R.color.font_labels))
            }
        }
    }

    // ────────────────────────── Analytics ────────────────────────────

    private fun trackAnalyticsForCurrentPodType(itemCount: Int) {
        if (itemCount == 0) return
        when (viewModelFlow.state.value.currentPodType) {
            PendingPodViewModelFlow.PodType.EPOD -> {
                if (!epodListShownTracked) {
                    analyticsUtil.moEngageTrackEvent(
                        EVENT_EPOD_LIST_SHOWN,
                        mutableListOf(PROPERTY_USER_ID, PROPERTY_PAGE_NAME),
                        mutableListOf(userPrefs.userId(), "my_pods")
                    )
                    epodListShownTracked = true
                }
            }
            PendingPodViewModelFlow.PodType.HPOD -> {
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

    // ────────────────────────── Action handler (adapter callbacks) ────

    override fun handleAction(
        actionId: String,
        position: Int,
        item: BaseHomePodRVAdapterItem<*>
    ) {
        when (actionId) {

            HomePodHeaderAction_Epod -> {
                if (!isLoadingData) selectPodType(PendingPodViewModelFlow.PodType.EPOD)
            }

            HomePodHeaderAction_Physical -> {
                if (!isLoadingData) selectPodType(PendingPodViewModelFlow.PodType.HPOD)
            }

            HomePodHeaderAction_Dispactched -> {
                // Not applicable for the Pending POD tab.
            }

            HomePodSearchAction_Search -> {
                context?.let { startActivity(searchIntent(it)) }
            }

            HomePodWarningAction_NoTrips -> {
                Log.i("HomePodWarningAction", "navigate to loads")
                action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
            }

            HomePodWarningAction_TimeOut -> {
                viewModelFlow.processIntent(UserIntent.Retry)
            }

            HomeTripsRequestAction_UploadEpod -> {
                val data = item.data as HomeTripsItemData
                val podAction = data.podAction()

                // Only open the upload screen when there is something the user can do.
                // UPLOAD → first-time upload, REJECT → re-upload after rejection.
                // REVIEW and VIEWPOD are read-only states; no upload screen needed.
                when (podAction) {
                    PODStatus.UPLOAD, PODStatus.REJECT -> {
                        val transactionId = data.transactionId ?: return  // guard before lambda
                        context?.let {
                            startActivityForResult(
                                uploadImageIntent(
                                    context                   = it,
                                    transactionId             = transactionId,
                                    reachedTime               = data.reachedTime ?: "",
                                    unloadedTime              = data.unloadingTime ?: "",
                                    podStatus                 = podAction,
                                    intermittentPayableAmount = data.intermittentPayableAmount,
                                    source                    = "epod"
                                ),
                                REQCODE_UPLOAD_POD
                            )
                        }
                    }
                    else -> { /* REVIEW / VIEWPOD: no action on this screen */ }
                }
            }

            HomeTripsRequestAction_UploadTracking -> {
                val data = item.data as HomeTripsItemData
                val isHpod = viewModelFlow.state.value.currentPodType == PendingPodViewModelFlow.PodType.HPOD
                context?.let {
                    if (isHpod) {
                        startActivityForResult(
                            docketUpdateIntent(context = it, trip = data, podStatus = data.podAction(), source = "hpod"),
                            REQCODE_UPLOAD_DOCKET
                        )
                    } else {
                        startActivityForResult(
                            uploadImageIntent(
                                it,
                                data.transactionId,
                                data.reachedTime!!,
                                data.unloadingTime!!,
                                podStatus = data.podAction(),
                                intermittentPayableAmount = data.intermittentPayableAmount,
                                source = "epod"
                            ),
                            REQCODE_UPLOAD_POD
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
                showLRListDialog(item.data as HomeTripsItemData)
            }
        }
    }

    // ────────────────────────── LR list dialog ───────────────────────

    private fun showLRListDialog(trip: HomeTripsItemData) {
        val lrNumbers = trip.getAllLRNumbersList()
        if (lrNumbers.isEmpty()) return

        val dialog = Dialog(requireContext())
        val dialogBinding = DialogLrListBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.rvLRList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = LRListAdapter(lrNumbers)
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }

        val metrics = resources.displayMetrics
        val marginPx = (16f * metrics.density).toInt()
        dialog.window?.apply {
            setLayout(metrics.widthPixels - 2 * marginPx, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes?.windowAnimations = R.style.DialogAnimation
            attributes = attributes?.also { it.gravity = Gravity.CENTER }
        }

        dialog.show()
    }

    // ────────────────────────── onActivityResult ─────────────────────

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            // After a successful upload, reload silently (shimmer, not swipe-spinner).
            REQCODE_UPLOAD_POD,
            REQCODE_UPLOAD_DOCKET -> viewModelFlow.processIntent(UserIntent.InitialLoad)
        }
    }
}
