package com.delhivery.orion.ui.base

import android.content.Context
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView

/**
 * Base View Holder for Recycler View
 *
 * @param binding [ViewDataBinding] of specific layout
 */
abstract class BaseViewHolder<out B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root) {
    //Context reference
    protected val context: Context by lazy { binding.root.context }
}