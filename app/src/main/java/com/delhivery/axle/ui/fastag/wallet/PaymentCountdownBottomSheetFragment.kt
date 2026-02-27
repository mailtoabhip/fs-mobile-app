package com.delhivery.axle.ui.fastag.wallet

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.delhivery.axle.databinding.PaymentStatusCountdownBinding
import com.delhivery.axle.ui.dialogs.PaymentStatus
import com.delhivery.axle.ui.dialogs.PaymentStatusDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PaymentCountdownBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: PaymentStatusCountdownBinding
    private lateinit var viewModel: AddMoneyDialogViewmodel
    private lateinit var viewModelFactory: ViewModelProvider.Factory

    private var countDownTimer: CountDownTimer? = null
    private var hasResolved = false  // guard so only one outcome fires
    private var tickCount = 0
    private lateinit var rechargeId: String
    private lateinit var startDate: String

    companion object {
        private const val ARG_RECHARGE_ID = "recharge_id"
        private const val ARG_START_DATE  = "start_date"

        fun newInstance(
            viewModelFactory: ViewModelProvider.Factory,
            rechargeId: String,
            startDate: String
        ): PaymentCountdownBottomSheetFragment {
            val fragment = PaymentCountdownBottomSheetFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_RECHARGE_ID, rechargeId)
                putString(ARG_START_DATE, startDate)
            }
            fragment.viewModelFactory = viewModelFactory
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PaymentStatusCountdownBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        rechargeId = arguments?.getString(ARG_RECHARGE_ID) ?: ""
        startDate  = arguments?.getString(ARG_START_DATE)  ?: ""
        viewModel  = ViewModelProvider(requireActivity(), viewModelFactory)
            .get(AddMoneyDialogViewmodel::class.java)
        startCountdown()
        observeStatus()
    }

    // ── Countdown ─────────────────────────────────────────────────────────

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(COUNTDOWN_MS, INTERVAL_MS) {

            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                // format as "00:30", "00:29" ... "00:00"
                binding.tvCountdownTitle.text =
                    String.format("00:%02d", secondsLeft)
            }

            override fun onFinish() {
                // Timer hit 0. If status hasn't resolved yet → show PENDING.
                if (!hasResolved) {
                    resolveWith(PaymentStatus.PENDING)
                }
            }
        }.start()
    }

    override fun onTick(millisUntilFinished: Long) {
        val secondsLeft = (millisUntilFinished / 1000).toInt()
        binding.tvCountdownTitle.text = String.format("00:%02d", secondsLeft)

        tickCount++
        if (tickCount % 3 == 0) {
            viewModel.checkTransactionStatus(rechargeId, startDate)
        }
    }

    // ── Observe ViewModel status ──────────────────────────────────────────

    private fun observeStatus() {
        viewModel.paymentStatusLiveData.observe(viewLifecycleOwner) { status ->
            if (status != null && status != PaymentStatus.PENDING && !hasResolved) {
                // SUCCESS or FAILURE arrived before the timer ran out
                resolveWith(status)
            }
        }
    }

    // ── Resolution ────────────────────────────────────────────────────────

    private fun resolveWith(status: PaymentStatus) {
        hasResolved = true
        countDownTimer?.cancel()
        dismiss()
        // Show the final status full-screen dialog
        PaymentStatusDialogFragment.show(
            fragmentManager = parentFragmentManager,
            initialStatus   = status
        )
    }

    // ── Bottom sheet setup (same pattern as AddMoneyDialogFragment) ────────

    override fun onStart() {
        super.onStart()
        dialog?.let { dlg ->
            val bottomSheetDialog = dlg as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
            val behavior = bottomSheetDialog.behavior
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false  // prevent swipe during countdown
            behavior.skipCollapsed = true
        }
    }

    override fun getTheme() = com.delhivery.axle.R.style.TransparentBottomSheetDialog

    override fun onDestroyView() {
        countDownTimer?.cancel()
        super.onDestroyView()
    }
}