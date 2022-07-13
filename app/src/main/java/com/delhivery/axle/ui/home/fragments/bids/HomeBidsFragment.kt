package com.delhivery.axle.ui.home.fragments.bids

import android.animation.ValueAnimator
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.home.bids.*
import com.delhivery.axle.database.entity.OffersEntity
import com.delhivery.axle.databinding.FragmentHomeBidsBinding
import com.delhivery.axle.ui.biddetails.*
import com.delhivery.axle.ui.bids.BidType.ActiveBid
import com.delhivery.axle.ui.bids.BidType.ConfirmedBid
import com.delhivery.axle.ui.bids.BidType.LostBid
import com.delhivery.axle.ui.bids.BulkBidDetailsDialog
import com.delhivery.axle.ui.bids.userBidsIntent
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar.ToolbarElevationChangeListener
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.sharerate.ShareRateActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import java.util.Calendar
import javax.inject.Inject

/**
 * All bids screen on home
 */
class HomeBidsFragment : HomeBaseFragment<FragmentHomeBidsBinding, HomeBidsViewModel>(),
    HomeBidsRVAdapterInterface, ToolbarElevationChangeListener {

  var _title: String = "My Bids"
  var launch : Boolean =true
  @Inject lateinit var userPrefs: UserPrefs

  override val title: CharSequence
    get() = _title

  init {
    toolbarElevationLiveData = MutableLiveData()
    hasInlineProgress = true
  }

  companion object {
    /* singleton instance */
    val _instance: HomeBidsFragment by lazy { HomeBidsFragment() }
  }

  override fun getViewModelClass() = HomeBidsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_bids

  /* RV adapter */
  private val adapter: HomeBidsRVAdapter by lazy { HomeBidsRVAdapter(this) }


  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      /* remove user transactions and fetch again */
      refreshData()
    }

    viewModel.fetchDatabaseOffers()

    viewModel.fetchDatabaseOffers().observe(viewLifecycleOwner, Observer {
      if (!it.isNullOrEmpty()) {
        viewModel.finalOffers.postValue(it as ArrayList<OffersEntity>?)
        adapter.notifyDataSetChanged()
      }
    })

    /* setup recycler view */
    binding.rvBids.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
      adapter = this@HomeBidsFragment.adapter
      addOnScrollListener(HomeBidsRVScrollListener(binding.editStickySearch))
      addOnScrollListener(PaginationInterface())
    }

    adapter.setItems(getStaticData())

    /* observe and update adapter items */
    viewModel.userBidsData.reobserve(this, Observer {
     if(launch) {
       analyticsUtil.trackEvent(
               EVENT_VIEW_BIDS_SCREEN,
               mutableListOf(PROPERTY_USER_ID, PROPERTY_ACTIVE_BIDS, PROPERTY_CONFIRMED_BIDS, PROPERTY_LOST_BIDS),
               mutableListOf(userPrefs.userId(), viewModel.activeBids, viewModel.confirmedBids, viewModel.lostBids)
       )

       launch=false
     }
      it?.let { _items -> adapter.operation(_items) }
    })

    viewModel.bidsCountLiveData.reobserve(this, Observer {
      _title = when (it) {
        0, null -> getString(string.label_my_bids)
        else -> "${getString(string.label_my_bids)}($it)"
      }
    })

    viewModel.dataLoadingLiveData.reobserve(this, Observer {
      isLoadingData = it ?: false
    })

    /* attach sticky search with adapter */
    binding.editStickySearch.attachWithAdapter(adapter, this)

    /* fetch bids data*/
    fetchBidsData()
  }

  private fun fetchBidsData() {
    viewModel.fetchBidsSummary()
    viewModel.fetchBids()
  }

  private fun refreshData() {
    /* remove user transactions */
    adapter.resetStaticData()
    /* fetch again */
    fetchBidsData()
  }

  private fun getStaticData() = mutableListOf<BaseHomeBidsRVAdapterItem<*>>().apply {
    add(0, HomeBidsHeaderItem())
    add(1, HomeBidsSearchItem(HomeBidsSearchItemData()))
    add(2, HomeBidsProgressItem())
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeBidsRVAdapterItem<*>
  ) {
    // handle actions here
    when (actionId) {
      HomeBidsHeaderAction_MyBids -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_VIEW_ACTIVE_BIDS,
            mutableListOf(PROPERTY_USER_ID, PROPERTY_ACTIVE_BIDS),
            mutableListOf(userPrefs.userId(), viewModel.activeBids)
        )
        startActivityForResult(userBidsIntent(context!!, ActiveBid), REQCODE_NO_ROUTES)
      }

      HomeBidsHeaderAction_ConfirmedBids -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_VIEW_CONFIRMED_BIDS,
            mutableListOf(PROPERTY_USER_ID, PROPERTY_CONFIRMED_BIDS),
            mutableListOf(userPrefs.userId(),viewModel.confirmedBids)
        )
        startActivityForResult(userBidsIntent(context!!, ConfirmedBid), REQCODE_NO_ROUTES)
      }

      HomeBidsHeaderAction_LostBids -> {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_VIEW_LOST_BIDS,
            mutableListOf(PROPERTY_USER_ID, PROPERTY_LOST_BIDS),
            mutableListOf(userPrefs.userId(), viewModel.lostBids)
        )
        startActivityForResult(userBidsIntent(context!!, LostBid), REQCODE_NO_ROUTES)
      }

      HomeBidsRequestAction_ViewDetails -> {
        val _item = item.data as HomeBidsRequestItemData
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_LIST_ITEM,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
            mutableListOf(VALUE_BID, _item.transactionId ?: "")
        )
        val dmtStatus = if(_item.isDMTIndent())
          "dmt"
        else ""
        val active = dmtStatus =="dmt" && _item.bidStatus().status == "Active"
        val id = if(dmtStatus =="dmt" && (_item.bidStatus().status == "Confirmed" ||_item.bidStatus().status == "Lost"|| _item.bidStatus().status == "Cancelled"))
          _item.transactionBid!!.childTransactionId else _item.key()
        if(id!=null)
        context?.let {
          startActivity(bidDetailsIntent(id, it, dmtStatus, true, active))
        }
        else{
          Toast.makeText(context,"Not Found",Toast.LENGTH_SHORT).show()
        }
      }

      HomeBidsRequestAction_ViewOtherDetails -> {
        val _item = item.data as HomeBidsRequestItemData
        // Capture event
        analyticsUtil.trackEvent(
          EVENT_LIST_ITEM,
          mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
          mutableListOf(VALUE_BID, _item.transactionId ?: "")
        )
        Log.i("itemDailog", "clicked")
        bidDialog(_item)
      }

      HomeBidsSearchAction_Search -> context?.let {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_SEARCH_LOCAL,
            mutableListOf(PROPERTY_TRANSACTION_TYPE),
            mutableListOf(VALUE_BID)
        )

        val childView = binding.rvBids.findViewHolderForAdapterPosition(1)!!.itemView
        val stickyView = binding.editStickySearch
        stickyView.visibility = View.VISIBLE
        stickyView.translationY = childView.top.toFloat()
        stickyView.alpha = 1f
        binding.rvBids.alpha = 0f
        adapter.enableFilter()

        val valueAnimator = ValueAnimator.ofInt(childView.top, 0)
        valueAnimator.duration = 250
        valueAnimator.addUpdateListener { t ->
          val animValue = t.animatedValue as Int
          stickyView.translationY = animValue.toFloat()
          stickyView.setRatio((animValue.toFloat() / childView.top))
          if (animValue.toFloat() / childView.top == 1f) {
            binding.rvBids.alpha = 1f
          }
        }
        valueAnimator.start()
        stickyView.postDelayed({
          stickyView.requestFocus()
          uiUtils.toggleKeyboard(false)
          toolbarElevationLiveData!!.postValue(0f)
        }, 300)
      }

      HomeBidsWarningAction_NoBids -> {
        action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
      }

      HomeBidsTimeOutAction -> {
        refreshData()
      }
    }
  }

  private fun bidDialog(transaction: HomeBidsRequestItemData? = null) {
      //  binding.transaction?.let {
          BulkBidDetailsDialog(
            context!!, transaction!!,transaction.bulkTransactionBids,viewModel, analyticsUtil = analyticsUtil, userPrefs = userPrefs
          ).show()
      //  }
  }
  override fun postElevation(elevation: Float) {
    toolbarElevationLiveData!!.postValue(elevation)
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      REQCODE_NO_ROUTES -> {
        if (resultCode == RESULT_OK) {
          action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
        }
      }
      else -> {

      }
    }
  }

  override fun getTotalOffers(origin_id: String?, dest_id: String?, tid: String?): Triple<Pair<Boolean?,String?>, Pair<String?, String?>?, Pair<String?, String?>?>? {
    var pres:Triple<Pair<Boolean?,String?>, Pair<String?, String?>?, Pair<String?, String?>?>? = Triple(Pair(false, null), Pair(tid, null), Pair(null, null))
    if(viewModel.finalOffers.value.isNullOrEmpty()){
      pres = null
      userPrefs.bidOfferCount=0
    }else{
      for(r in viewModel.finalOffers.value!!){
        if(r.oc?.toLowerCase()?.equals(origin_id?.toLowerCase()) == true && r.dc?.toLowerCase().equals(dest_id?.toLowerCase())){
          pres = pres?.copy(Pair(true,r.offerId), Pair(tid, r.tdn), Pair(r.occ, r.dcc))
        }
      }
      userPrefs.bidOfferCount= viewModel.finalOffers.value!!.size
    }
    return pres
  }

  override fun callShareRate(data: HomeBidsRequestItemData?, itemTD: String?, offerTD: String?, occ:String?, dcc:String?, offerid:String?) {
    val bundle = Bundle()
    bundle.putString("originname", data?.origin)
    bundle.putString("destname", data?.destination)
    bundle.putString("occ", occ)
    bundle.putString("dcc", dcc)
    bundle.putString("truckNumber", data?.transactionBid?.vehicleNumber)
    bundle.putString("truckType", data?.truckSpecification?.truckDispName)
    bundle.putString("truckCapacity", data?.truckCapacity())
    bundle.putString("itemTD", itemTD)
    bundle.putString("offerTD", offerTD)
    bundle.putString("offerid", offerid)

    analyticsUtil.trackEvent(
            EVENT_CLICKED_OFFER,
            mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_SOURCE, PROPERTY_OFFER_ID),
            mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy", "bid_screen", offerid?:"")
    )
    navigationUtils.navigate(ShareRateActivity::class.java, false, bundle)
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(10) {
    override fun loadMore() = viewModel.fetchBids(true)

    override fun hasMore() = viewModel.offset < viewModel.total

    override fun isLoading() = isLoadingData
  }

  inner class HomeBidsRVScrollListener(
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

      if (!adapter.checkFiltering()) {
        val layoutManager =
          (recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager)
        val pos = layoutManager.findFirstVisibleItemPosition()
        val _toolbarElevation = if (pos == 0) {
          stickyView.translationY = 0f
          stickyView.visibility = View.GONE
          stickyView.alpha = 0f
          stickyView.setRatio(1f)
          defToolbarElevation
        } else if (pos == 1) {
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
        } else {
          stickyView.visibility = View.VISIBLE
          stickyView.translationY = 0f
          stickyView.alpha = 1f
          stickyView.setRatio(0f)
          0f
        }
        if (_toolbarElevation != toolbarElevation) {
          toolbarElevation = _toolbarElevation
          toolbarElevationLiveData!!.postValue(toolbarElevation)
        }
      }
    }
  }
}