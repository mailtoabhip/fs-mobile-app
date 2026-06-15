package com.dfd.delfin.ui.home.fragments.pod

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.dfd.delfin.R
import com.dfd.delfin.R.string
import com.dfd.delfin.api.repository.UserTripsLoadLimit
import com.dfd.delfin.api.request.SearchRequest
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
import com.dfd.delfin.data.home.trips.PODStatus
import com.dfd.delfin.data.home.trips.TripStatus.EPodUploaded
import com.dfd.delfin.data.home.trips.TripStatus.TruckUnloaded
import com.dfd.delfin.databinding.FragmentHomePodBinding
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.common.UiEvent
import com.dfd.delfin.ui.common.UiState
import com.dfd.delfin.ui.custom.DelfinAnimatedSearchBar
import com.dfd.delfin.ui.custom.DelfinAnimatedSearchBar.ToolbarElevationChangeListener
import com.dfd.delfin.ui.home.activity.docket.docketUpdateIntent
import com.dfd.delfin.ui.home.fragments.HomeBaseFragment
import com.dfd.delfin.ui.home.fragments.HomeFragmentType
import com.dfd.delfin.ui.home.fragments.NavigateHomeFragmentAction
import com.dfd.delfin.ui.home.fragments.pod.HomePodRVAdapterItemType.Pod
import com.dfd.delfin.ui.searchtrip.searchIntent
import com.dfd.delfin.ui.tripdetails.tripDetailsIntent
import com.dfd.delfin.ui.tripdetails.uploadImageIntent
import com.dfd.delfin.utils.DatePatterns.OrionDateFormat
import com.dfd.delfin.utils.DateUtils
import com.dfd.delfin.utils.DialogUtils
import com.dfd.delfin.utils.DocumentUtils
import com.dfd.delfin.ui.common.UserIntent
import com.dfd.delfin.utils.PaginationScrollListener
import com.dfd.delfin.utils.REQCODE_UPLOAD_DOCKET
import com.dfd.delfin.utils.REQCODE_UPLOAD_POD
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import javax.inject.Inject

class HomePodsFragment : HomeBaseFragment<FragmentHomePodBinding, HomePodViewModel>(),
    HomePodRVAdapterInterface, ToolbarElevationChangeListener {

  var _title: String = "PODs"

  override val title: CharSequence
    get() = _title

  init {
    toolbarElevationLiveData = MutableLiveData()
    hasInlineProgress = true
  }
  private var fragmentSetupTrace: Trace? = null
  private var isFirstResume = true
  companion object {
    /* singleton instance */
    val _instance: HomePodsFragment by lazy { HomePodsFragment() }
  }

  @Inject lateinit var dialogUtils: DialogUtils
  @Inject lateinit var userPrefs: UserPrefs
  @Inject lateinit var documentUtils: DocumentUtils
  @Inject lateinit var viewModelFlow: HomePodViewModelFlow
  
  // Store current download context
  private var currentPodUrl: String? = null
  private var currentPodFile: File? = null

  override fun getViewModelClass() = HomePodViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_pod

  private val adapter: HomePodRVAdapter by lazy { HomePodRVAdapter(this) }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomePodsFragment_SetupTime")
    fragmentSetupTrace?.start()
    
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
            // Pod counts available if needed for future use
          }
        }
      }
    }
    
   


    binding.rvPod.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
      adapter = this@HomePodsFragment.adapter
      addOnScrollListener(HomePODRVScrollListener(binding.editStickySearch))
      addOnScrollListener(PaginationInterface())
    }

    binding.editStickySearch.setOnClickListener {
      handleAction(HomePodSearchAction_Search, 1, HomePodSearchItem())
    }


    binding.btnSave.setOnClickListener {
      context?.let {
        val list = arrayListOf<String>()
        list.addAll(viewModel.selectedTransactions)
        startActivityForResult(
            docketUpdateIntent(context = it, transactionIds = list), REQCODE_UPLOAD_DOCKET
        )
      }
    }

    adapter.setItems(getStaticItems())

    viewModel.userPodsData.observe(viewLifecycleOwner, Observer {
      it?.let { _items ->
        adapter.operation(_items)
      }
    })

    viewModel.tripsCountLiveData.reobserve(this, Observer {
      _title = when (it) {
        0, null -> getString(string.label_pod_status)
        else -> "${getString(string.label_pod_status)}($it)"
      }
    })

    viewModel.dataLoadingLiveData.reobserve(this, Observer {
      isLoadingData = it ?: false
    })

    viewModel.selectedLiveData.observe(viewLifecycleOwner, Observer {
      if (it == 0) {
        viewModel.selectable = false
        binding.btnSave.visibility = View.GONE
        adapter.itemsList()
            .forEach { t ->
              if (t.type == Pod) {
                val trip = t.data as HomeTripsItemData
                trip.selectable = viewModel.selectable
              }
            }
      }
    })

    viewModel.dispatch = false
    refreshData()
  }


  private fun refreshData() {
    adapter.resetStaticData()
    viewModel.selectedTransactions.clear()
    viewModel.selectable = false
    binding.btnSave.visibility = View.GONE
    
    // Build search request
    val request = SearchRequest()
    // Note: vendorId will be set by ViewModel internally
    
    // Set status-specific parameters
    when (viewModel.status) {
      TruckUnloaded -> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -14)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        request.tripStatus = viewModel.status.statusKey
        request.value = DateUtils.formatDate(cal.time, OrionDateFormat)
      }
      EPodUploaded -> {
        request.tripStatus = EPodUploaded.statusKey + "," + TruckUnloaded.statusKey
        request.value = null
      }
      else -> {}
    }
    
    // Send Search intent to ViewModel (pure MVI approach)
    viewModelFlow.processIntent(UserIntent.Search(request))
  }

  /**
   * Renders UI based on current UiState.
   * This function handles all possible states exhaustively.
   */
  private fun renderState(state: UiState<List<HomeTripsItemData>>) {
    when (state) {
      is UiState.Idle -> {
        // Initial state — only set static items if the adapter is empty.
        // This prevents the StateFlow replay from clobbering the shimmer
        // that resetStaticData() already added during refreshData().
        if (adapter.itemsList().isEmpty()) {
          adapter.setItems(getStaticItems())
        }
      }
      
      is UiState.Loading -> {
        if (state.isRefreshing) {
          // Pull-to-refresh in progress
          binding.refreshLayout.isRefreshing = true
        } else {
          // Initial loading — mark as loading.
          // The shimmer progress item is already added by resetStaticData(),
          // so only do AddUpdate if it's somehow missing (defensive).
          isLoadingData = true
          adapter.operation(HomePodProgressItem(), DataRVAdapterOperationType.AddUpdate)
        }
      }
      
      is UiState.Success -> {
        // Hide loading indicators
        binding.refreshLayout.isRefreshing = false
        isLoadingData = false
        
        // Remove progress item
        val items = mutableListOf<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>()
        items.add(Pair(HomePodProgressItem(), DataRVAdapterOperationType.Remove))
        
        // Add trip items
        for (trip in state.data) {
          trip.selectable = viewModel.selectable
          items.add(Pair(HomePodTripItem(trip), DataRVAdapterOperationType.Add))
        }
        
        adapter.operation(items)
        
        // Update title with count
        _title = when (state.data.size) {
          0 -> getString(string.label_pod_status)
          else -> "${getString(string.label_pod_status)}(${state.data.size})"
        }
        
        // Show/hide load more footer
        if (state.isLoadingMore) {
          adapter.operation(listOf(
            Pair(HomePodProgressItem(), DataRVAdapterOperationType.AddUpdate)
          ))
        }
      }
      
      is UiState.Empty -> {
        // No data available
        binding.refreshLayout.isRefreshing = false
        isLoadingData = false
        
        val items = mutableListOf<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>()
        items.add(Pair(HomePodProgressItem(), DataRVAdapterOperationType.Remove))
        items.add(Pair(HomePodWarningItem_NoLoads, DataRVAdapterOperationType.AddUpdate))
        
        adapter.operation(items)
        
        _title = getString(string.label_pod_status)
      }
      
      is UiState.Error -> {
        // Error occurred
        binding.refreshLayout.isRefreshing = false
        isLoadingData = false
        
        val items = mutableListOf<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>()
        items.add(Pair(HomePodProgressItem(), DataRVAdapterOperationType.Remove))
        items.add(Pair(HomePodWarningItem_TimeOut, DataRVAdapterOperationType.AddUpdate))
        
        adapter.operation(items)
        
        // Show error message
        uiUtils.showSnackbar(state.message)
      }
    }
  }

  /**
   * Handles one-time UI events.
   * These events are consumed only once and don't replay after config changes.
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
        // For now, navigation is handled through existing action system
      }
    }
  }

  private fun getStaticItems() = mutableListOf<BaseHomePodRVAdapterItem<*>>().apply {
    add(0, HomePodHeaderItem())
    add(1, HomePodProgressItem())
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
        userPrefs.setPreviousScreen(this.javaClass.name)
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
        when (data.podAction()) {
          PODStatus.UPLOAD, PODStatus.REJECT -> {
            context?.let {
              startActivityForResult(
                  uploadImageIntent(it, data.transactionId, data.reachedTime!!, data.unloadingTime!!, data.podAction()), REQCODE_UPLOAD_POD
              )
            }
          }

            else -> {}
        }
      }

      HomeTripsRequestAction_UploadTracking -> {
        val data = item.data as HomeTripsItemData
        when {
          data.hasPODTracking() -> context?.let {
            startActivityForResult(
                docketUpdateIntent(context = it, trip = data), REQCODE_UPLOAD_DOCKET
            )
          }
          viewModel.selectable -> {
            if (data.selected) {
              data.selected = false
              viewModel.selectedTransactions.remove(data.transactionId)
            } else {
              data.selected = true
              viewModel.selectedTransactions.add(data.transactionId)
            }
            adapter.notifyItemChanged(position)
          }
          else -> {
            viewModel.selectable = true
            adapter.itemsList()
                .forEach { t ->
                  if (t.type == Pod) {
                    val trip = t.data as HomeTripsItemData
                    trip.selectable = viewModel.selectable
                  }
                }
            data.selected = true
            viewModel.selectedTransactions.add(data.transactionId)
          }
        }
        binding.btnSave.visibility =
          if (viewModel.selectedTransactions.isEmpty()) View.GONE else View.VISIBLE
        adapter.notifyDataSetChanged()
        viewModel.selectedLiveData.postValue(viewModel.selectedTransactions.size)
      }

      HomeTripsRequestAction_ViewDetails -> {
        context?.let {
          val data = item.data as HomeTripsItemData
          startActivity(tripDetailsIntent(data.key(), it))
        }
      }
    }
  }

  private fun getFile(): File? {
    val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val basePath = "$storageDir/" + viewModel.transactionId
    if (viewModel.podUrl != null) {
      return when {
        (viewModel.podUrl).endsWith("pdf") -> File(basePath + "_pod.pdf")
        (viewModel.podUrl).endsWith("png") -> File(basePath + "_pod.png")
        (viewModel.podUrl).endsWith("jpg") || (viewModel.podUrl).endsWith("jpeg") -> File(
            basePath + "_pod.jpg"
        )
        else -> {
          return null
        }
      }
    }
    return null
  }


  private fun openFile(file: File) {
    try {
      require(context != null)
      val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
      val intent = Intent(Intent.ACTION_VIEW)
      if (file.toString().contains(".pdf")) {
        intent.setDataAndType(uri, "application/pdf")
      } else if (file.toString().contains(".jpg") ||
          file.toString().contains(".jpeg") || file.toString().contains(".png")
      ) {
        intent.setDataAndType(uri, "image/jpeg")
      } else {
        intent.setDataAndType(uri, "*/*")
      }
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      startActivity(intent)
    } catch (e: java.lang.Exception) {
      uiUtils.showSnackbar("No application found which can open the file")
    }
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      REQCODE_UPLOAD_POD -> {
        if (resultCode == Activity.RESULT_OK) {
          refreshData()
        }
      }
      REQCODE_UPLOAD_DOCKET -> {
        refreshData()
      }
    }
  }

  override fun postElevation(elevation: Float) {
    toolbarElevationLiveData!!.postValue(elevation)
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(
      UserTripsLoadLimit
  ) {

    override fun hasMore(): Boolean {
      // Check if more data is available from current state
      val currentState = viewModelFlow.uiState.value
      return currentState is UiState.Success && currentState.hasMore
    }

    override fun isLoading() = isLoadingData
  }

  inner class HomePODRVScrollListener(
      private val stickyView: DelfinAnimatedSearchBar,
      private val elevation: Float = 12f
  ) : OnScrollListener() {

    private var toolbarElevation = -1f

    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      val layoutManager =
        (recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager)
      val _toolbarElevation = when (layoutManager.findFirstVisibleItemPosition()) {
        0 -> {
          stickyView.translationY = 0f
          stickyView.visibility = View.GONE
          stickyView.alpha = 0f
          stickyView.setRatio(1f)
          defToolbarElevation
        }
        1 -> {
          stickyView.visibility = View.VISIBLE
          val childView = recyclerView.findViewHolderForAdapterPosition(1)!!.itemView
          val viewTopGap = childView.height - stickyView.height * 1f
          val viewTop = childView.top + viewTopGap
          if (viewTop > 0) {
            val factor = viewTop / viewTopGap
            val invFactor = 1f - factor
            stickyView.translationY = viewTop
            stickyView.alpha = invFactor
            ViewCompat.setElevation(stickyView, elevation * invFactor)
          } else {
            stickyView.translationY = stickyView.top * 1f
            stickyView.alpha = 1f
            ViewCompat.setElevation(stickyView, elevation)
          }
          val factor =
            (childView.height.toFloat() - childView.bottom.toFloat()) / childView.height.toFloat()
          stickyView.setRatio((1 - factor))
          factor * defToolbarElevation
        }
        else -> {
          stickyView.visibility = View.VISIBLE
          stickyView.translationY = 0f
          stickyView.alpha = 1f
          stickyView.setRatio(0f)
          0f
        }
      }
      if (_toolbarElevation != toolbarElevation) {
        toolbarElevation = _toolbarElevation
        toolbarElevationLiveData!!.postValue(toolbarElevation)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    /**\
     * Check if it's generated from deep link
     */
    if( userPrefs.dpLinkArg != ""){
      if (!isLoadingData) {
        if(userPrefs.dpLinkArg == "physicalPod") {
          viewModel.status = EPodUploaded
        }
        else if(userPrefs.dpLinkArg == "ePod"){
          viewModel.status = TruckUnloaded
        }
        viewModel.dispatch = false
        userPrefs.dpLinkArg = ""
        refreshData()
      }
    }
    if (fragmentSetupTrace != null && isFirstResume) {
      fragmentSetupTrace?.stop()
      isFirstResume = false
    }
  }

}