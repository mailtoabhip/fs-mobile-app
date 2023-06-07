package com.delhivery.axle.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
/*
Pagination scroll listener where the number of items per page is not fixed
 */
abstract class DynamicPaginationScrollListener: RecyclerView.OnScrollListener() {
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
          if (visibleItemCount + firstVisible >= totalItemCount && firstVisible >= 0) {
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