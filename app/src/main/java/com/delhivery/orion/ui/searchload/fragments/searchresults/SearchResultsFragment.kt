package com.delhivery.orion.ui.searchload.fragments.searchresults

import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.data.home.bids.HomeBidsRequestAction_AcceptBid
import com.delhivery.orion.data.home.bids.HomeBidsRequestAction_PlaceBid
import com.delhivery.orion.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.orion.data.home.bids.HomeBidsRequestItemData
import com.delhivery.orion.databinding.FragmentSearchResultsBinding
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.biddetails.BidDetailsCreateEditDialog
import com.delhivery.orion.ui.biddetails.bidDetailsIntent
import com.delhivery.orion.ui.searchload.fragments.ProgressSearchLoadAction
import com.delhivery.orion.ui.searchload.fragments.SearchLoadBaseFragment
import com.delhivery.orion.utils.extensions.centerX
import com.delhivery.orion.utils.extensions.centerY
import com.delhivery.orion.utils.extensions.setup

class SearchResultsFragment : SearchLoadBaseFragment<FragmentSearchResultsBinding, SearchResultsViewModel>(),
    SearchLoadsRVAdapterInterface {

  companion object {
    val _instance: SearchResultsFragment by lazy { SearchResultsFragment() }
  }

  override fun getViewModelClass() = SearchResultsViewModel::class.java

  override fun layoutId() = R.layout.fragment_search_results

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
      layoutManager = LinearLayoutManager(context)
      adapter = _adapter
      addOnScrollListener(_scrollListener)
    }

    viewModel.bidsActionLiveData.observe(this, Observer {
      uiUtils.toggleKeyboard()
          .apply {
            when {
              it != null -> {
                (_adapter.itemsList()
                    .get(it.first).data as HomeBidsRequestItemData).transactionBid = it.second
                _adapter.notifyItemChanged(it.first)
              }
            }
          }
    })

    /* transform observe search results */
    Transformations.map(viewModel.searchResults) {
      return@map mutableListOf<Pair<BaseSearchLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
        /* add all transactions */
        it.forEach { _item -> add(Pair(SearchLoadsRequestItem(_item), Add)) }
      }
    }
        .observe(this, SearchResultsObserver())
  }

  /**
   * Setup spinners
   */
  private fun setupSpinners() {
    binding.spinnerTruckType.isEnabled = false
    binding.spinnerTruckType.isClickable = false
    /* truck type */
    binding.spinnerTruckType.setup(R.array.array_truck_type) { p, v ->

    }
  }

  /**
   * Search with query params
   */
  fun search(
    origin: CityModel,
    destination: CityModel?,
    type: String,
    progress: Boolean = true
  ) {
    /* clear and add first dummy item */
    _adapter.clearItems()
    _adapter.operation(SearchLoadsSearchSpinnerItem(), Add)
    /* show progress if needed */
    if (progress)
      action(ProgressSearchLoadAction(true))
    binding.origin = origin
    binding.destination = destination
    val pos = when (type) {
      "Closed" -> 0
      "Open" -> 1
      else -> 2
    }
    binding.spinnerTruckType.setSelection(pos, true)
    viewModel.searchLoad(origin, destination, type)
  }

  override fun handleAction(
    actionId: String,
    item: BaseSearchLoadsRVAdapterItem<*>
  ) {
    when (actionId) {
      HomeBidsRequestAction_ViewDetails -> context?.let {
        startActivity(
            bidDetailsIntent(item.data as HomeBidsRequestItemData, it)
        )
      }
    }
  }

  override fun handleAction(
    actionId: String,
    item: BaseSearchLoadsRVAdapterItem<*>,
    position: Int
  ) {
    when (actionId) {
      HomeBidsRequestAction_PlaceBid -> {
        (item.data as HomeBidsRequestItemData).let {
          BidDetailsCreateEditDialog(
              context!!, it, it.transactionBid, viewModel, position, uiUtils
          ).show()
        }
      }
      HomeBidsRequestAction_AcceptBid -> {
        (item.data as HomeBidsRequestItemData).let {
          it.transactionId?.let { it1 -> viewModel.createBid(it1, it.targetPrice, position) }
        }
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
      if (t == null) { //&& viewModel.offset == 0
        //error
      } else {
        _adapter.operation(t)
      }
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
  inner class SearchResultsRVScrollListener() : OnScrollListener() {
    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)
      binding.apply {
        val layoutManager = (recyclerView.layoutManager as LinearLayoutManager)
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