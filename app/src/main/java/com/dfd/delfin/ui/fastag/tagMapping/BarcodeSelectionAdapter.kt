package com.dfd.delfin.ui.fastag.tagMapping

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dfd.delfin.databinding.ItemBarcodeSelectionBinding

class BarcodeSelectionAdapter(
    private var barcodes: List<String>,
    private var selectedBarcode: String?,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<BarcodeSelectionAdapter.BarcodeViewHolder>() {

    inner class BarcodeViewHolder(
        private val binding: ItemBarcodeSelectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(barcode: String) {
            binding.tvBarcodeNumber.text = barcode
            binding.rbSelect.isChecked = barcode == selectedBarcode

            binding.rootItem.setOnClickListener {
                selectedBarcode = barcode
                notifyDataSetChanged()
                onItemClick(barcode)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeViewHolder {
        val binding = ItemBarcodeSelectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BarcodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BarcodeViewHolder, position: Int) {
        holder.bind(barcodes[position])
    }

    override fun getItemCount(): Int = barcodes.size

    fun updateList(newBarcodes: List<String>) {
        barcodes = newBarcodes
        notifyDataSetChanged()
    }
}
