package com.delhivery.axle.ui.searchload.fragments.searchresults

import android.os.Bundle
import android.view.View
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
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.home.fragments.bids.SearchLoadWarningItem_NoLoad
import com.delhivery.axle.ui.searchload.fragments.ProgressSearchLoadAction
import com.delhivery.axle.ui.searchload.fragments.SearchLoadBaseFragment
import com.delhivery.axle.utils.Config
import com.delhivery.axle.utils.ContactUtils
import com.delhivery.axle.utils.DialogUtils
import com.delhivery.axle.utils.EVENT_LIST_ITEM
import com.delhivery.axle.utils.EVENT_SEARCH_LOAD
import com.delhivery.axle.utils.EVENT_SEARCH_SAVED_LOAD
import com.delhivery.axle.utils.PROPERTY_DESTINATION
import com.delhivery.axle.utils.PROPERTY_NUM_RESULTS
import com.delhivery.axle.utils.PROPERTY_ORIGIN
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_ID
import com.delhivery.axle.utils.PROPERTY_TRANSACTION_TYPE
import com.delhivery.axle.utils.PROPERTY_TRUCK_TYPE
import com.delhivery.axle.utils.VALUE_LOAD
import com.delhivery.axle.utils.extensions.centerX
import com.delhivery.axle.utils.extensions.centerY
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
              it != null -> {
                (_adapter.itemsList()[it.first].data as HomeBidsRequestItemData).transactionBid =
                  it.second
                _adapter.notifyItemChanged(it.first)
              }
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
        analyticsUtil.trackEvent(
            EVENT_LIST_ITEM,
            mutableListOf(PROPERTY_TRANSACTION_TYPE, PROPERTY_TRANSACTION_ID),
            mutableListOf(VALUE_LOAD, _item.transactionId ?: "")
        )
        context?.let { startActivity(bidDetailsIntent(_item, it)) }
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
            (item.data as HomeBidsRequestItemData).let {
              BidDetailsCreateEditDialog(
                  context!!, it, it.transactionBid, viewModel, position, analyticsUtil, userPrefs
              ).show()
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

      analyticsUtil.trackEvent(
          event,
          mutableListOf(
              PROPERTY_ORIGIN, PROPERTY_DESTINATION,
              PROPERTY_TRUCK_TYPE, PROPERTY_NUM_RESULTS
          ),
          mutableListOf(
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