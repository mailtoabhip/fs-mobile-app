package com.delhivery.axle.ui.searchongoingtrip

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.UserSearchLimit
import com.delhivery.axle.databinding.ActivitySearchOngoingTripBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.PaginationScrollListener

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 12/5/21
 */

class SearchOngoingTripActivity : BaseActivity<ActivitySearchOngoingTripBinding,
    SearchOngoingTripViewModel>(), SearchOngoingTripRVAdapterInterface {

  override fun getViewModelClass() = SearchOngoingTripViewModel::class.java

  override fun layoutId() = R.layout.activity_search_ongoing_trip

  override fun requireConnection() = true

  var isLoadingData = true

  init {
    hasInlineProgress = true
  }

  private val adapter by lazy { SearchOngoingTripRVAdapter(this) }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    setSupportActionBar(binding.toolbar)
    title = "Search Ongoing Trips"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding.rvOngoingTripSearch.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
      adapter = this@SearchOngoingTripActivity.adapter
      addOnScrollListener(PaginationInterface())
    }

    viewModel.searchLiveData.observe(this, Observer {
      adapter.resetStaticData()
      if (it != null) {
        adapter.operation(it)
      }
    })

    binding.editQuery.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) = Unit
      override fun beforeTextChanged(
        s: CharSequence?,
        start: Int,
        count: Int,
        after: Int
      ) = Unit

      override fun onTextChanged(
        s: CharSequence?,
        start: Int,
        before: Int,
        count: Int
      ) {
        if (s != null) {
          try {
            viewModel.searchText = s.trim().toString()
            Log.d("prefix", s.trim().toString())

            if (viewModel.searchText.length in 3..10) {
              refreshData()
            } else {
              adapter.resetStaticData()
            }
          }  catch (e: Exception) {
            binding.editQuery.error = e.message
          }
        }
      }
    })

    if (viewModel.searchText.length in 3..10) {
      refreshData()
    }
  }

  /**
   * refresh data
   */
  fun refreshData() {
    adapter.resetStaticData()
    viewModel.searchTrips(false)
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(
      UserSearchLimit
  ) {
    override fun loadMore() = viewModel.searchTrips(true)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }

  override fun handleAction(
    actionId: String,
    item: BaseSearchOngoingTripRVAdapterItem<*>
  ) {

  }

}

/**
 * Trip details intent
 */
fun searchOngoingTripIntent(
  context: Context
) = Intent(context, SearchOngoingTripActivity::class.java).apply {
}