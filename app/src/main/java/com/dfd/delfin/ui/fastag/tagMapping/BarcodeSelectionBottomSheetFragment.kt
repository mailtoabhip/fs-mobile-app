package com.dfd.delfin.ui.fastag.tagMapping

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dfd.delfin.R
import com.dfd.delfin.databinding.DialogBottomBarcodeSelectionBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager

class BarcodeSelectionBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: DialogBottomBarcodeSelectionBinding? = null
    private val binding get() = _binding!!

    private var barcodeList: List<String> = emptyList()
    private var selectedBarcode: String? = null
    private var onBarcodeSelected: ((String) -> Unit)? = null
    private lateinit var adapter: BarcodeSelectionAdapter

    companion object {
        fun newInstance(
            barcodes: List<String>,
            selectedBarcode: String? = null,
            onBarcodeSelected: (String) -> Unit
        ): BarcodeSelectionBottomSheetFragment {
            val fragment = BarcodeSelectionBottomSheetFragment()
            fragment.barcodeList = barcodes
            fragment.selectedBarcode = selectedBarcode
            fragment.onBarcodeSelected = onBarcodeSelected
            return fragment
        }
    }

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBottomBarcodeSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupCloseButton()
    }

    override fun onStart() {
        super.onStart()
        val behavior = (dialog as? BottomSheetDialog)?.behavior ?: return
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }

    private fun setupRecyclerView() {
        adapter = BarcodeSelectionAdapter(
            barcodes = barcodeList,
            selectedBarcode = selectedBarcode
        ) { barcode ->
            onBarcodeSelected?.invoke(barcode)
            dismiss()
        }
        binding.rvBarcodes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBarcodes.adapter = adapter
        updateEmptyState(barcodeList.isEmpty())
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                val filteredList = if (query.isEmpty()) {
                    barcodeList
                } else {
                    barcodeList.filter { it.contains(query, ignoreCase = true) }
                }
                adapter.updateList(filteredList)
                updateEmptyState(filteredList.isEmpty())
            }
        })
    }

    private fun setupCloseButton() {
        binding.ivClose.setOnClickListener { dismiss() }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvBarcodes.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
