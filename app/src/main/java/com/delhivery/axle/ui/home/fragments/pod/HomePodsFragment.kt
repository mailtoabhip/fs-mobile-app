package com.delhivery.axle.ui.home.fragments.pod

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.repository.UserTripsLoadLimit
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
import com.delhivery.axle.data.home.trips.TripStatus.EPodUploaded
import com.delhivery.axle.data.home.trips.TripStatus.TruckUnloaded
import com.delhivery.axle.databinding.FragmentHomePodBinding
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar.ToolbarElevationChangeListener
import com.delhivery.axle.ui.home.activity.docket.docketUpdateIntent
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType.LoadsFragment
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Pod
import com.delhivery.axle.ui.searchtrip.searchIntent
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.ui.tripdetails.uploadImageIntent
import com.delhivery.axle.utils.AWSUtils
import com.delhivery.axle.utils.AWSUtils.AWSProgressInterface
import com.delhivery.axle.utils.DialogUtils
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.REQCODE_UPLOAD_DOCKET
import com.delhivery.axle.utils.REQCODE_UPLOAD_POD
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import javax.inject.Inject

class HomePodsFragment : HomeBaseFragment<FragmentHomePodBinding, HomePodViewModel>(),
    HomePodRVAdapterInterface, ToolbarElevationChangeListener, AWSProgressInterface {

  var _title: String = "Pods"

  override val title: CharSequence
    get() = _title

  init {
    toolbarElevationLiveData = MutableLiveData()
    hasInlineProgress = true
  }

  companion object {
    /* singleton instance */
    val _instance: HomePodsFragment by lazy { HomePodsFragment() }
  }

  @Inject lateinit var dialogUtils: DialogUtils
  @Inject lateinit var awsUtils: AWSUtils
  @Inject lateinit var userPrefs: UserPrefs

  override fun getViewModelClass() = HomePodViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_pod

  private val adapter: HomePodRVAdapter by lazy { HomePodRVAdapter(this) }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      refreshData()
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

    viewModel.userPodsData.observe(this, Observer {
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

    viewModel.selectedLiveData.observe(this, Observer {
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

    viewModel.delegationLiveData.observe(this, Observer {
      if (it != null) {
        awsUtils.startDownload(it.first, it.second, it.third, this)
      } else {
        uiUtils.showSnackbar("Please try again")
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
    viewModel.fetchTrips()
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
        context?.let { startActivity(searchIntent(it)) }
      }

      HomePodWarningAction_NoTrips -> {
        action(NavigateHomeFragmentAction(LoadsFragment))
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
                  uploadImageIntent(it, data.transactionId, data.reachedTime!!, data.unloadingTime!!), REQCODE_UPLOAD_POD
              )
            }
          }
          else -> {
            compositeDisposable += requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onBackground()
                .subscribe { granted, error ->
                  if (error == null && granted) {
                    uiUtils.showSnackbar("Downloading POD....")
                    val file = getFile()
                    if (file != null && !TextUtils.isEmpty(data.podUrl)) {
                      uiUtils.showProgress()
                      data.podUrl?.let {
                        viewModel.podUrl = it
                        viewModel.getDelegationToken(it, file)
                      }
                    } else {
                      uiUtils.showSnackbar("Can't process POD")
                    }
                  } else {
                    uiUtils.showSnackbar(getString(string.storage_permission))
                  }
                }
          }
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

  override fun onAWSSuccess(
    path: String
  ) {
    if (isAdded) {
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Downloaded")
      val file = getFile()
      if (file != null) {
        openFile(file)
      } else {
        uiUtils.showSnackbar("Can't process POD")
      }
    }
  }

  override fun onAWSFailure() {
    if (isAdded) {
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Couldn't complete download, please try after sometime")
    }
  }

  private fun openFile(file: File) {
    try {
      require(context != null)
      val uri = FileProvider.getUriForFile(context!!, "${context!!.packageName}.provider", file)
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
    override fun loadMore() = viewModel.fetchTrips(true)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }

  inner class HomePODRVScrollListener(
    private val stickyView: DelhiveryAnimatedSearchBar,
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
  }

}