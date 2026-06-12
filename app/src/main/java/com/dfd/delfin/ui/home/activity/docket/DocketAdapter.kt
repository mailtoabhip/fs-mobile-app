package com.dfd.delfin.ui.home.activity.docket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dfd.delfin.data.DocketItem
import com.dfd.delfin.data.DocketState
import com.dfd.delfin.databinding.ItemPodBinding
import com.dfd.delfin.injection.module.GlideApp
import com.dfd.delfin.utils.BitmapUtils

/**
 * Adapter for displaying Docket items in RecyclerView
 * Max 1 item (can be extended in future)
 */
class DocketAdapter(
    private val onDocketClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit,
    private val bitmapUtils: BitmapUtils
) : RecyclerView.Adapter<DocketAdapter.DocketViewHolder>() {

    private var items: List<DocketItem> = emptyList()

    fun updateItems(newItems: List<DocketItem>) {
        val filterItems = newItems.filter { it.state != DocketState.EMPTY }

        val diffCallback = DocketDiffCallback(this.items, filterItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.items = filterItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocketViewHolder {
        val binding = ItemPodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DocketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocketViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class DocketViewHolder(
        private val binding: ItemPodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DocketItem) {
            when (item.state) {
                DocketState.EMPTY -> {
                    binding.root.visibility = View.GONE
                }
                DocketState.AVAILABLE -> {
                    binding.root.visibility = View.VISIBLE
                    binding.addIcon.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.podImageCard.visibility = View.GONE
                    binding.podImage.visibility = View.GONE
                    binding.deleteButton.visibility = View.GONE
                    binding.podContainer.setOnClickListener {
                        onDocketClick(item.id)
                    }
                    binding.deleteButton.setOnClickListener(null)
                }
                DocketState.SELECTED -> {
                    binding.root.visibility = View.VISIBLE
                    binding.addIcon.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.podImageCard.visibility = View.VISIBLE
                    binding.podImage.visibility = View.VISIBLE
                    binding.deleteButton.visibility = View.VISIBLE

                    // Load thumbnail from local path
                    item.imagePath?.let { path ->
                        binding.podImage.viewTreeObserver.addOnPreDrawListener(object : android.view.ViewTreeObserver.OnPreDrawListener {
                            override fun onPreDraw(): Boolean {
                                binding.podImage.viewTreeObserver.removeOnPreDrawListener(this)
                                val imageViewHeight = binding.podImage.measuredHeight
                                val imageViewWidth = binding.podImage.measuredWidth
                                GlideApp.with(binding.podImage.context)
                                    .load(bitmapUtils.decodeSampledBitmap(path, imageViewWidth, imageViewHeight))
                                    .into(binding.podImage)
                                return true
                            }
                        })
                    }

                    binding.podContainer.setOnClickListener(null)
                    binding.deleteButton.setOnClickListener {
                        onDeleteClick(item.id)
                    }
                }
                DocketState.UPLOADING -> {
                    binding.root.visibility = View.VISIBLE
                    binding.addIcon.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.podImageCard.visibility = View.VISIBLE
                    binding.podImage.visibility = View.VISIBLE
                    binding.deleteButton.visibility = View.GONE

                    // Load image while uploading
                    item.imagePath?.let { path ->
                        binding.podImage.viewTreeObserver.addOnPreDrawListener(object : android.view.ViewTreeObserver.OnPreDrawListener {
                            override fun onPreDraw(): Boolean {
                                binding.podImage.viewTreeObserver.removeOnPreDrawListener(this)
                                val imageViewHeight = binding.podImage.measuredHeight
                                val imageViewWidth = binding.podImage.measuredWidth
                                GlideApp.with(binding.podImage.context)
                                    .load(bitmapUtils.decodeSampledBitmap(path, imageViewWidth, imageViewHeight))
                                    .into(binding.podImage)
                                return true
                            }
                        })
                    }

                    binding.podContainer.setOnClickListener(null)
                    binding.deleteButton.setOnClickListener(null)
                }
                DocketState.UPLOADED -> {
                    binding.root.visibility = View.VISIBLE
                    binding.addIcon.visibility = View.GONE
                    binding.deleteButton.visibility = View.VISIBLE

                    // Load image from path
                    if (item.imagePath != null) {
                        binding.progressBar.visibility = View.GONE
                        binding.podImageCard.visibility = View.VISIBLE
                        binding.podImage.visibility = View.VISIBLE
                        
                        binding.podImage.viewTreeObserver.addOnPreDrawListener(object : android.view.ViewTreeObserver.OnPreDrawListener {
                            override fun onPreDraw(): Boolean {
                                binding.podImage.viewTreeObserver.removeOnPreDrawListener(this)
                                val imageViewHeight = binding.podImage.measuredHeight
                                val imageViewWidth = binding.podImage.measuredWidth
                                GlideApp.with(binding.podImage.context)
                                    .load(bitmapUtils.decodeSampledBitmap(item.imagePath, imageViewWidth, imageViewHeight))
                                    .into(binding.podImage)
                                return true
                            }
                        })
                    } else {
                        // Image is being pre-fetched/downloaded
                        binding.progressBar.visibility = View.VISIBLE
                        binding.podImageCard.visibility = View.GONE
                        binding.podImage.visibility = View.GONE
                    }

                    binding.podContainer.setOnClickListener {
                        // View image in full screen (viewImage will handle download if needed)
                        onDocketClick(item.id)
                    }
                    binding.deleteButton.setOnClickListener {
                        onDeleteClick(item.id)
                    }
                }
            }
        }
    }

    class DocketDiffCallback(
        private val oldList: List<DocketItem>,
        private val newList: List<DocketItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

