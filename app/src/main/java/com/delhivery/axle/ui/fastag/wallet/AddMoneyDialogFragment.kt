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
import androidx.lifecycle.ViewModelProvider
import com.delhivery.axle.R
import com.delhivery.axle.databinding.AddMoneyBottomSheetBinding
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
        dialog?.let { dlg ->
            val bottomSheetDialog = dlg as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                it.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                it.layoutParams = it.layoutParams
            }
            val behavior = bottomSheetDialog.behavior
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = true
            behavior.skipCollapsed = true
        }
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

        binding.btnProceedToPay.setOnClickListener {
            val amount = binding.etAmount.text.toString().trim()
            if (amount.isNotEmpty()) {
                viewModel.initiateRecharge(
                    amount   = amount.toInt(),
                    deeplink = deeplink       // the redirect URL passed to the API
                )
            }
        }
    }

    private fun onPaymentGatewayComplete() {
        // Called when WebView detects the redirect URL
        dismiss()
        PaymentCountdownBottomSheetFragment
            .newInstance(viewModelFactory)
            .show(parentFragmentManager, "PaymentCountdown")
    }

    private fun setupObservers() {
        // Recharge initiated → open WebView activity
        viewModel.rechargeInitLiveData.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                val (paymentLink, rechargeId) = result
                val intent = Intent(requireContext(), PaymentWebViewActivity::class.java).apply {
                    putExtra(PaymentWebViewActivity.EXTRA_PAYMENT_URL, paymentLink)
                    putExtra(PaymentWebViewActivity.EXTRA_RECHARGE_ID, rechargeId)
                    putExtra(PaymentWebViewActivity.EXTRA_REDIRECT_URL, deeplink)
                }
                webViewLauncher.launch(intent)
            } else {
                // initiation failed — error is already posted to exceptionLiveData
            }
        }

        // Loading state
        viewModel.progressLiveData.observe(viewLifecycleOwner) { isLoading ->
            binding.rlProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnProceedToPay.isEnabled = !isLoading
        }

        // Error
        viewModel.exceptionLiveData.observe(viewLifecycleOwner) { throwable ->
            throwable?.let {
                // show a toast or snackbar with it.message
            }
        }
    }

    private val webViewLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val rechargeId = result.data?.getStringExtra(PaymentWebViewActivity.EXTRA_RECHARGE_ID) ?: return@registerForActivityResult
            val startDate  = result.data?.getStringExtra(PaymentWebViewActivity.EXTRA_START_DATE)  ?: return@registerForActivityResult
            dismiss()
            PaymentCountdownBottomSheetFragment
                .newInstance(viewModelFactory, rechargeId, startDate)
                .show(parentFragmentManager, "PaymentCountdown")
        }
        // If RESULT_CANCELED → user closed WebView without paying → do nothing, stay on bottom sheet
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onPaymentResult = null
    }
}