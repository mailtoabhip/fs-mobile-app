package com.delhivery.axle.ui.loadwallet

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogWalletFilterBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Filter bottom sheet for wallet history
 */
class WalletFilterBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: DialogWalletFilterBinding
    private var onApplyCallback: ((WalletFilter) -> Unit)? = null
    private var currentFilter: WalletFilter = WalletFilter()
    private var showTypeFilter: Boolean = true

    // Custom date range
    private var customStartDate: Calendar? = null
    private var customEndDate: Calendar? = null

    companion object {
        fun newInstance(
            currentFilter: WalletFilter = WalletFilter(),
            showTypeFilter: Boolean = true,
            onApply: (WalletFilter) -> Unit
        ): WalletFilterBottomSheet {
            val fragment = WalletFilterBottomSheet()
            fragment.currentFilter = currentFilter
            fragment.showTypeFilter = showTypeFilter
            fragment.onApplyCallback = onApply
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogWalletFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!showTypeFilter) {
            binding.labelType.visibility = View.GONE
            binding.chipGroupType.visibility = View.GONE
        }

        setupClickListeners()
        restoreSelection()
    }

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialog

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheetDialog =
                dialog as com.google.android.material.bottomsheet.BottomSheetDialog
            val bottomSheetInternal = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheetInternal?.let { bottomSheet ->
                bottomSheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                bottomSheet.requestLayout()
            }

            val behavior = bottomSheetDialog.behavior
            behavior.isFitToContents = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isDraggable = true
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener { dismiss() }

        // Disable apply button initially if no date range selected
        updateApplyButtonState()

        // Listen for date range chip selection changes
        binding.chipGroupDate.setOnCheckedChangeListener { _, _ ->
            updateApplyButtonState()
        }

        binding.btnApply.setOnClickListener {
            val typeFilter = when {
                binding.chipCredit.isChecked -> FilterType.CREDIT
                binding.chipDebit.isChecked -> FilterType.DEBIT
                else -> null
            }

            val dateFilter = when {
                binding.chipToday.isChecked -> FilterDateRange.TODAY
                binding.chipYesterday.isChecked -> FilterDateRange.YESTERDAY
                binding.chipThisWeek.isChecked -> FilterDateRange.THIS_WEEK
                binding.chipLastWeek.isChecked -> FilterDateRange.LAST_WEEK
                binding.chipThisMonth.isChecked -> FilterDateRange.THIS_MONTH
                binding.chipLastMonth.isChecked -> FilterDateRange.LAST_MONTH
                binding.chipLast90Days.isChecked -> FilterDateRange.LAST_90_DAYS
                else -> null
            }

            val filter = WalletFilter(
                type = typeFilter,
                dateRange = dateFilter,
                customStartDate = customStartDate,
                customEndDate = customEndDate
            )
            onApplyCallback?.invoke(filter)
            dismiss()
        }
    }

    private fun updateApplyButtonState() {
        val hasDateRange = binding.chipGroupDate.checkedChipId != View.NO_ID
        binding.btnApply.isEnabled = hasDateRange
        binding.btnApply.alpha = if (hasDateRange) 1.0f else 0.5f
    }

    private fun restoreSelection() {
        when (currentFilter.type) {
            FilterType.CREDIT -> binding.chipCredit.isChecked = true
            FilterType.DEBIT -> binding.chipDebit.isChecked = true
            else -> {}
        }
        when (currentFilter.dateRange) {
            FilterDateRange.TODAY -> binding.chipToday.isChecked = true
            FilterDateRange.YESTERDAY -> binding.chipYesterday.isChecked = true
            FilterDateRange.THIS_WEEK -> binding.chipThisWeek.isChecked = true
            FilterDateRange.LAST_WEEK -> binding.chipLastWeek.isChecked = true
            FilterDateRange.THIS_MONTH -> binding.chipThisMonth.isChecked = true
            FilterDateRange.LAST_MONTH -> binding.chipLastMonth.isChecked = true
            FilterDateRange.LAST_90_DAYS -> binding.chipLast90Days.isChecked = true
            else -> {}
        }
    }

    private fun showDateRangePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            customStartDate = Calendar.getInstance().apply { set(year, month, day) }
            // Pick end date
            DatePickerDialog(requireContext(), { _, y2, m2, d2 ->
                customEndDate = Calendar.getInstance().apply { set(y2, m2, d2) }
                updateCustomRangeLabel()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateCustomRangeLabel() {
        val fmt = SimpleDateFormat("d MMM", Locale.getDefault())
        val start = customStartDate?.let { fmt.format(it.time) } ?: ""
        val end = customEndDate?.let { fmt.format(it.time) } ?: ""
    }
}

/**
 * Wallet filter data
 */
data class WalletFilter(
    val type: FilterType? = null,
    val dateRange: FilterDateRange? = null,
    val customStartDate: Calendar? = null,
    val customEndDate: Calendar? = null
)

enum class FilterType(val label: String) {
    CREDIT("Credit"),
    DEBIT("Debit")
}

enum class FilterDateRange(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    LAST_WEEK("Last Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_90_DAYS("Last 90 Days"),
    CUSTOM("Custom Range")
}
