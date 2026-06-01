package com.delhivery.axle.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Pagination scroll listener
 */
abstract class PaginationScrollListener(private val pageSize: Int, private val isConsolidatedApi: Boolean = false) : RecyclerView.OnScrollListener() {
  override fun onScrolled(
    recyclerView: RecyclerView,
    dx: Int,
    dy: Int
  ) {
    super.onScrolled(recyclerView, dx, dy)
    if (recyclerView.layoutManager is LinearLayoutManager) {
      (recyclerView.layoutManager as LinearLayoutManager?)?.apply {
        val visibleItemCount = childCount
        val totalItemCount = itemCount
        val firstVisible = findFirstVisibleItemPosition()
      }
    }
  }

  /**
   * Check is has more items
   */
  abstract fun hasMore(): Boolean

  /**
   * Check if loading
   */
  abstract fun isLoading(): Boolean
}