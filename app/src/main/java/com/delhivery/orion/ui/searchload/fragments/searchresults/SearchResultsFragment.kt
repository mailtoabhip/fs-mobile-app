package com.delhivery.orion.ui.searchload.fragments.searchresults

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.data.home.HomeBidsRequestAction_ViewDetails
import com.delhivery.orion.databinding.FragmentSearchResultsBinding
import com.delhivery.orion.ui.biddetails.bidDetailsIntent
import com.delhivery.orion.ui.home.fragments.bids.BaseHomeBidsRVAdapterItem
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapter
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsRVAdapterInterface
import com.delhivery.orion.ui.searchload.fragments.ProgressSearchLoadAction
import com.delhivery.orion.ui.searchload.fragments.SearchLoadBaseFragment
import com.delhivery.orion.utils.extensions.centerX
import com.delhivery.orion.utils.extensions.centerY
import com.delhivery.orion.utils.extensions.setup

class SearchResultsFragment : SearchLoadBaseFragment<FragmentSearchResultsBinding, SearchResultsViewModel>(),
    HomeBidsRVAdapterInterface {

  companion object {
    val _instance: SearchResultsFragment by lazy { SearchResultsFragment() }
  }

  override fun getViewModelClass() = SearchResultsViewModel::class.java

  override fun layoutId() = R.layout.fragment_search_results

  private val _adapter by lazy {
    HomeBidsRVAdapter(this)
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
      LinearSnapHelper().attachToRecyclerView(this)
    }

    /* observe search results */
    viewModel.searchResults.observe(this, SearchResultsObserver())
  }

  /**
   * Setup spinners
   */
  private fun setupSpinners() {
    /* truck type */
    binding.spinnerTruckType.setup(R.array.array_truck_type) { p, v ->

    }

    /* truck size */
    binding.spinnerTruckSize.setup(R.array.array_truck_size) { p, v ->

    }
  }

  /**
   * Search with query params
   */
  fun search(
    origin: CityModel,
    destination: CityModel,
    type: String,
    size: String,
    progress: Boolean = true
  ) {
    /* show progress if needed */
    if (progress)
      action(ProgressSearchLoadAction(true))
    binding.origin = origin
    binding.destination = destination
    viewModel.searchLoad(origin, destination, type, size)
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeBidsRVAdapterItem<*>
  ) {
    when (actionId) {
      HomeBidsRequestAction_ViewDetails -> context?.let { startActivity(bidDetailsIntent(it)) }
    }
  }

  /**
   * Search results observer
   */
  inner class SearchResultsObserver : Observer<List<BaseHomeBidsRVAdapterItem<*>>> {
    override fun onChanged(t: List<BaseHomeBidsRVAdapterItem<*>>?) {
      resetSpinnerContainer()
      /* hide progress */
      action(ProgressSearchLoadAction(false))
      /* show results */
      if (t == null) {
        //error
      } else {
        _adapter.setItems(t)
      }
    }
  }

  /**
   * Reset spinner container
   */
  private fun resetSpinnerContainer() {
    binding.apply {
      _scrollListener.coordinateView(spinnerTruckType, viewHiddenIndicator, 0f)
      _scrollListener.coordinateView(spinnerTruckSize, viewHiddenIndicator, 0f)
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
          updateVisibility(spinnerTruckSize, View.INVISIBLE)
          maxTranslationY
        } else {
          val childView = recyclerView.findViewHolderForAdapterPosition(0)!!.itemView
          val childTop = childView.top * 1f
          val factor = Math.min(childTop / maxTranslationY, 1f)
          viewHiddenIndicator.alpha = factor
          coordinateView(spinnerTruckType, viewHiddenIndicator, factor)
          coordinateView(spinnerTruckSize, viewHiddenIndicator, factor)
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