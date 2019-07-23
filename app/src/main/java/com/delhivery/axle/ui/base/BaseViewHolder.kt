package com.delhivery.axle.ui.base

import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Base View Holder for Recycler View
 *
 * @param binding [ViewDataBinding] of specific layout
 */
abstract class BaseViewHolder<out B : ViewDataBinding>(val binding: B) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
    //Context reference
    protected val context: Context by lazy { binding.root.context }
}