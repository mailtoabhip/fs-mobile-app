package com.delhivery.axle.ui.fastag.wallet

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.delhivery.axle.R
import com.delhivery.axle.databinding.PaymentStatusCountdownBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.PaymentStatus
import com.delhivery.axle.ui.dialogs.PaymentStatusDialogFragment
import com.delhivery.axle.utils.extensions.viewModelFactoryExtension
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject
import kotlin.getValue

class PaymentCountdownBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: PaymentStatusCountdownBinding
    private val viewModel: AddMoneyDialogViewmodel by activityViewModels {
        viewModelFactoryExtension()
    }
    private var countDownTimer: CountDownTimer? = null
    private var hasResolved = false  // guard so only one outcome fires
    private var tickCount = 0
    private lateinit var rechargeId: String
    private lateinit var startDate: String
    companion object {
        private const val ARG_RECHARGE_ID = "recharge_id"
        private const val ARG_START_DATE  = "start_date"
        private const val COUNTDOWN_MS    = 31_000L
        private const val INTERVAL_MS     =  1_000L
        fun newInstance(
            rechargeId: String,
            startDate: String
        ): PaymentCountdownBottomSheetFragment {
            val fragment = PaymentCountdownBottomSheetFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_RECHARGE_ID, rechargeId)
                putString(ARG_START_DATE, startDate)
            }
            return fragment
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
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
        rechargeId = arguments?.getString(ARG_RECHARGE_ID) ?: ""
        startDate  = arguments?.getString(ARG_START_DATE)  ?: ""

        startCountdown()
        observeStatus()
    }

    // ── Countdown ─────────────────────────────────────────────────────────

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(COUNTDOWN_MS, INTERVAL_MS) {

            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                binding.tvCountdownTitle.text = String.format("00:%02d", secondsLeft)

                tickCount++
                if (tickCount % 3 == 0) {
                    viewModel.checkTransactionStatus(rechargeId, startDate)
                }
            }

            override fun onFinish() {
                // Timer hit 0. If status hasn't resolved yet → show PENDING.
                if (!hasResolved) {
                    resolveWith(PaymentStatus.PENDING)
                }
            }
        }.start()
    }

    // ── Observe ViewModel status ──────────────────────────────────────────

    private fun observeStatus() {
        viewModel.paymentStatusLiveData.observe(viewLifecycleOwner) { status ->
            if (status != null && status != PaymentStatus.PENDING && !hasResolved) {
                // SUCCESS or FAILURE arrived before the timer ran out
                resolveWith(status)
                viewModel.paymentStatusLiveData.value = null
            }
        }
    }

    // ── Resolution ────────────────────────────────────────────────────────

    private fun resolveWith(status: PaymentStatus) {
        hasResolved = true
        countDownTimer?.cancel()
        dismiss()
        // Show the final status bottom sheet
        PaymentStatusDialogFragment.show(
            fragmentManager = parentFragmentManager,
            initialStatus   = status
        )
    }

    // ── Bottom sheet setup (same pattern as AddMoneyDialogFragment) ────────

    override fun onStart() {
        super.onStart()
        val behavior = (dialog as? BottomSheetDialog)?.behavior ?: return
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
        behavior.skipCollapsed = true
    }

    override fun getTheme() = R.style.TransparentBottomSheetDialog

    override fun onDestroyView() {
        countDownTimer?.cancel()
        super.onDestroyView()
    }
}