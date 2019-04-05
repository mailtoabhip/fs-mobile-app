package com.delhivery.orion.utils

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Pagination scroll listener
 */
abstract class PaginationScrollListener(private val pageSize: Int) : RecyclerView.OnScrollListener() {
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