package com.delhivery.axle.ui.home.fragments.loads

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.loads.*
import com.delhivery.axle.data.home.trips.HomeTripsSearchAction_Search
import com.delhivery.axle.databinding.FragmentHomeLoadsBinding
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialog
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.biddetails.BulkBidDetailsCreateEditDialog
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.axle.ui.dialogs.BidConfirmReviseDialog
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckBaseFragment
import com.delhivery.axle.ui.searchload.SearchLoadActivity
import com.delhivery.axle.ui.trucks.truckIntent
import com.delhivery.axle.ui.userroutes.userRoutesIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotEmpty
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import com.github.florent37.kotlin.pleaseanimate.core.position.PositionAnimExpectation
import javax.inject.Inject

class HomeLoadsFragment : HomeLoadsTruckBaseFragment<FragmentHomeLoadsBinding, HomeLoadsViewModel>(),
    HomeLoadsRVAdapterInterface, TitleProvider {

  var _title: String = "Load Request"

  override val title: CharSequence
    get() = _title

  private val MINIMUM = 25
  var scrollDist = 0
  var visible = false
  var express: String?= null
  var isExpress = false
  var pos = 0

  @Inject lateinit var dialogUtils: DialogUtils
  @Inject lateinit var fcmUtils: FCMUtils
  @Inject lateinit var userPrefs: UserPrefs

  init {
    toolbarElevationLiveData = MutableLiveData()
    hasInlineProgress = true
  }

  companion object {
    /* singleton instance */
    val _instance: HomeLoadsFragment by lazy { HomeLoadsFragment() }
  }

  override fun getViewModelClass() = HomeLoadsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_loads

  /* RV adapter */
  private val adapter: HomeLoadsRVAdapter by lazy {
    HomeLoadsRVAdapter(this)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      refreshData()
    }

    /* setup recycler view */
    binding.rvLoads.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@HomeLoadsFragment.adapter
      addOnScrollListener(HomeLoadsRVScrollListener(binding.editStickySearch))
      addOnScrollListener(PaginationInterface())
    }

    binding.editStickySearch.setOnClickListener {
      handleAction(
          HomeTripsSearchAction_Search, HomeLoadsSearchItem()
      )
    }

    binding.routesBanner.setOnClickListener {
      context?.let {
        startActivity(userRoutesIntent(it))
      }
    }

    viewModel.progressLiveData.reobserve(viewLifecycleOwner, ProgressObserver())

    viewModel.userLoadsData.reobserve(viewLifecycleOwner, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })

    viewModel.loadsCountLiveData.reobserve(viewLifecycleOwner, Observer {
      _title = when (it) {
        0, null -> getString(string.label_load_request)
        else -> "${getString(string.label_load_request)}($it)"
      }
    })

    viewModel.routesLiveData.reobserve(viewLifecycleOwner, Observer {
      when (it) {
        false -> binding.rvLoads.apply {
          this@HomeLoadsFragment.visible = true
          binding.routesBanner.visibility = View.VISIBLE
          addOnScrollListener(BannerRVScrollListener())
        }

        true -> {
          binding.routesBanner.visibility = View.GONE
          this@HomeLoadsFragment.visible = false
        }
      }
    })

    viewModel.reviseBidLiveData.reobserve(viewLifecycleOwner, Observer {
      if (it.first) {
        val data = adapter.itemsList()[it.second].data as? HomeBidsRequestItemData
        BidDetailsCreateEditDialog(
            context!!, data!!, data!!.transactionBid, viewModel, it.second, analyticsUtil, userPrefs , "load_screen"
        ).show()
      }
    })

    viewModel.bidsActionLiveData.reobserve(viewLifecycleOwner, Observer {
      uiUtils.toggleKeyboard()
          .apply {
            when {
              it != null -> {
                val data = adapter.itemsList()[it.first].data as? HomeBidsRequestItemData
                data?.transactionBid = it.second

                if (data != null) {
                  uiUtils.showProgress()
                  viewModel.fetchLowestBid(data, it.first)
                }
              }
            }
          }
    })

    viewModel.bulkBidActionLiveData.reobserve(viewLifecycleOwner, Observer {
      if(it != null){
        val data = adapter.itemsList()[it.first].data as? HomeBidsRequestItemData
        data?.bulkTransactionBids = it.second
        adapter.notifyItemChanged(it.first)
      }
    })

    viewModel.editBulkLiveData.reobserve(viewLifecycleOwner, Observer {
      if(it.first == 10){
        Toast.makeText(context,"Bids Created Successfully",Toast.LENGTH_SHORT).show()
      }
      if(it.first == 20){
        Toast.makeText(context,"Bids Updated Successfully",Toast.LENGTH_SHORT).show()
      }
      if(it.first == 30){
        Toast.makeText(context,"Bids Deleted Successfully",Toast.LENGTH_SHORT).show()
      }
        if(viewModel.editFlg[0] &&  viewModel.editFlg[1] && viewModel.editFlg[2]){
          viewModel.transactionBidForBulk(it.second, pos)
          viewModel.editFlg = mutableListOf(false, false, false)
        }
    })

    viewModel.lowestBidLiveData.reobserve(viewLifecycleOwner, Observer {
      uiUtils.hideProgress()
      if (it != null) {
        if (it.second.oneVisibility()==View.VISIBLE || it.second.twoVisibility()==View.VISIBLE){
          analyticsUtil.trackEvent(
                  EVENT_BID_INLINE_PROMPT,
                  mutableListOf(PROPERTY_USER_ID , PROPERTY_TRANSACTION_ID, PROPERTY_DEMAND_TYPE , PROPERTY_OVERALL_PERFORMANCE),
                  mutableListOf(userPrefs.userId() , it.second.key() , userPrefs.demandType, userPrefs.userPerformance)
          )

        }
        else if (it.second.threeVisibility()==View.VISIBLE || it.second.fourVisibility()==View.VISIBLE){
          analyticsUtil.trackEvent(
                  EVENT_BID_REVISE_PROMPT,
                  mutableListOf(PROPERTY_USER_ID , PROPERTY_TRANSACTION_ID , PROPERTY_DEMAND_TYPE , PROPERTY_OVERALL_PERFORMANCE),
                  mutableListOf(userPrefs.userId() , it.second.key() , userPrefs.demandType, userPrefs.userPerformance)
          )
        }

        BidConfirmReviseDialog(
            context!!, it.second, viewModel, it.first
        ).show()
      }
      adapter.notifyItemChanged(it.first)
    })

    viewModel.dataLoadingLiveData.reobserve(viewLifecycleOwner, Observer {
      isLoadingData = it ?: false
    })

    if (viewModel.isFCMTokenGenerated()) {
      fcmUtils.generateToken {
        if (it.isNotNullOrEmpty()) {
          viewModel.updateFCMToken(it)
        }
      }
    }

    refreshData()

    viewModel.updateUserAppAccess()

    viewModel.truckGetLiveData.reobserve(viewLifecycleOwner, Observer {
      uiUtils.hideProgress()
      if(it!= null ){
        val pageTitle = if(it.second.bulkTransactionBids!= null && it.second.bulkTransactionBids.isNotEmpty()) "EDIT BIDS" else "PLACE BIDS"
        if(it.second.truckUUID != null) {
          BulkBidDetailsCreateEditDialog(context!!, it.second, it.second.bulkTransactionBids, it.first, viewModel, it.second.unAllocatedVolume!!,
            pos, analyticsUtil, userPrefs, "load_screen", pageTitle).show()
        }
        else{
          Toast.makeText(context, "No Vehicle Types Found",Toast.LENGTH_SHORT).show()
        }
      }
    })
  }

  override fun onResume() {
    super.onResume()
    viewModel.paginateCount = 0
    viewModel.checkUserRoutes()
    if (viewModel.routeUpdated || viewModel.fromNotification) {
      refreshData()
      viewModel.routeUpdated = false
      viewModel.fromNotification = false
    }
  }

  override fun onStop() {
    super.onStop()
    if (viewModel.paginateCount > 0) {
      analyticsUtil.trackEvent(
              EVENT_LOAD_SCROLL,
              mutableListOf(PROPERTY_USER_ID, PROPERTY_DEMAND_TYPE, PROPERTY_NO_OF_SCROLLS, PROPERTY_OVERALL_PERFORMANCE),
              mutableListOf(userPrefs.userId(), userPrefs.demandType, viewModel.paginateCount.toString(), userPrefs.userPerformance)
      )
    }
    viewModel.paginateCount = 0
  }

  override fun onPause() {
    super.onPause()
    if (viewModel.paginateCount > 0) {
      analyticsUtil.trackEvent(
              EVENT_LOAD_SCROLL,
              mutableListOf(PROPERTY_USER_ID, PROPERTY_DEMAND_TYPE, PROPERTY_NO_OF_SCROLLS, PROPERTY_OVERALL_PERFORMANCE),
              mutableListOf(userPrefs.userId(), userPrefs.demandType, viewModel.paginateCount.toString(), userPrefs.userPerformance)
      )
    }
    viewModel.paginateCount = 0
  }

  private fun refreshData() {
    viewModel.paginateCount = 0
    viewModel.hasOrionLoadOnce = false
    viewModel.routeUpdated = false
    adapter.resetStaticData()
    viewModel.fetchUserTransactions(false, express, isExpress)
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeLoadsRVAdapterItem<*>
  ) {
    // handle actions here
    when (actionId) {
      HomeBidsRequestAction_ViewDetails -> {
        val data = item.data as HomeBidsRequestItemData
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_ITEM,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
            mutableListOf(VALUE_LOAD, data.transactionId ?: "")
        )
        context?.let { startActivity(bidDetailsIntent(data.key(), it, if(data.isDMTIndent()) "dmt" else "")) }
      }

      HomeLoadsSearchAction_Search -> {
        context?.let {
          startActivity(
              Intent(it, SearchLoadActivity::class.java)
          )
        }
      }

      HomeLoadsInfoAction_EditRoute -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_EDIT_ROUTE,
            mutableListOf(PROPERTY_SOURCE),
            mutableListOf(VALUE_LOAD_INFO)
        )
        context?.let {
          startActivity(userRoutesIntent(it))
        }
      }

      HomeLoadsWarningAction_NoLoads -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_EDIT_ROUTE,
            mutableListOf(PROPERTY_SOURCE),
            mutableListOf(VALUE_NO_RESULTS)
        )
        context?.let {
          startActivity(userRoutesIntent(it))
        }
      }

      HomeLoadsTimeOutAction -> {
        refreshData()
      }

      HomeLoadsFilterAction -> {
        //Capture Event
        analyticsUtil.trackEvent(
                EVENT_FILTER_EXPRESS_LOADS,
                mutableListOf(PROPERTY_USER_ID),
                mutableListOf(userPrefs.userId())
        )

        if (isExpress) {
          isExpress = false
          express = null
        } else {
          isExpress = true
          express = "EXP"
        }
        refreshData()
      }

      HomeLoadsVehicleFilterAction -> {
        showVehicleFilterDialog()
      }

      HomeLoadsInfoAction_Search -> {
        //Capture Event
        analyticsUtil.trackEvent(
                EVENT_SHOW_ADDITIONAL_LOADS,
                mutableListOf(PROPERTY_USER_ID , PROPERTY_DEMAND_TYPE),
                mutableListOf(userPrefs.userId(), userPrefs.demandType)
        )

        viewModel.hasOrionLoadOnce = true
        adapter.removeInfoData()
        val all_truck_types = listOf("open", "closed", "trailer")
        var currentVehicleFilterList = listOf<String>()
        var exclude_truck_types = listOf<String>()
        currentVehicleFilterList = if (viewModel.passing_vehicle_type.isNotNullOrEmpty()) {
          viewModel.passing_vehicle_type!!.split(",")
        } else {
          userPrefs.truckTypes!!.split(",")
        }

        if (currentVehicleFilterList.contains("all")) {
          exclude_truck_types = listOf("open", "closed", "trailer")
        } else {
          for (vehicle in all_truck_types) {
            if (currentVehicleFilterList.contains(vehicle)) {
              exclude_truck_types = exclude_truck_types + vehicle
            }
          }
        }
        val exclude_truck_str = exclude_truck_types.joinToString( separator = ",") {it}
        viewModel.filterVehicleType = null
        viewModel.fetchUserTransactions(false, express, isExpress, true, exclude_truck_str)
      }

      HomeLoadsPriorityAction -> {
        context?.let { startActivityForResult(truckIntent(context!!), REQCODE_ADD_TRUCK) }
      }

      HomeLoadsBannerAction -> {
        context?.let { startActivityForResult(truckIntent(context!!), REQCODE_ADD_TRUCK) }
      }
    }
  }

  private fun showVehicleFilterDialog(){
    lateinit var dialog: AlertDialog

    // Initialize an array of vehicles
    val arrayVehicle = arrayOf("open","closed","trailer","all")

    val arrayChecked = booleanArrayOf(false,false,false,false)

    var currentVehicleFilterList = listOf<String>()
    currentVehicleFilterList = if (viewModel.passing_vehicle_type.isNotNullOrEmpty()) {
      viewModel.passing_vehicle_type!!.split(",")
    } else {
      userPrefs.truckTypes!!.split(",")
    }
    if (currentVehicleFilterList.isNotEmpty()) {
      for (vehicle in currentVehicleFilterList) {
        if (arrayVehicle.contains(vehicle))
        {
          arrayChecked[arrayVehicle.indexOf(vehicle)] = true
        }
      }
    }

    val builder = AlertDialog.Builder(context)

    builder.setTitle("-- Select vehicle types --")

    builder.setMultiChoiceItems(arrayVehicle, arrayChecked) { _, which, isChecked ->
      arrayChecked[which] = isChecked
    }

    builder.setPositiveButton("Filter") { _, _ ->

      //Capture event
      analyticsUtil.trackEvent(
              EVENT_FILTER_VEHICLE_TYPE,
              mutableListOf(PROPERTY_USER_ID),
              mutableListOf(userPrefs.userId())
      )

      var filterVehicleTypes = listOf<String>()
      for (vehicle in arrayVehicle) {
        if (arrayChecked[arrayVehicle.indexOf(vehicle)]) {
          filterVehicleTypes  = filterVehicleTypes + vehicle
        }
      }

      viewModel.vehicleStr = filterVehicleTypes.joinToString( separator = ",") {it}
      viewModel.filterVehicleType = true
      refreshData()
    }

    builder.setNegativeButton("Cancel") {_, _ ->
      dialog.dismiss()
    }

    dialog = builder.create()
    dialog.show()
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeLoadsRVAdapterItem<*>,
    position: Int
  ) {
    when (viewModel.userPrefs.canBid()) {
      APPROVED -> {
        when (actionId) {
          HomeBidsRequestAction_PlaceBid -> {
            pos =position
            val data = item.data as HomeBidsRequestItemData
            if (data.isDMTIndent()) {
              uiUtils.showProgress()
              viewModel.fetchTruckType(data)
            }
            else{
              item.data.let {
                BidDetailsCreateEditDialog(
                        context!!, it, it.transactionBid, viewModel, position, analyticsUtil, userPrefs , "load_screen"
                ).show()
              }
            }
          }
        }
      }
      UNAPPROVED -> {
        dialogUtils.showBasicConfirmDialog(
            string.title_dialog_supplier_not_approved,
            string.msg_dialog_supplier_not_approved,
            getString(string.label_call_us), getString(string.label_mail_us),
            { callHelpline() }, { sendMail() }
        )
      }
      DISABLED -> {
        dialogUtils.showBasicConfirmDialog(
            string.title_dialog_supplier_disabled,
            string.msg_dialog_supplier_disabled,
            getString(string.label_call_us), getString(string.label_mail_us),
            { callHelpline() }, { sendMail() }
        )
      }
    }
  }

  fun hide() {
    binding.routesBanner.animate()
        .translationY(
            PositionAnimExpectation.dpToPx(
                this@HomeLoadsFragment.context!!, binding.routesBanner.height.toFloat()
            )
        )
        .setInterpolator(AccelerateInterpolator(2f))
        .setDuration(200L)
        .start()
  }

  fun show() {
    binding.routesBanner.animate()
        .translationY(
            -PositionAnimExpectation.dpToPx(
                this@HomeLoadsFragment.context!!, 0f
            )
        )
        .setInterpolator(DecelerateInterpolator(2f))
        .setDuration(400L)
        .start()
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQCODE_EDIT_ROUTE && resultCode == RESULT_OK) {
      refreshData()
    }
  }

  /**
   * Progress observer
   */
  inner class ProgressObserver : Observer<Boolean> {
    override fun onChanged(t: Boolean?) {
      t?.let {
        when (t) {
          true -> uiUtils.showProgress("Placing your bid, hang on!")
          false -> uiUtils.hideProgress()
        }
      }
    }
  }

  /**
   * Home loads rv scroll listener for search bar animation related stuff
   */
  inner class HomeLoadsRVScrollListener(
    private val stickyView: DelhiveryAnimatedSearchBar,
    private val elevation: Float = 12f
  ) : OnScrollListener() {
    /* Current toolbar elevation */
    private var toolbarElevation = -1f

    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      val layoutManager: LinearLayoutManager? = recyclerView.layoutManager as? LinearLayoutManager
      val pos = layoutManager?.findFirstVisibleItemPosition()
      val toolbarElevation = if (pos == 0) {
        val childView = recyclerView.findViewHolderForAdapterPosition(0)!!.itemView
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
        defToolbarElevation
      } else {
        stickyView.translationY = 0f
        stickyView.alpha = 1f
        stickyView.setRatio(0f)
        0f
      }
      if (toolbarElevation != this.toolbarElevation && toolbarElevationLiveData != null) {
        this.toolbarElevation = toolbarElevation
        toolbarElevationLiveData?.postValue(this.toolbarElevation)
      }
    }
  }

  /**
   * Home loads rv scroll listener for banner animation related stuff
   */
  inner class BannerRVScrollListener : OnScrollListener() {

    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      if (visible && scrollDist > MINIMUM) {
        hide()
        scrollDist = 0
        visible = false
      } else if (!visible && scrollDist < -MINIMUM) {
        show()
        scrollDist = 0
        visible = true
      }

      if ((visible && dy > 0) || (!visible && dy < 0)) {
        scrollDist += dy
      }
    }
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(50) {
    override fun loadMore() = viewModel.fetchUserTransactions(true, express, isExpress)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }
}