package com.delhivery.orion.ui.base.adapter

import android.databinding.ViewDataBinding
import com.delhivery.orion.data.BaseKeyTypeModel
import com.delhivery.orion.ui.base.BaseViewHolder
import com.delhivery.orion.utils.extensions.isNotNullOrEmpty

abstract class BaseFilterableDataRVAdapter<D : BaseKeyTypeModel<out Any>, B : ViewDataBinding, VH : BaseViewHolder<*>>(private val clickListener: ItemClickListener<D>) :
    BaseDataRVAdapter<D, B, VH>(clickListener) {

  /* List of filtered items */
  private val filteredItems = mutableListOf<D>()

  /* Is filter mode enabled */
 protected var isFiltering = false

  override fun itemsList() = when (isFiltering) {
    true -> filteredItems
    false -> items
  }

  /**
   * Filtered items, override to change
   */
  protected open fun filterList(query: String) = items.filter { it.filter(query) }

  /**
   * Filter items as per query
   *
   * @return true if filtered results are found else false
   */
  fun filter(query: String?): Boolean {
    filteredItems.clear()
    /* filter new results */
    if (query.isNotNullOrEmpty()) {
      filteredItems.addAll(filterList(query!!))
    }
    notifyDataSetChanged()
    return filteredItems.isNotEmpty()
  }

  /**
   * Enable filtering
   */
  open fun enableFilter() {
    isFiltering = true
    notifyDataSetChanged()
  }

  /**
   * Cancel filtering
   */
  fun cancelFilter() {
    isFiltering = false
    filteredItems.clear()
    notifyDataSetChanged()
  }

  fun checkFiltering(): Boolean {
    return isFiltering
  }
}