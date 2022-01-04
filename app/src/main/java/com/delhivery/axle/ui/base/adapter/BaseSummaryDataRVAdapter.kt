package com.delhivery.axle.ui.base.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.indexById
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.NoOp
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Update
import com.delhivery.axle.utils.extensions.safeEquals

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 10/5/21
 */

abstract class BaseSummaryDataRVAdapter<
    D : BaseKeyTypeModel<out Any>,
    B : ViewDataBinding,
    VH : BaseViewHolder<*>>(private val clickListener: ItemClickListener<D>) : androidx.recyclerview.widget.RecyclerView.Adapter<VH>() {

  /* List of items */
  protected val items: MutableList<D> = mutableListOf()

  /**
   * Clear data set and set items and notifyDataSetChanged()
   *
   * @param items [List] of [D] items
   */
  open fun setItems(items: List<D>) {
    this.items.clear()
    this.items.addAll(items)
    notifyDataSetChanged()
  }

  /**
   * Remove all items off the list and update adapter for same
   */
  fun clearItems() {
    this.items.clear()
    notifyDataSetChanged()
  }

  override fun getItemCount() = itemsList().size

  override fun onBindViewHolder(
    holder: VH,
    position: Int
  ) {
    itemsList()[position].let { item ->
      bindVH(holder, item)
      holder.binding.root.setOnClickListener { clickListener.onItemClicked(item,position) }
    }
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ) = parent.let {
    getBinding(LayoutInflater.from(it.context), it, viewType)
  }.let {
    createVH(it)
  }

  /**
   * Items list for further expansion
   */
  open fun itemsList(): List<D> = items

  /**
   * Get VH Binding as [B]
   *
   * @param inflater LayoutInflater
   * @param parent View Holder parent
   *
   * @return [ViewDataBinding] of type [B]
   */
  abstract fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ): B

  /**
   * Create View Holder with binding [B]
   *
   * @param binding of type [B]
   *
   * @return [VH] View Holder
   */
  abstract fun createVH(binding: B): VH

  /**
   * Bind View Holder with [D] data item
   *
   * @param holder View Holder of type [VH]
   * @param item [D] data item
   */
  abstract fun bindVH(
    holder: VH,
    item: D
  )

  /**
   * Update item in list
   *
   * @param updatedItem [D] type item
   *
   * @return [Boolean] to consider if updated item is added to list or not, as matching is taking cared on key
   */
  fun updateItem(updatedItem: D?): Boolean {
    if (updatedItem == null) return false
    items.forEach {
      if (it.key().safeEquals(updatedItem.key())) {
        val index = items.indexOf(it)
        items[index] = updatedItem
        notifyItemChanged(index)
        return true
      }
    }
    return false
  }

  /**
   * Perform [DataRVAdapterOperationType] on [items]
   */
  fun operation(items: List<Pair<D, DataRVAdapterOperationType>>) {
    items.forEach {
      operation(it.first, it.second)
    }
  }

  /**
   * Perform [DataRVAdapterOperationType] on [item]
   */
  fun operation(
    item: D,
    operationType: DataRVAdapterOperationType
  ) {
    when (operationType) {
      NoOp -> {/* nothing */
      }
      Add -> {
        items.add(item)
        notifyItemInserted(items.size)
      }

      else -> {
        val _itemIndex = items.indexById(item.key())
        when (operationType) {
          Remove -> {
            if (_itemIndex != -1) {
              items.removeAt(_itemIndex)
              notifyItemRemoved(_itemIndex)
            }
          }
          AddUpdate, Update -> {
            if (_itemIndex != -1) {
              items[_itemIndex] = item
              notifyItemChanged(_itemIndex)
            } else if (operationType == AddUpdate) {
              items.add(item)
              notifyItemInserted(items.size)
            }
          }
          else -> {/* do nothing */
          }
        }
      }
    }
  }

  /**
   * RV Adapter item click listener of data item [D]
   */
  interface ItemClickListener<D> {
    fun onItemClicked(item: D, position: Int)
  }

}