package com.delhivery.axle.ui.fastag.wallet

import android.app.Activity
import android.content.Intent
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
            deeplink: String,
            viewModelFactory: ViewModelProvider.Factory,
            onPaymentResult: (success: Boolean) -> Unit
        ): AddMoneyDialogFragment {
            val fragment = AddMoneyDialogFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_DEEPLINK, deeplink)
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

        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(AddMoneyDialogViewmodel::class.java)

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
        dialog?.window?.setGravity(Gravity.BOTTOM)
    }

    private fun setupClickListeners() {
        binding.ivClose.setOnClickListener { dismiss() }

        binding.etAmount.addTextChangedListener { text ->
            binding.btnProceedToPay.isEnabled = !text.isNullOrBlank()
        }

        binding.tvQuick500.setOnClickListener {
            val current = binding.etAmount.text.toString().toIntOrNull() ?: 0
            binding.etAmount.setText((current + 500).toString())
        }
        binding.tvQuick1000.setOnClickListener {
            val current = binding.etAmount.text.toString().toIntOrNull() ?: 0
            binding.etAmount.setText((current + 1000).toString())
        }
        binding.tvQuick5000.setOnClickListener {
            val current = binding.etAmount.text.toString().toIntOrNull() ?: 0
            binding.etAmount.setText((current + 5000).toString())
        }

        binding.btnProceedToPay.setOnClickListener {
            val amountText = binding.etAmount.text.toString().trim()
            if (amountText.isNotEmpty()) {
                viewModel.initiateRecharge(
                    amount   = amountText.toInt(),
                    deeplink = deeplink
                )
            }
        }
    }

    private fun setupObservers() {
        // Recharge initiated → open WebView
        viewModel.rechargeInitLiveData.observe(viewLifecycleOwner) { result ->
            if (result != null) {
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
            binding.btnProceedToPay.isEnabled = !isLoading
        }

        viewModel.exceptionLiveData.observe(viewLifecycleOwner) { throwable ->
            throwable?.let {
                // Do nothing
            }
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