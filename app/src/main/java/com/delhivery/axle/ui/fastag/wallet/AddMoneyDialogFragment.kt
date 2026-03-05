package com.delhivery.axle.ui.fastag.wallet

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.delhivery.axle.R
import com.delhivery.axle.databinding.AddMoneyBottomSheetBinding
import com.delhivery.axle.ui.payment.PaymentWebViewActivity
import com.delhivery.axle.utils.extensions.setEnabledState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddMoneyDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: AddMoneyBottomSheetBinding
    private lateinit var viewModel: AddMoneyDialogViewmodel

    private var deeplink: String = ""
    private var onPaymentResult: ((success: Boolean) -> Unit)? = null

    companion object {
        private const val ARG_DEEPLINK = "deeplink"

        fun newInstance(
            redirectUrl: String,
            viewModelFactory: ViewModelProvider.Factory,
            onPaymentResult: (success: Boolean) -> Unit
        ): AddMoneyDialogFragment {
            val fragment = AddMoneyDialogFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_DEEPLINK, redirectUrl)
            }
            fragment.viewModelFactory = viewModelFactory
            fragment.onPaymentResult = onPaymentResult
            return fragment
        }
    }

    // Received from the calling activity
    private lateinit var viewModelFactory: ViewModelProvider.Factory

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

        viewModel = ViewModelProvider(this, viewModelFactory).get(AddMoneyDialogViewmodel::class.java)

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

        binding.etAmount.addTextChangedListener { text ->
            binding.btnProceedToPay.setEnabledState(!text.isNullOrBlank())
        }

        binding.tvQuick500.setOnClickListener {
            addQuickAmount(500)
        }
        binding.tvQuick1000.setOnClickListener {
            addQuickAmount(1000)
        }
        binding.tvQuick5000.setOnClickListener {
            addQuickAmount(5000)
        }

        binding.btnProceedToPay.setOnClickListener {
            val amountText = binding.etAmount.text.toString().trim()
            if (amountText.isNotEmpty()) {
                viewModel.callWalletRecharge(
                    amount   = amountText.toInt(),
//                    deeplink = deeplink
                )
            }
        }
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
            binding.btnProceedToPay.setEnabledState(!isLoading)
        }

        viewModel.exceptionLiveData.observe(viewLifecycleOwner) { throwable ->
            throwable?.let {
                // Do nothing
            }
        }
    }

    private fun addQuickAmount(delta: Int) {
        val current = binding.etAmount.text.toString().toIntOrNull() ?: 0
        binding.etAmount.setText((current + delta).toString())
        binding.etAmount.setSelection(binding.etAmount.text?.length ?: 0)
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
                        viewModelFactory = viewModelFactory,
                        rechargeId       = viewModel.currentRechargeId,  // store this after initiate
                        startDate        = viewModel.rechargeStartDate    // store this after initiate
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
}