package com.delhivery.axle.ui.searchload.fragments.searchresults

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.FragmentSearchResultsBinding
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialog
import com.delhivery.axle.ui.biddetails.BulkBidDetailsCreateEditDialog
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.dialogs.BidConfirmReviseDialog
import com.delhivery.axle.ui.home.activity.home.orderRank
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsRequestItem
import com.delhivery.axle.ui.home.fragments.bids.SearchLoadWarningItem_NoLoad
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRequestItem
import com.delhivery.axle.ui.searchload.fragments.ProgressSearchLoadAction
import com.delhivery.axle.ui.searchload.fragments.SearchLoadBaseFragment
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.centerX
import com.delhivery.axle.utils.extensions.centerY
import com.delhivery.axle.utils.extensions.isNotEmpty
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
    SearchLoadsRVAdapterInterface {

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
    }

    viewModel.bidsActionLiveData.observe(this, Observer {
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
            numBids = data?.numBids-oldBidCount+newBidCount
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
            EVENT_BID_REVISE_SUBMITTED,
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
            EVENT_BID_REVISE_SUBMITTED,
            mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE,
              PROPERTY_USER_BID_VALUE_OLD, PROPERTY_USER_BID_VALUE_NEW),
            mutableListOf(data?.transactionId?:"",data?.numBids.toString()?:"",data?.lowestBid.toString()?:" ",oldAmount.toString()?:"",data?.bidAmountValue().toString()?:"")
          )
          reviseInitiated=false
        }
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
              EVENT_BID_REVISE_INITIATED,
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
          BulkBidDetailsCreateEditDialog(context!!, it.second, it.second.bulkTransactionBids, it.first, viewModel, it.second.unAllocatedVolume!!,
            pos, analyticsUtil, userPrefs, "load_screen", pageTitle).show()
        }
        else{
          Toast.makeText(context, "No Vehicle Types Found",Toast.LENGTH_SHORT).show()
        }
      }
    })

    Transformations.map(viewModel.searchResults) {
      return@map mutableListOf<Pair<BaseSearchLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
        if (it.isNullOrEmpty()) {
          add(Pair(SearchLoadWarningItem_NoLoad, Add))
        } else {
          it.forEach { _item -> add(Pair(SearchLoadsRequestItem(_item), Add)) }
        }
      }
    }
        .observe(this, SearchResultsObserver())
  }

  private fun setupSpinners() {
    binding.spinnerTruckType.isEnabled = false
    binding.spinnerTruckType.isClickable = false
    binding.spinnerTruckType.setup(R.array.array_truck_type) { p, v -> }
  }

  /**
   * Search with query params
   */
  fun search(
    origin: CityModel,
    destination: CityModel?,
    type: String,
    saveToHistory: Boolean,
    progress: Boolean = true
  ) {
    this.saveToHistory = saveToHistory
    /* clear and add first dummy item */
    _adapter.clearItems()
    _adapter.operation(SearchLoadsSearchSpinnerItem(), Add)
    /* show progress if needed */
    if (progress)
      action(ProgressSearchLoadAction(true))
    binding.origin = origin
    binding.destination = destination
    val pos = when (type) {
      "Closed" -> 1
      "Open" -> 2
      "Trailer" -> 3
      else -> 0
    }
    binding.spinnerTruckType.setSelection(pos, true)
    viewModel.searchLoad(origin, destination, type)
  }

  override fun handleAction(
    actionId: String,
    item: BaseSearchLoadsRVAdapterItem<*>
  ) {
    when (actionId) {
      HomeBidsRequestAction_ViewDetails -> {
        val _item = item.data as HomeBidsRequestItemData
        // Capture event
        analyticsUtil.moEngageTrackEvent(
            EVENT_SEARCH_RESULTS_ORDER_CARD_CLICK,
            mutableListOf(PROPERTY_ORDER_ID, PROPERTY_ORDER_RANK),
            mutableListOf( _item.transactionId ?: "",orderRank.toString())
        )
        analyticsUtil.trackEvent(
            EVENT_LIST_ITEM,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
            mutableListOf(VALUE_LOAD, _item.transactionId ?: "")
        )
        context?.let {
          userPrefs.setPreviousScreen(this.javaClass.name)
          startActivity(bidDetailsIntent(_item.key(), it, if(_item.isDMTIndent()) "dmt" else "")) }
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
                    EVENT_BID_REVISE_INITIATED,
                    mutableListOf(PROPERTY_ORDER_ID, PROPERTY_BID_COUNT, PROPERTY_ORDER_LOWEST_BID_VALUE),
                    mutableListOf(
                      data.transactionId.toString(), data?.numBids.toString(),
                      data?.lowestBid.toString()
                    )
                  )
                  reviseInitiated=true
                }
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
  inner class SearchResultsObserver : Observer<MutableList<Pair<BaseSearchLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>> {
    override fun onChanged(t: MutableList<Pair<BaseSearchLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>?) {
      resetSpinnerContainer()
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
            EVENT_PAGE_LOAD_SEARCH_RESULTS_NO_ORDERS,
            mutableListOf(PROPERTY_SEARCH_ORIGIN_CITY, PROPERTY_SEARCH_DESTINATION_CITY,
                PROPERTY_SEARCH_BODY_TYPE),
            mutableListOf(  binding.origin?.cityName() ?: "Anywhere",
                binding.destination?.cityName() ?: "Anywhere",
                binding.spinnerTruckType.selectedItem.toString())
        )
      }else{
        analyticsUtil.moEngageTrackEvent(
            EVENT_PAGE_LOAD_SEARCH_RESULTS_WITH_ORDERS,
            mutableListOf(PROPERTY_SEARCH_ORIGIN_CITY, PROPERTY_SEARCH_DESTINATION_CITY,
                PROPERTY_SEARCH_BODY_TYPE, PROPERTY_ORDER_COUNT),
            mutableListOf(  binding.origin?.cityName() ?: "Anywhere",
                binding.destination?.cityName() ?: "Anywhere",
                binding.spinnerTruckType.selectedItem.toString(),
              numResults.toString())
        )
      }
      analyticsUtil.trackEvent(
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
          updateVisibility(spinnerTruckType, View.INVISIBLE)
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
}