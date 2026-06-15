package com.dfd.delfin.ui.fastag.wallet

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.dfd.delfin.R
import com.dfd.delfin.databinding.AddMoneyBottomSheetBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.payment.PaymentWebViewActivity
import com.dfd.delfin.utils.extensions.setEnabledState
import com.dfd.delfin.utils.extensions.viewModelFactoryExtension
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddMoneyDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: AddMoneyBottomSheetBinding
    private val viewModel: AddMoneyDialogViewmodel by activityViewModels {
        viewModelFactoryExtension()
    }

    private var deeplink: String = ""
    private var onPaymentResult: ((success: Boolean) -> Unit)? = null

    companion object {
        private const val ARG_DEEPLINK = "deeplink"

        fun newInstance(
            redirectUrl: String,
            onPaymentResult: (success: Boolean) -> Unit
        ): AddMoneyDialogFragment {
            val fragment = AddMoneyDialogFragment()
            fragment.arguments = Bundle().apply { putString(ARG_DEEPLINK, redirectUrl) }
            fragment.onPaymentResult = onPaymentResult
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddMoneyBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deeplink = arguments?.getString(ARG_DEEPLINK) ?: ""

        binding.btnProceedToPay.setEnabledState(false)

        setupDialog()
        setupClickListeners()
        setupObservers()
    }

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialog

    override fun onStart() {
        super.onStart()
        val behavior = (dialog as? BottomSheetDialog)?.behavior ?: return
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = true
            behavior.skipCollapsed = true
        }

    private fun setupDialog() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog?.window?.setGravity(Gravity.BOTTOM)
    }

    private fun setupClickListeners() {
        binding.ivClose.setOnClickListener { dismiss() }

        // Restrict to max 2 decimal places
        binding.etAmount.filters = arrayOf(DecimalDigitsInputFilter(2))

        binding.etAmount.addTextChangedListener { text ->
            binding.btnProceedToPay.setEnabledState(validateAmount(text?.toString()))
        }

        binding.tvQuick500.setOnClickListener  { addQuickAmount(500)  }
        binding.tvQuick1000.setOnClickListener { addQuickAmount(1000) }
        binding.tvQuick5000.setOnClickListener { addQuickAmount(5000) }

        binding.btnProceedToPay.setOnClickListener {
            val amount =
                    binding.etAmount.text.toString().trim().toFloatOrNull()
                            ?: return@setOnClickListener
            viewModel.initiateRecharge(amount, deeplink)
        }
    }

    /**
     * Returns true if the amount is valid (₹1–₹100000).
     * Decimals are allowed only when amount >= 1, max 2 decimal places.
     * Shows/hides the error label as a side-effect.
     */
    private fun validateAmount(text: String?): Boolean {
        val trimmed = text?.trim()
        val amount = trimmed?.toFloatOrNull()
        
        // Check max 2 decimal places
        val hasValidDecimals = trimmed?.contains(".") != true || 
            (trimmed.substringAfter(".").length <= 2)
        
        val isValid = amount != null && amount >= 1.0f && amount <= 100_000.0f && hasValidDecimals
        val showError = !text.isNullOrBlank() && !isValid  // only show error when user has typed something
        binding.tvAmountError.visibility = if (showError) View.VISIBLE else View.GONE
        return isValid
    }

    private fun setupObservers() {
        // Recharge initiated → open WebView
        viewModel.rechargeInitLiveData.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                viewModel.rechargeInitLiveData.value = null
                val (paymentLink, _) = result
                val intent = PaymentWebViewActivity.createIntent(
                    context     = requireContext(),
                    paymentUrl  = paymentLink,
                    redirectUrl = deeplink,
                    title       = "Wallet Recharge"
                )
                webViewLauncher.launch(intent)
            }
            // null = initiation failed, error already shown via exceptionLiveData
        }

        viewModel.progressLiveData.observe(viewLifecycleOwner) { isLoading ->
            binding.rlProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // When loading finishes, re-validate instead of blindly enabling
            val canPay = !isLoading && validateAmount(binding.etAmount.text?.toString())
            binding.btnProceedToPay.setEnabledState(canPay)
        }

        viewModel.exceptionLiveData.observe(viewLifecycleOwner) { throwable ->
            throwable?.let {
                (activity as? BaseActivity<*, *>)?.errorUtils?.handle(it)
            }
        }
    }
    private fun addQuickAmount(delta: Int) {
        val current = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
        if (current >= 100_000.0) {
            Toast.makeText(requireContext(), "Maximum amount limit of ₹1,00,000 reached", Toast.LENGTH_SHORT).show()
            return
        }

        val newAmount = (current + delta).coerceAtMost(100_000.0)
        // Format: show decimals only if present, otherwise show as integer
        val formattedAmount = if (newAmount % 1.0 == 0.0) {
            newAmount.toInt().toString()
        } else {
            newAmount.toString()
        }
        binding.etAmount.setText(formattedAmount)
        binding.etAmount.setSelection(binding.etAmount.text?.length ?: 0)

        if (current + delta > 100_000.0) {
            Toast.makeText(requireContext(), "Amount capped at ₹1,00,000", Toast.LENGTH_SHORT).show()
        }
    }

    private val webViewLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            PaymentWebViewActivity.RESULT_SUCCESS -> {
                // Payment gateway redirect detected → start polling
                dismiss()
                PaymentCountdownBottomSheetFragment
                    .newInstance(
                        rechargeId       = viewModel.currentRechargeId,
                        startDate        = viewModel.rechargeStartDate
                    )
                    .show(parentFragmentManager, "PaymentCountdown")
            }
            PaymentWebViewActivity.RESULT_CANCELLED -> {
                // User closed WebView without paying — do nothing, stay on bottom sheet
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onPaymentResult = null
    }

    /**
     * InputFilter that restricts decimal input to a maximum number of digits after the decimal point.
     */
    private class DecimalDigitsInputFilter(private val decimalDigits: Int) : InputFilter {
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val newText = dest.substring(0, dstart) + source.substring(start, end) + dest.substring(dend)
            val dotIndex = newText.indexOf('.')
            if (dotIndex >= 0 && newText.length - dotIndex - 1 > decimalDigits) {
                return ""  // Reject input
            }
            return null  // Accept input
        }
    }
}