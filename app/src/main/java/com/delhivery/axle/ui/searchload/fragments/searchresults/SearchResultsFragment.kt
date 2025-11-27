package com.delhivery.axle.ui.searchload.fragments.searchresults

import android.R.layout
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.ContractType
import com.delhivery.axle.api.repository.UserTripsLoadLimit
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_AcceptBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_NavigationMap
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.bids.HomeBidsSearchSpinnerItemData
import com.delhivery.axle.data.home.bids.SUB_REQUEST_TYPE_INTRACITY
import com.delhivery.axle.databinding.FragmentSearchResultsBinding
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.biddetails.AcceptAdhocIntracityBidBottomDialog
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialog
import com.delhivery.axle.ui.biddetails.BulkBidDetailsCreateEditDialog
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.contractDetails.contractDetailsIntent
import com.delhivery.axle.ui.dialogs.BidConfirmReviseDialog
import com.delhivery.axle.ui.home.activity.home.orderRank
import com.delhivery.axle.ui.home.fragments.bids.SearchLoadWarningItem_NoLoad
import com.delhivery.axle.ui.searchload.fragments.ProgressSearchLoadAction
import com.delhivery.axle.ui.searchload.fragments.SearchLoadBaseFragment
import com.delhivery.axle.utils.BidSuccessInterface
import com.delhivery.axle.utils.DialogUtils
import com.delhivery.axle.utils.EVENT_LIST_ITEM
import com.delhivery.axle.utils.EVENT_PAGE_CONTRACT_SEARCH_RESULTS_NO_ORDERS
import com.delhivery.axle.utils.EVENT_PAGE_CONTRACT_SEARCH_RESULTS_WITH_ORDERS
import com.delhivery.axle.utils.EVENT_PAGE_LOAD_SEARCH_RESULTS_NO_ORDERS
import com.delhivery.axle.utils.EVENT_PAGE_LOAD_SEARCH_RESULTS_WITH_ORDERS
import com.delhivery.axle.utils.EVENT_SEARCH_LOAD
import com.delhivery.axle.utils.EVENT_SEARCH_RESULTS_ORDER_CARD_CLICK
import com.delhivery.axle.utils.EVENT_SEARCH_RESULT_BID_INITIATE
import com.delhivery.axle.utils.EVENT_SEARCH_RESULT_BID_REVISE_INITIATED
import com.delhivery.axle.utils.EVENT_SEARCH_RESULT_BID_REVISE_SUBMITTED
import com.delhivery.axle.utils.EVENT_SEARCH_RESULT_BID_SUBMIT
import com.delhivery.axle.utils.EVENT_SEARCH_SAVED_LOAD
import com.delhivery.axle.utils.PROPERTY_BID_COUNT
import com.delhivery.axle.utils.PROPERTY_DESTINATION
import com.delhivery.axle.utils.PROPERTY_NUM_RESULTS
import com.delhivery.axle.utils.PROPERTY_ORDER_COUNT
import com.delhivery.axle.utils.PROPERTY_ORDER_ID
import com.delhivery.axle.utils.PROPERTY_ORDER_LOWEST_BID_VALUE
import com.delhivery.axle.utils.PROPERTY_ORDER_RANK
import com.delhivery.axle.utils.PROPERTY_ORIGIN
import com.delhivery.axle.utils.PROPERTY_SEARCH_BODY_TYPE
import com.delhivery.axle.utils.PROPERTY_SEARCH_DESTINATION_CITY
import com.delhivery.axle.utils.PROPERTY_SEARCH_ORIGIN_CITY
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_TYPE
import com.delhivery.axle.utils.PROPERTY_TRUCK_TYPE
import com.delhivery.axle.utils.PROPERTY_USER_BID_VALUE
import com.delhivery.axle.utils.PROPERTY_USER_BID_VALUE_NEW
import com.delhivery.axle.utils.PROPERTY_USER_BID_VALUE_OLD
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.PROPERTY_VEHICLE_REPORTING_DATE_TIME
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.VALUE_LOAD
import com.delhivery.axle.utils.VALUE_SEARCH_LISITING
import com.delhivery.axle.utils.extensions.centerX
import com.delhivery.axle.utils.extensions.centerY
import com.delhivery.axle.utils.extensions.isNotEmpty
import com.delhivery.axle.utils.extensions.setHintColor
import com.delhivery.axle.utils.extensions.setup
import com.delhivery.axle.utils.prefs.APPROVED
import com.delhivery.axle.utils.prefs.DISABLED
import com.delhivery.axle.utils.prefs.UNAPPROVED
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * Search results screen
 */
class SearchResultsFragment : SearchLoadBaseFragment<FragmentSearchResultsBinding, SearchResultsViewModel>(),
    SearchLoadsRVAdapterInterface,BidSuccessInterface {

  companion object {
    val _instance: SearchResultsFragment by lazy { SearchResultsFragment() }
  }

  override fun getViewModelClass() = SearchResultsViewModel::class.java

  override fun layoutId() = R.layout.fragment_search_results

  private var saveToHistory: Boolean = false

  @Inject lateinit var dialogUtils: DialogUtils

  @Inject lateinit var userPrefs: UserPrefs
  var pos = 0
  var oldAmount:Double?=0.0
  var reviseInitiated:Boolean=false
  var isContract = false
  var isIntraCity  =false

  private lateinit var origin: CityModel
  private var destination: CityModel? = null
  private var requestType: String? =  ""
  private var contractType:String? = ""
  private var type: String?= ""
  private var displayName: String?= ""
  private var status: String?=""
  private var isFlexible: Boolean?= null
  private var includeFlexibleContracts: Boolean?= null

  private val _adapter by lazy {
    SearchLoadsRVAdapter(this)
  }

  private val _scrollListener by lazy {
    SearchResultsRVScrollListener()
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    setupSpinners()

    /* setup rv */
    binding.rv.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
      adapter = _adapter
      addOnScrollListener(_scrollListener)
      addOnScrollListener(PaginationInterface())
    }

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      refreshData()
    }

    viewModel.reviseBidLiveData.reobserve(viewLifecycleOwner, Observer {
      if (it.first) {
        val data = _adapter.itemsList()[it.second].data as? HomeBidsRequestItemData
        reviseInitiated=true
        BidDetailsCreateEditDialog(
          requireContext(), data!!, data!!.transactionBid, viewModel, it.second, analyticsUtil, userPrefs, "search_screen"
        ).show()
      }
    })

    viewModel.bidsActionLiveData.observe(viewLifecycleOwner, Observer {
      uiUtils.toggleKeyboard()
          .apply {
            when {
              it != null && it.first !=null  -> {
                val data = _adapter.itemsList()[it.first].data as? HomeBidsRequestItemData
                oldAmount= data?.transactionBid?.bidAmount
                (_adapter.itemsList()[it.first].data as HomeBidsRequestItemData).transactionBid =
                  it.second
                _adapter.notifyItemChanged(it.first)
                if (data != null) {
                  uiUtils.showProgress()
                  viewModel.fetchLowestBid(data, it.first)
                }

              }
            }
          }
    })



    viewModel.acceptBidLiveData.reobserve(viewLifecycleOwner, Observer {
          if(it!=null){
            dialogUtils.showSuccessBidDialog(this,
              resources.getString(R.string.details_submitted_successfully),
              null
            )
            refreshData()
          }
        })

    viewModel.bulkBidActionLiveData.reobserve(viewLifecycleOwner, Observer {
      if(it != null){
        val data = _adapter.itemsList()[it.first].data as? HomeBidsRequestItemData
        var oldAmountbids=""
        var bidAmount =""
        var lowestBid =0.0
        var numBids=0
        var oldBidCount=0
        var newBidCount =0
        var oldUserLowestAmount =0.0
        var expectedArrivalPickup=""
        if(data!=null&&data?.bulkTransactionBids.isNotEmpty()) {
          oldBidCount=data.bulkTransactionBids.size
        for(transactionBid in data!!.bulkTransactionBids){
          oldUserLowestAmount= transactionBid.bidAmount
          if(oldUserLowestAmount>transactionBid.bidAmount){
            oldUserLowestAmount = transactionBid.bidAmount
          }
          if(oldAmountbids.isNullOrEmpty()){
            oldAmountbids= transactionBid.bidAmount.toString()
          }else {
            oldAmountbids = oldAmountbids + ","+transactionBid.bidAmount.toString()
          }
        }
        }
        data?.bulkTransactionBids = it.second
        if(data!=null && data?.bulkTransactionBids.isNotEmpty()) {
          newBidCount=data.bulkTransactionBids.size
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
            numBids = data?.numBids!!-oldBidCount+newBidCount
          }
          data.numBids = numBids
          data.lowestBid = lowestBid
        }
        _adapter.notifyItemChanged(it.first)
        if(!reviseInitiated) {
          analyticsUtil.moEngageTrackEvent(
            EVENT_SEARCH_RESULT_BID_SUBMIT,
            mutableListOf(
              PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_USER_BID_VALUE,
              PROPERTY_VEHICLE_REPORTING_DATE_TIME
            ),
            mutableListOf(
              data?.transactionId ?: "", data?.numBids.toString(), bidAmount ?: "",
              expectedArrivalPickup
            )
          )
        }else{
          analyticsUtil.moEngageTrackEvent(
            EVENT_SEARCH_RESULT_BID_REVISE_SUBMITTED,
            mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE,
              PROPERTY_USER_BID_VALUE_OLD, PROPERTY_USER_BID_VALUE_NEW),
            mutableListOf(data?.transactionId?:"",data?.numBids.toString()?:"",lowestBid.toString()?:" ",oldAmountbids,bidAmount)
          )
          reviseInitiated=false
        }
      }
    })
    viewModel.lowestBidLiveData.reobserve(viewLifecycleOwner, Observer {
      uiUtils.hideProgress()
      if (it != null) {
        var data = it.second
        if(!reviseInitiated) {
          analyticsUtil.moEngageTrackEvent(
            EVENT_SEARCH_RESULT_BID_SUBMIT,
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
            EVENT_SEARCH_RESULT_BID_REVISE_SUBMITTED,
            mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE,
              PROPERTY_USER_BID_VALUE_OLD, PROPERTY_USER_BID_VALUE_NEW),
            mutableListOf(data?.transactionId?:"",data?.numBids.toString()?:"",data?.lowestBid.toString()?:" ",oldAmount.toString()?:"",data?.bidAmountValue().toString()?:"")
          )
          reviseInitiated=false
        }

        BidConfirmReviseDialog(
          requireContext(), it.second, viewModel, it.first
        ).show()
      }
    })
    viewModel.editBulkLiveData.reobserve(viewLifecycleOwner, Observer {
      if(it.first == 10){
        Toast.makeText(context,"Bids Created Successfully", Toast.LENGTH_SHORT).show()
      }
      if(it.first == 20){
        Toast.makeText(context,"Bids Updated Successfully", Toast.LENGTH_SHORT).show()
      }
      if(it.first == 30){
        Toast.makeText(context,"Bids Deleted Successfully", Toast.LENGTH_SHORT).show()
      }
      if(viewModel.editFlg[0] &&  viewModel.editFlg[1] && viewModel.editFlg[2]){
        viewModel.transactionBidForBulk(it.second, pos)
        viewModel.editFlg = mutableListOf(false, false, false)
      }
    })


    viewModel.truckGetLiveData.reobserve(viewLifecycleOwner, Observer {
      uiUtils.hideProgress()
      if(it!= null ){
        val pageTitle = if(it.second.bulkTransactionBids!= null && it.second.bulkTransactionBids.isNotEmpty()) "EDIT BIDS" else "PLACE BIDS"
        if (it.second.bulkTransactionBids != null && it.second.bulkTransactionBids.isNotEmpty()) {
          analyticsUtil.moEngageTrackEvent(
              EVENT_SEARCH_RESULT_BID_REVISE_INITIATED,
              mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE),
              mutableListOf(
                  it.second.transactionId.toString(), it.second?.numBids.toString(),
                  it.second?.lowestBid.toString()
              )
          )
          reviseInitiated =true
        }else{
          analyticsUtil.moEngageTrackEvent(
            EVENT_SEARCH_RESULT_BID_INITIATE,
            mutableListOf(PROPERTY_ORDER_ID, PROPERTY_ORDER_RANK, PROPERTY_ORDER_COUNT),
            mutableListOf( it.second.transactionId?:"",pos.toString(),viewModel.total.toString())
          )
          reviseInitiated=false
        }
        if(it.second.truckUUID != null) {
          BulkBidDetailsCreateEditDialog(requireContext(), it.second, it.second.bulkTransactionBids, it.first, viewModel, it.second.unAllocatedVolume!!,
            pos, analyticsUtil, userPrefs, "load_screen", pageTitle).show()
        }
        else{
          Toast.makeText(context, "No Vehicle Types Found",Toast.LENGTH_SHORT).show()
        }
      }
    })

    viewModel.dataLoadingLiveData.reobserve(viewLifecycleOwner) {
      isLoadingData = it == true
    }

    viewModel.searchResults
      .reobserve(this, SearchResultsObserver())
  }

  private fun refreshData() {
    _adapter.resetStaticData(requestType?:"load")
    viewModel.searchLoad(false, origin, destination, type, displayName, status, requestType, contractType, isFlexible =isFlexible,includeFlexibleContracts=includeFlexibleContracts)
  }

  private fun setupSpinners() {
    binding.spinnerTruckType.isEnabled = false
    binding.spinnerTruckType.isClickable = false

    binding.spinnerTruckDisplayName.isEnabled = false
    binding.spinnerTruckDisplayName.isClickable = false
    binding.spinnerTruckType.setup(R.array.array_truck_type) {  p, v -> }
    }

  /**
   * Search with query params
   */
  fun search(
    origin: CityModel,
    destination: CityModel?,
    type: String?,
    displayName: String?,
    status: String?,
    saveToHistory: Boolean,
    requestType:String?,
    contractType:String?,
    truckDisplayNames: ArrayList<String>?,
    progress: Boolean = true,
    isFlexible:Boolean? = null,
    includeFlexibleContracts:Boolean?=null
  ) {
    saveQueryParams(origin, destination, type, displayName, status, requestType, contractType, saveToHistory,isFlexible,includeFlexibleContracts)
    /* clear and add first dummy item */
    _adapter.clearItems()
    if(contractType==ContractType.INTRACITY.type)
      _adapter.operation(SearchLoadsSearchSpinnerItem(data= HomeBidsSearchSpinnerItemData(true)),Add)
    else
      _adapter.operation(SearchLoadsSearchSpinnerItem(), Add)
    /* show progress if needed */
    if (progress)
      action(ProgressSearchLoadAction(true, if(isContract)"Searching contracts" else "Searching loads"))
    binding.origin = origin
    binding.destination = destination
    val pos = when (type) {
      "Closed" -> 1
      "Open" -> 2
      "Trailer" -> 3
      else -> 0
    }
    binding.spinnerTruckDisplayName.apply {
      truckDisplayNames?.let { setTruckDisplayAdapter(it) }
    }
    binding.spinnerTruckType.setSelection(pos, true)
    truckDisplayNames?.indexOf(displayName)
      ?.let { binding.spinnerTruckDisplayName.setSelection(it) }
    isContract = contractType!=null
    isIntraCity = contractType!=null && contractType==ContractType.INTRACITY.type
    binding.isIntraCityContract = isIntraCity
    if(isFlexible==null && isIntraCity){
      binding.checkBoxFixedIntracity.isChecked = true
      binding.checkBoxFlexibleIntracity.isChecked =true
    }else{
      binding.checkBoxFixedIntracity.isChecked = isFlexible==false
      binding.checkBoxFlexibleIntracity.isChecked =isFlexible==true
    }

    viewModel.searchLoad(false, origin, destination, type,displayName, status,requestType,contractType,isFlexible,includeFlexibleContracts)
  }

  /**
   * Save the query params to be used during pagination
   */
  private fun saveQueryParams(
    origin: CityModel,
    destination: CityModel?,
    type: String?,
    displayName: String?,
    status: String?,
    requestType: String?,
    contractType: String?,
    saveToHistory: Boolean,
    isFlexible:Boolean?,
    includeFlexibleContracts: Boolean?
  ) {
    this.origin = origin
    this.destination = destination
    this.type = type
    this.displayName = displayName
    this.status = status
    this.requestType = requestType
    this.contractType = contractType
    this.saveToHistory = saveToHistory
    this.isFlexible = isFlexible
    this.includeFlexibleContracts = includeFlexibleContracts
  }

  private fun AppCompatSpinner.setTruckDisplayAdapter(truckDisplayNames: ArrayList<String>) {
    val truckDisplayAdapter =
      ArrayAdapter(this.context!!, layout.simple_spinner_item, truckDisplayNames).apply {
        setDropDownViewResource(layout.simple_spinner_dropdown_item)
        onItemSelectedListener = object : OnItemSelectedListener {
          override fun onItemSelected(
            p0: AdapterView<*>?,
            p1: View?,
            p2: Int,
            p3: Long
          ) {
            setHintColor(getItemAtPosition(p2).toString())
          }

          override fun onNothingSelected(parent: AdapterView<*>?) {
            return
          }
        }
      }
    adapter = truckDisplayAdapter
  }

  override fun handleAction(
    actionId: String,
    item: BaseSearchLoadsRVAdapterItem<*>
  ) {
    when (actionId) {
      HomeBidsRequestAction_ViewDetails -> {
        val _item = item.data as HomeBidsRequestItemData
        if (_item.isItContract()) {
          if (_item.transactionId != null) {
            userPrefs.setPreviousScreen(this.javaClass.name)
            startActivity(contractDetailsIntent(_item.transactionId, requireContext(), VALUE_SEARCH_LISITING))
          } else {
            Toast.makeText(context, "Not Found", Toast.LENGTH_SHORT).show()
          }
        } else {
        // Capture event
        analyticsUtil.moEngageTrackEvent(
          EVENT_SEARCH_RESULTS_ORDER_CARD_CLICK,
          mutableListOf(PROPERTY_ORDER_ID, PROPERTY_ORDER_RANK),
          mutableListOf(_item.transactionId ?: "", orderRank.toString())
        )
        analyticsUtil.moEngageTrackEvent(
          EVENT_LIST_ITEM,
          mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
          mutableListOf(VALUE_LOAD, _item.transactionId ?: "")
        )
        if(_item.subRequestType!= SUB_REQUEST_TYPE_INTRACITY)
        context?.let {
          userPrefs.setPreviousScreen(this.javaClass.name)
          startActivity(bidDetailsIntent(_item.key(), it, if (_item.isDMTIndent()) "dmt" else ""))
        }
      }
      }
    }
  }

  override fun handleAction(
    actionId: String,
    item: BaseSearchLoadsRVAdapterItem<*>,
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
                if(it.transactionBid==null){
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_SEARCH_RESULT_BID_INITIATE,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_ORDER_RANK, PROPERTY_ORDER_COUNT),
                    mutableListOf(data.transactionId?:"",pos.toString(),viewModel.total.toString())
                  )
                  reviseInitiated=false
                }else{
                  analyticsUtil.moEngageTrackEvent(
                    EVENT_SEARCH_RESULT_BID_REVISE_INITIATED,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE),
                    mutableListOf(
                      data.transactionId.toString(), data?.numBids.toString(),
                      data?.lowestBid.toString()
                    )
                  )
                  reviseInitiated=true
                }
                BidDetailsCreateEditDialog(
                  requireContext(), it, it.transactionBid, viewModel, position, analyticsUtil, userPrefs , "load_screen"
                ).show()
              }
            }
          }
          HomeBidsRequestAction_AcceptBid -> {
            pos = position
            val data = item.data as HomeBidsRequestItemData
            AcceptAdhocIntracityBidBottomDialog(
              requireContext(),
              position,
              data,
              viewModel,
              analyticsUtil,
              userPrefs,
              viewModel,
              null,
              SearchResultsFragment._instance,
              uiUtils
            ).show()

          }
          HomeBidsRequestAction_NavigationMap -> {
            pos = position
            val data = item.data as HomeBidsRequestItemData
            try {
              val gmmIntentUri = Uri.parse("geo:0,0?q=${data.pickupLocationCoordinates?.lon},${data.pickupLocationCoordinates?.lon}"+"(" + data.origin+ ")")
              val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
              mapIntent.setPackage("com.google.android.apps.maps")
              startActivity(mapIntent)
            } catch (e: Exception) {
              Toast.makeText(context, "Unable to open map", Toast.LENGTH_SHORT).show()
            }
          }
        }
      }
      UNAPPROVED -> {
        dialogUtils.showBasicConfirmDialog(
            R.string.title_dialog_supplier_not_approved,
            R.string.msg_dialog_supplier_not_approved,
            getString(R.string.label_call_us), getString(R.string.label_mail_us),
            { callHelpline() }, { sendMail() }
        )
      }
      DISABLED -> {
        dialogUtils.showBasicConfirmDialog(
            R.string.title_dialog_supplier_disabled,
            R.string.msg_dialog_supplier_disabled,
            getString(R.string.label_call_us), getString(R.string.label_mail_us),
            { callHelpline() }, { sendMail() }
        )
      }
    }
  }

  override fun deleteItem(item: BaseSearchLoadsRVAdapterItem<*>, position: Int) {
    binding.rv.post(Runnable {
      val bidData = item.data as  HomeBidsRequestItemData
      _adapter.operation(listOf(Pair(SearchLoadsRequestItem(bidData), DataRVAdapterOperationType.Remove)))
      _adapter.notifyDataSetChanged()
    })
  }

  /**
   * Search results observer
   */
  inner class SearchResultsObserver : Observer<List<Pair<BaseSearchLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>?> {
    override fun onChanged(t: List<Pair<BaseSearchLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>?) {
      //resetSpinnerContainer()
      /* hide progress */
        action(ProgressSearchLoadAction(false))
      /* show results */
      val event: String = when (saveToHistory) {
        true -> {
          EVENT_SEARCH_LOAD
        }
        false -> {
          EVENT_SEARCH_SAVED_LOAD
        }
      }

      val numResults: Int
      if (t == null) {
        numResults = 0
        //error
      } else {
        numResults = t.size
        _adapter.operation(t)
      }
      if(t==null || t.contains(Pair(SearchLoadWarningItem_NoLoad, Add))){
        analyticsUtil.moEngageTrackEvent(
            if(isContract)EVENT_PAGE_CONTRACT_SEARCH_RESULTS_NO_ORDERS else EVENT_PAGE_LOAD_SEARCH_RESULTS_NO_ORDERS,
            mutableListOf(PROPERTY_SEARCH_ORIGIN_CITY, PROPERTY_SEARCH_DESTINATION_CITY,
                PROPERTY_SEARCH_BODY_TYPE),
            mutableListOf(  binding.origin?.cityName() ?: "Anywhere",
                binding.destination?.cityName() ?: "Anywhere",
                if(isIntraCity)binding.spinnerTruckDisplayName.selectedItem?.toString() ?: "" else binding.spinnerTruckType.selectedItem.toString())
        )
      }else{
        analyticsUtil.moEngageTrackEvent(
            if(isContract) EVENT_PAGE_CONTRACT_SEARCH_RESULTS_WITH_ORDERS else EVENT_PAGE_LOAD_SEARCH_RESULTS_WITH_ORDERS,
            mutableListOf(PROPERTY_SEARCH_ORIGIN_CITY, PROPERTY_SEARCH_DESTINATION_CITY,
                PROPERTY_SEARCH_BODY_TYPE, PROPERTY_ORDER_COUNT),
            mutableListOf(  binding.origin?.cityName() ?: "Anywhere",
                binding.destination?.cityName() ?: "Anywhere",
              if(isIntraCity)binding.spinnerTruckDisplayName.selectedItem?.toString() ?: "" else binding.spinnerTruckType.selectedItem.toString(),
              numResults.toString())
        )
      }
      analyticsUtil.moEngageTrackEvent(
          event,
          mutableListOf(
              PROPERTY_USER_ID,
              PROPERTY_ORIGIN, PROPERTY_DESTINATION,
              PROPERTY_TRUCK_TYPE, PROPERTY_NUM_RESULTS
          ),
          mutableListOf(
              userPrefs.userId(),
              binding.origin?.cityName() ?: "Anywhere",
              binding.destination?.cityName() ?: "Anywhere",
              binding.spinnerTruckType.selectedItem.toString(),
              numResults.toString()
          )
      )
    }
  }

  /**
   * Reset spinner container
   */
  private fun resetSpinnerContainer() {
    binding.apply {
      _scrollListener.coordinateView(spinnerTruckType, viewHiddenIndicator, 0f)
      viewHiddenIndicator.alpha = 0f
      rv.scrollToPosition(0)
      containerSpinner.translationY = 0f
    }
  }

  inner class PaginationInterface: PaginationScrollListener(UserTripsLoadLimit){
    override fun loadMore()=viewModel.searchLoad(true, origin, destination, type, displayName, status, requestType, contractType,isFlexible,includeFlexibleContracts)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData

  }

  /**
   * Search results rv scroll listener
   */
  inner class SearchResultsRVScrollListener : OnScrollListener() {
    override fun onScrolled(
      recyclerView: androidx.recyclerview.widget.RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)
      binding.apply {
        val layoutManager =
          (recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager)
        val pos = layoutManager.findFirstVisibleItemPosition()
        val visibleHeight = viewHiddenIndicator.height * 3f
        val maxTranslationY = visibleHeight - containerSpinner.height

        containerSpinner.translationY = if (pos >= 1) {
          viewHiddenIndicator.alpha = 1f
          if(!isIntraCity){
            updateVisibility(spinnerTruckType, View.INVISIBLE)
          }
          maxTranslationY
        } else {
          val childView = recyclerView.findViewHolderForAdapterPosition(0)!!.itemView
          val childTop = childView.top * 1f
          val factor = Math.min(childTop / maxTranslationY, 1f)
          viewHiddenIndicator.alpha = factor
          coordinateView(spinnerTruckType, viewHiddenIndicator, factor)
          Math.max(maxTranslationY, childTop)
        }
      }
    }

    /**
     * Coordinate [view] with [target] as per factor
     */
    fun coordinateView(
      view: View,
      target: View,
      factor: Float
    ) {
      if(!isIntraCity)
        updateVisibility(view, View.VISIBLE)
      view.alpha = 1f - factor
      view.translationX = (target.centerX() - view.centerX()) * factor
      view.translationY = (target.centerY() - view.centerY()) * factor
    }

    /**
     * Update view visibility
     */
    private fun updateVisibility(
      view: View,
      visibility: Int
    ) {
      if (view.visibility != visibility) {
        view.visibility = visibility
      }
    }
  }

  override fun bidPlacedSuccess(success: Boolean) {
//    TODO("Not yet implemented")
  }
}