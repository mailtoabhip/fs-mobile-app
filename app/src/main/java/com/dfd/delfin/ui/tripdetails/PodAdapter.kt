package com.dfd.delfin.ui.tripdetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dfd.delfin.data.PodItem
import com.dfd.delfin.data.PodState
import com.dfd.delfin.databinding.ItemPodBinding
import com.dfd.delfin.injection.module.GlideApp
import com.dfd.delfin.utils.BitmapUtils

/**
 * Adapter for displaying POD items in RecyclerView
 */
class PodAdapter(
    private val onPodClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit,
    private val bitmapUtils: BitmapUtils
) : RecyclerView.Adapter<PodAdapter.PodViewHolder>() {

    private var items: List<PodItem> = emptyList()

    fun updateItems(newItems: List<PodItem>) {
        val filterItems = newItems.filter { it.state != PodState.EMPTY }

        val diffCallback = PodDiffCallback(this.items, filterItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.items = filterItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodViewHolder {
        val binding = ItemPodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PodViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class PodViewHolder(
        private val binding: ItemPodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PodItem) {
            when (item.state) {
                PodState.EMPTY -> {
                    binding.root.visibility = ViewGroup.GONE
                }
                PodState.AVAILABLE -> {
                    binding.root.visibility = ViewGroup.VISIBLE
                    binding.addIcon.visibility = ViewGroup.VISIBLE
                    binding.progressBar.visibility = ViewGroup.GONE
                    binding.podImageCard.visibility = ViewGroup.GONE
                    binding.podImage.visibility = ViewGroup.GONE
                    binding.deleteButton.visibility = ViewGroup.GONE
                    binding.podContainer.setOnClickListener {
                        onPodClick(item.id)
                    }
                    binding.deleteButton.setOnClickListener(null)
                }
                PodState.SELECTED -> {
                    binding.root.visibility = ViewGroup.VISIBLE
                    binding.addIcon.visibility = ViewGroup.GONE
                    binding.progressBar.visibility = ViewGroup.GONE
                    binding.podImageCard.visibility = ViewGroup.VISIBLE
                    binding.podImage.visibility = ViewGroup.VISIBLE
                    binding.deleteButton.visibility = ViewGroup.VISIBLE
                    
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
                    
                    binding.podContainer.setOnClickListener (null)
                    binding.deleteButton.setOnClickListener {
                        onDeleteClick(item.id)
                    }
                }
                PodState.UPLOADING -> {
                    binding.root.visibility = ViewGroup.VISIBLE
                    binding.addIcon.visibility = ViewGroup.GONE
                    //binding.progressBar.visibility = ViewGroup.VISIBLE
                    binding.podImageCard.visibility = ViewGroup.VISIBLE
                    binding.podImage.visibility = ViewGroup.VISIBLE
                    binding.deleteButton.visibility = ViewGroup.GONE
                    binding.podContainer.setOnClickListener(null)
                    binding.deleteButton.setOnClickListener(null)

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
                }
                PodState.UPLOADED -> {
                    binding.root.visibility = ViewGroup.VISIBLE
                    binding.addIcon.visibility = ViewGroup.GONE
                    binding.progressBar.visibility = ViewGroup.GONE
                    binding.podImageCard.visibility = ViewGroup.VISIBLE
                    binding.podImage.visibility = ViewGroup.VISIBLE
                    binding.deleteButton.visibility = ViewGroup.VISIBLE
                    
                    // Load image
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
                    } ?: item.imageUrl?.let { url ->
                        GlideApp.with(binding.podImage.context)
                            .load(url)
                            .into(binding.podImage)
                    }
                    
                    binding.podContainer.setOnClickListener {
                        // View image (optional - can open full screen)
                    }
                    binding.deleteButton.setOnClickListener {
                        onDeleteClick(item.id)
                    }
                }
            }
        }
    }

    class PodDiffCallback(
        private val oldList: List<PodItem>,
        private val newList: List<PodItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

