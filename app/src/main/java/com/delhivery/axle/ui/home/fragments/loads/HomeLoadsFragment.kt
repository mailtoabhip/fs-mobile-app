package com.delhivery.axle.ui.home.fragments.loads

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.loads.*
import com.delhivery.axle.data.home.trips.HomeTripsSearchAction_Search
import com.delhivery.axle.data.home.trucks.TruckFrequentItem
import com.delhivery.axle.databinding.DialogBottomTruckAddBinding
import com.delhivery.axle.databinding.FragmentHomeLoadsBinding
import com.delhivery.axle.databinding.ViewFrequentTruckItemBinding
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialog
import com.delhivery.axle.ui.biddetails.BulkBidDetailsCreateEditDialog
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.axle.ui.dialogs.BidConfirmReviseDialog
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.activity.home.orderRank
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckBaseFragment
import com.delhivery.axle.ui.profile.raterewards.ShareRateGetRewardsActivity
import com.delhivery.axle.ui.searchload.SearchLoadActivity
import com.delhivery.axle.ui.trucks.truckIntent
import com.delhivery.axle.ui.userroutes.userRoutesIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotEmpty
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import com.github.florent37.kotlin.pleaseanimate.core.position.PositionAnimExpectation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.moe.pushlibrary.MoEHelper
import com.moengage.core.Properties
import com.moengage.core.internal.MoEConstants.USER_ATTRIBUTE_UNIQUE_ID
import com.moengage.core.internal.MoEConstants.USER_ATTRIBUTE_USER_FIRST_NAME
import com.moengage.core.internal.MoEConstants.USER_ATTRIBUTE_USER_LAST_NAME
import com.moengage.core.internal.MoEConstants.USER_ATTRIBUTE_USER_MOBILE
import com.moengage.core.internal.MoEConstants.USER_ATTRIBUTE_USER_NAME
import com.moengage.firebase.MoEFireBaseHelper
import javax.inject.Inject

class HomeLoadsFragment : HomeLoadsTruckBaseFragment<FragmentHomeLoadsBinding, HomeLoadsViewModel>(),
    HomeLoadsRVAdapterInterface, TitleProvider {

  var _title: String = "Load Request"

  override val title: CharSequence
    get() = _title

  @Inject lateinit var dialogUtils: DialogUtils
  @Inject lateinit var fcmUtils: FCMUtils
  @Inject lateinit var userPrefs: UserPrefs

  private val MINIMUM = 25
  var scrollDist = 0
  var visible = false
  var demandType: String = ""
  var isInternal = false
  var pos = 0
  var oldAmount:Double?=0.0
  var currSize:Int? = null
  var itemDeleted:Boolean = false
  var reviseInitiated:Boolean=false

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

    demandType = userPrefs.demandType

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

    binding.rvLoads.setItemAnimator(null);

    binding.editStickySearch.setOnClickListener {
      handleAction(
              HomeTripsSearchAction_Search, HomeLoadsSearchItem()
      )
    }

    binding.routesBanner.setOnClickListener {
      context?.let {
        userPrefs.setPreviousScreen(this.javaClass.name)
        startActivity(userRoutesIntent(it))
      }
    }

    viewModel.progressLiveData.reobserve(viewLifecycleOwner, ProgressObserver())

    viewModel.userLoadsData.reobserve(viewLifecycleOwner, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })
    viewModel.userLoadsDataFetch.reobserve(viewLifecycleOwner, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })
    viewModel.loadsCountLiveData.reobserve(viewLifecycleOwner, Observer {
      userPrefs.loadCount = it.toString()
      _title = when (it) {
        0, null -> getString(string.label_load_request)
        else -> "${getString(string.label_load_request)}($it)"
      }
    })

    viewModel.routesLiveData.reobserve(viewLifecycleOwner, Observer {
      when (it) {
        false -> binding.rvLoads.apply {
          this@HomeLoadsFragment.visible = true
          binding.routesBanner.visibility = View.GONE
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
        analyticsUtil.moEngageTrackEvent(
          EVENT_LOADFEED_BID_REVISE_INITIATED,
            mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE),
            mutableListOf(data?.transactionId.toString(),data?.numBids.toString(),data?.lowestBid.toString())
        )
        reviseInitiated=true
        BidDetailsCreateEditDialog(
                context!!, data!!, data!!.transactionBid, viewModel, it.second, analyticsUtil, userPrefs, "load_screen"
        ).show()
      }
    })

    viewModel.bidsActionLiveData.reobserve(viewLifecycleOwner, Observer {
      uiUtils.toggleKeyboard()
              .apply {
                when {
                  it != null -> {
                    val data = adapter.itemsList()[it.first].data as? HomeBidsRequestItemData
                     oldAmount= data?.transactionBid?.bidAmount
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
      if (it != null) {
        val data = adapter.itemsList()[it.first].data as? HomeBidsRequestItemData
        var oldAmountbids=""
        var bidAmount =""
        var lowestBid =0.0
        var numBids=0
        var oldBidCount=0
        var newBidCount =0
        var oldUserLowestAmount =0.0
        var expectedArrivalPickup=""
        if(data!=null) {
          if(data.bulkTransactionBids.isNotEmpty()) {
              oldBidCount=data.bulkTransactionBids.size
            for (transactionBid in data!!.bulkTransactionBids) {
              oldUserLowestAmount= transactionBid.bidAmount
              if(oldUserLowestAmount>transactionBid.bidAmount){
                oldUserLowestAmount = transactionBid.bidAmount
              }
              if (oldAmountbids.isNullOrEmpty()) {
                oldAmountbids = transactionBid.bidAmount.toString()
              } else {
                oldAmountbids = oldAmountbids + "," + transactionBid.bidAmount.toString()
              }
            }
          }
        }
        data?.bulkTransactionBids = it.second
        if(data!=null &&data?.bulkTransactionBids.isNotEmpty()) {
          newBidCount = data?.bulkTransactionBids.size
        for(transactionBid in data!!.bulkTransactionBids){
          if(data?.lowestBid!=null){
           if(data?.lowestBid!!>transactionBid.bidAmount){
             lowestBid = transactionBid.bidAmount
            }else{
              if(data?.lowestBid==oldUserLowestAmount){
                lowestBid = transactionBid.bidAmount
              }else{
                lowestBid = data.lowestBid!!
              }

            }
          }else{
            lowestBid = transactionBid.bidAmount
          }
          if(bidAmount.isNullOrEmpty()){
            bidAmount=transactionBid.bidAmount.toString()
          }else {
            bidAmount = bidAmount +","+ transactionBid.bidAmount.toString()
          }
          if(expectedArrivalPickup.isNullOrEmpty()){
            expectedArrivalPickup=transactionBid.expectedArrivalTimePickupRemark.toString()
          }else {
            expectedArrivalPickup = expectedArrivalPickup + transactionBid.expectedArrivalTimePickupRemark.toString()
          }
        }
          if(data?.numBids==0){
            numBids = newBidCount
          }else{
            numBids = data?.numBids-oldBidCount+newBidCount
          }
          data.numBids = numBids
          data.lowestBid = lowestBid
        }
        adapter.notifyItemChanged(it.first)
        if(!reviseInitiated) {
          analyticsUtil.moEngageTrackEvent(
              EVENT_LOADFEED_BID_SUBMIT,
              mutableListOf(
                  PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_USER_BID_VALUE,
                  PROPERTY_VEHICLE_REPORTING_DATE_TIME
              ),
              mutableListOf(
                  data?.transactionId ?: "",numBids.toString(), bidAmount ?: "",
                  expectedArrivalPickup
              )
          )
        }else{
          analyticsUtil.moEngageTrackEvent(
              EVENT_LOADFEED_BID_REVISE_SUBMITTED,
              mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE,
                  PROPERTY_USER_BID_VALUE_OLD, PROPERTY_USER_BID_VALUE_NEW),
              mutableListOf(data?.transactionId?:"",numBids.toString()?:"",lowestBid.toString()?:" ",oldAmountbids,bidAmount)
          )
          reviseInitiated=false
        }
      }
    })

    viewModel.editBulkLiveData.reobserve(viewLifecycleOwner, Observer {
      if (it.first == 10) {
        Toast.makeText(context, "Bids Created Successfully", Toast.LENGTH_SHORT).show()
      }
      if (it.first == 20) {
        Toast.makeText(context, "Bids Updated Successfully", Toast.LENGTH_SHORT).show()
      }
      if (it.first == 30) {
        Toast.makeText(context, "Bids Deleted Successfully", Toast.LENGTH_SHORT).show()
      }
      if (viewModel.editFlg[0] && viewModel.editFlg[1] && viewModel.editFlg[2]) {
        viewModel.transactionBidForBulk(it.second, pos)
        viewModel.editFlg = mutableListOf(false, false, false)
      }
    })

    viewModel.lowestBidLiveData.reobserve(viewLifecycleOwner, Observer {
      uiUtils.hideProgress()
      if (it != null) {
        var data = it.second
        if(!reviseInitiated) {
          analyticsUtil.moEngageTrackEvent(
            EVENT_LOADFEED_BID_SUBMIT,
            mutableListOf(
              PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_USER_BID_VALUE,
              PROPERTY_VEHICLE_REPORTING_DATE_TIME
            ),
            mutableListOf(
              data?.transactionId ?: "", data?.numBids.toString(),
              data?.bidAmountValue() ?: "",
              data?.transactionBid?.expectedArrivalTimePickupRemark ?: ""
            )
          )
        }else{
          analyticsUtil.moEngageTrackEvent(
            EVENT_LOADFEED_BID_REVISE_SUBMITTED,
            mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE,
              PROPERTY_USER_BID_VALUE_OLD, PROPERTY_USER_BID_VALUE_NEW),
            mutableListOf(data?.transactionId?:"",data?.numBids.toString()?:"",data?.lowestBid.toString()?:" ",oldAmount.toString()?:"",data?.bidAmountValue().toString()?:"")
          )
          reviseInitiated=false
        }
        if (it.second.oneVisibility() == View.VISIBLE || it.second.twoVisibility() == View.VISIBLE) {
          analyticsUtil.trackEvent(
                  EVENT_BID_INLINE_PROMPT,
                  mutableListOf(PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID, PROPERTY_DEMAND_TYPE, PROPERTY_OVERALL_PERFORMANCE),
                  mutableListOf(userPrefs.userId(), it.second.key(), userPrefs.demandType, userPrefs.userPerformance)
          )

        } else if (it.second.threeVisibility() == View.VISIBLE || it.second.fourVisibility() == View.VISIBLE) {
          analyticsUtil.trackEvent(
                  EVENT_BID_REVISE_PROMPT,
                  mutableListOf(PROPERTY_USER_ID, PROPERTY_TRANSACTION_ID, PROPERTY_DEMAND_TYPE, PROPERTY_OVERALL_PERFORMANCE),
                  mutableListOf(userPrefs.userId(), it.second.key(), userPrefs.demandType, userPrefs.userPerformance)
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
      userPrefs.moengageFcmTokenGenerated = true
      fcmUtils.generateToken {
        if (it.isNotNullOrEmpty()) {
          activity?.let { it1 -> MoEFireBaseHelper.getInstance().passPushToken(it1.applicationContext,it) }
          viewModel.updateFCMToken(it)
        }
      }
    }

    if (!viewModel.isMoengageFCMTokenGenerated()) {
      fcmUtils.generateToken {
        if (it.isNotNullOrEmpty()) {
          activity?.let { it1 -> MoEFireBaseHelper.getInstance().passPushToken(it1.applicationContext,it) }
          viewModel.updateFCMToken(it)
        }
      }
    }

    refreshData()

    viewModel.updateUserAppAccess()

    viewModel.truckGetLiveData.reobserve(viewLifecycleOwner, Observer {
      uiUtils.hideProgress()
      if (it != null) {
        val pageTitle = if (it.second.bulkTransactionBids != null && it.second.bulkTransactionBids.isNotEmpty()) "EDIT BIDS" else "PLACE BIDS"
        if (it.second.bulkTransactionBids != null && it.second.bulkTransactionBids.isNotEmpty()) {
          analyticsUtil.moEngageTrackEvent(
            EVENT_LOADFEED_BID_REVISE_INITIATED,
              mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE),
              mutableListOf(
                  it.second.transactionId.toString(), it.second?.numBids.toString(),
                  it.second?.lowestBid.toString()
              )
          )
          reviseInitiated=true
        }else{
          analyticsUtil.moEngageTrackEvent(
            EVENT_LOADFEED_BID_INITIATE,
            mutableListOf(PROPERTY_ORDER_ID, PROPERTY_ORDER_RANK, PROPERTY_ORDER_COUNT),
            mutableListOf( it.second.transactionId?:"",(pos- STATIC_ITEM_LIST-((pos- STATIC_ITEM_LIST)/HomeLoadsAddTruckItemDataConfig)).toString(),viewModel.total.toString())
          )
          reviseInitiated=false
        }
        if (it.second.truckUUID != null) {
          try {
            BulkBidDetailsCreateEditDialog(
                    context!!,
                    it.second,
                    it.second.bulkTransactionBids,
                    it.first,
                    viewModel,
                    it.second.unAllocatedVolume!!,
                    pos,
                    analyticsUtil,
                    userPrefs,
                    "load_screen",
                    pageTitle
            ).show()
          } catch (e: Exception) {

          }
        } else {
          Toast.makeText(context, "No Vehicle Types Found", Toast.LENGTH_SHORT).show()
        }
      }
    })
  }



  override fun onResume() {
    super.onResume()
    viewModel.paginateCount = 0
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
    viewModel.fetchUserTransactions(false, demandType, isInternal)
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
        analyticsUtil.moEngageTrackEvent(
            EVENT_HOME_ORDER_CARD_CLICK,
            mutableListOf(PROPERTY_ORDER_ID, PROPERTY_ORDER_RANK, PROPERTY_ORDER_COUNT),
            mutableListOf(data.transactionId?:" ",(orderRank- STATIC_ITEM_LIST-((orderRank- STATIC_ITEM_LIST).div(HomeLoadsAddTruckItemDataConfig))).toString(),viewModel.total.toString())
            )
        analyticsUtil.trackEvent(
                EVENT_LIST_ITEM,
                mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
                mutableListOf(VALUE_LOAD, data.transactionId ?: "")
        )
        context?.let {
          userPrefs.setPreviousScreen(this.javaClass.name)
          startActivity(bidDetailsIntent(data.key(), it, if (data.isDMTIndent()) "dmt" else "")) }
      }

      HomeLoadsSearchAction_Search -> {
        context?.let {
          analyticsUtil.moEngageTrackEvent( EVENT_HOME_SEARCH_INITIATE,
            mutableListOf(PROPERTY_ORDER_COUNT),
            mutableListOf(viewModel.total.toString()))
          userPrefs.setPreviousScreen(this.javaClass.name)
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
          userPrefs.setPreviousScreen(this.javaClass.name)
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
          userPrefs.setPreviousScreen(this.javaClass.name)
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

        if (isInternal) {
          isInternal = false
          demandType = userPrefs.demandType
        } else {
          isInternal = true
          demandType = "Internal"
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
                mutableListOf(PROPERTY_USER_ID, PROPERTY_DEMAND_TYPE),
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
        val exclude_truck_str = exclude_truck_types.joinToString(separator = ",") { it }
        viewModel.filterVehicleType = null
        viewModel.fetchUserTransactions(false, demandType, isInternal, true, exclude_truck_str)
      }

      HomeLoadsPriorityAction -> {
        analyticsUtil.trackEvent(
                EVENT_BANNER_CLICK_TOP,
                mutableListOf(PROPERTY_USER_ID, PROPERTY_PAGE_NAME),
                mutableListOf(userPrefs.userId(), "loads_screen")
        )
        when (viewModel.userPrefs.canBid()) {
          APPROVED -> {
            analyticsUtil.moEngageTrackEvent(
              EVENT_ADD_TRUCK_INITIATE,
              mutableListOf(PROPERTY_SOURCE),
              mutableListOf(VALUE_BANNER)
            )
            showAddTruckDialog(mutableListOf(
                TruckFrequentItem("closed", "32FTMXL", 14.0, 14.0, 18.0, "FTL"),
                TruckFrequentItem("open", "10_TYRE", 16.0, 15.0, 20.0, "PMT"),
                TruckFrequentItem("open", "12_TYRE", 21.0, 20.0, 25.0, "PMT")
        ), VALUE_ADD_TRUCK_TOP_BANNER)}
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
      HomeLoadsShareRateAction ->{
        analyticsUtil.trackEvent(
            EVENT_CLICKED_PRICE_BANNER,
            mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO),
            mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"")
        )
        navigationUtils.navigate(ShareRateGetRewardsActivity::class.java)
      }

      HomeLoadsBannerAction -> {
        analyticsUtil.trackEvent(
                EVENT_BANNER_CLICK_SCROLL,
                mutableListOf(PROPERTY_USER_ID, PROPERTY_PAGE_NAME),
                mutableListOf(userPrefs.userId(), "loads_screen")
        )
        when (viewModel.userPrefs.canBid()) {
          APPROVED -> {
            showAddTruckDialog(mutableListOf(TruckFrequentItem("closed", "32FTMXL", 14.0, 14.0, 18.0, "FTL"),
                    TruckFrequentItem("open", "10_TYRE", 16.0, 15.0, 20.0, "PMT"),
                    TruckFrequentItem("open", "12_TYRE", 21.0, 20.0, 25.0, "PMT")
            ), VALUE_ADD_TRUCK_SCROLL_BANNER)
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
    }
  }

  private fun showAddTruckDialog(items: List<TruckFrequentItem>, source: String) {
    val dialog = Dialog(context!!)
    val bindingDialog= DialogBottomTruckAddBinding.inflate(layoutInflater)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(bindingDialog.root)

    bindingDialog.containerTrucks.removeAllViews()
    items.forEachIndexed { index, item ->
      val itemBinding = createTruckFrequentItem(bindingDialog)
      itemBinding.data = item
      itemBinding.root.setOnClickListener{
        context?.let { startActivityForResult(truckIntent(context!!, item.truckType, item.truckSize, item.capacity, item.minCap, item.maxCap, item.sourcedAs, source = source), REQCODE_ADD_TRUCK) }
        dialog.dismiss()
      }

      bindingDialog.containerTrucks.addView(itemBinding.root, index)
    }
    bindingDialog.closeBtn.setOnClickListener{
      dialog.dismiss()
    }

    bindingDialog.addTruckLayout.setOnClickListener{
      userPrefs.setPreviousScreen(this.javaClass.name)
      context?.let { startActivityForResult(truckIntent(context!!, source = source), REQCODE_ADD_TRUCK) }
      dialog.dismiss()
    }

    dialog.show()
    dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
    dialog.window!!.setGravity(Gravity.BOTTOM)
  }

  private fun showVehicleFilterDialog(){
    lateinit var dialog: AlertDialog

    // Initialize an array of vehicles
    val arrayVehicle = arrayOf("open", "closed", "trailer", "all")

    val arrayChecked = booleanArrayOf(false, false, false, false)

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

      viewModel.vehicleStr = filterVehicleTypes.joinToString(separator = ",") {it}
      viewModel.filterVehicleType = true
      refreshData()
    }

    builder.setNegativeButton("Cancel") { _, _ ->
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
            pos = position
            val data = item.data as HomeBidsRequestItemData
            var eventPos=pos- STATIC_ITEM_LIST
            if (data.isDMTIndent()) {
              uiUtils.showProgress()
              viewModel.fetchTruckType(data)
            } else {
              item.data.let {
                if(it.transactionBid==null){
                  analyticsUtil.moEngageTrackEvent(
                      EVENT_LOADFEED_BID_INITIATE,
                      mutableListOf(PROPERTY_ORDER_ID, PROPERTY_ORDER_RANK, PROPERTY_ORDER_COUNT),
                      mutableListOf(data.transactionId?:"",(eventPos-(eventPos/HomeLoadsAddTruckItemDataConfig)).toString(),viewModel.total.toString())
                  )
                  reviseInitiated=false
                }else{
                  analyticsUtil.moEngageTrackEvent(
                      EVENT_LOADFEED_BID_REVISE_INITIATED,
                      mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE),
                      mutableListOf(
                          data.transactionId.toString(), data?.numBids.toString(),
                          data?.lowestBid.toString()
                      )
                  )
                  reviseInitiated=true
                }
                BidDetailsCreateEditDialog(
                        context!!, it, it.transactionBid, viewModel, position, analyticsUtil, userPrefs, "load_screen"
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

  override fun deleteItem(item: BaseHomeLoadsRVAdapterItem<*>, position: Int) {
    binding.rvLoads.post(Runnable {
      val bidData = item.data as HomeBidsRequestItemData
      currSize = currSize?.minus(1)
      itemDeleted = true
      adapter.operation(listOf(Pair(HomeLoadsRequestItem(bidData), DataRVAdapterOperationType.Remove)))
      adapter.notifyDataSetChanged()
    })
  }

  override fun fetchCurrSize(): Int? {
    return currSize
  }

  override fun itemDeleted(): Boolean {
    return itemDeleted
  }

  override fun itemDeleted(cp: Boolean) {
    itemDeleted = cp
  }

  override fun updateCurrSize(size: Int) {
    currSize = size
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
    override fun loadMore() = viewModel.fetchUserTransactions(true, demandType, isInternal)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }

  /** Create new frequent truck item*/
  private fun createTruckFrequentItem(binding: DialogBottomTruckAddBinding)=
    ViewFrequentTruckItemBinding.inflate(layoutInflater, binding.containerTrucks, false)
}
const val STATIC_ITEM_LIST = 3