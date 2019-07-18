package com.delhivery.axle.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Pagination scroll listener
 */
abstract class PaginationScrollListener(private val pageSize: Int) : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
  override fun onScrolled(
    recyclerView: androidx.recyclerview.widget.RecyclerView,
    dx: Int,
    dy: Int
  ) {
    super.onScrolled(recyclerView, dx, dy)
    if (recyclerView.layoutManager is androidx.recyclerview.widget.LinearLayoutManager) {
      (recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager?)?.apply {
        val visibleItemCount = childCount
        val totalItemCount = itemCount
        val firstVisible = findFirstVisibleItemPosition()
        if (!isLoading() && hasMore()) {
          if (visibleItemCount + firstVisible >= totalItemCount && firstVisible >= 0 && totalItemCount >= pageSize) {
            loadMore()
          }
        }
      }
    }
  }

  /**
   * Load more items/paginate
   */
  abstract fun loadMore()

  /**
   * Check is has more items
   */
  abstract fun hasMore(): Boolean

  /**
   * Check if loading
   */
  abstract fun isLoading(): Boolean
}