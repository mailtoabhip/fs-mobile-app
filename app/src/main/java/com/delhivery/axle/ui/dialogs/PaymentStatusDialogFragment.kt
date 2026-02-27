package com.delhivery.axle.ui.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FsPaymentStatusBinding

/**
 * Full-screen payment status dialog.
 *
 * Shows success / failure / pending state using [fs_payment_status.xml].
 * The UI is driven by an internal [MutableLiveData], so the state can be
 * updated at any point while the dialog is visible — e.g. when polling
 * returns a final result after the dialog was initially shown as pending.
 *
 * ── Showing from an Activity ──────────────────────────────────────────────
 *
 *   val dialog = PaymentStatusDialogFragment.show(
 *       fragmentManager  = supportFragmentManager,
 *       initialStatus    = PaymentStatus.PENDING,
 *       onDismiss        = { /* optional cleanup */ }
 *   )
 *
 *   // Later — e.g. inside a polling callback:
 *   dialog.updateStatus(PaymentStatus.SUCCESS)
 *
 * ── Showing from a Fragment ───────────────────────────────────────────────
 *
 *   val dialog = PaymentStatusDialogFragment.show(
 *       fragmentManager = parentFragmentManager,
 *       initialStatus   = PaymentStatus.PENDING
 *   )
 *
 * ── Updating without holding a reference ──────────────────────────────────
 *
 *   PaymentStatusDialogFragment.updateStatus(
 *       fragmentManager = supportFragmentManager,
 *       status          = PaymentStatus.FAILURE
 *   )
 *
 * NOTE: Add the three drawables to res/drawable/ before shipping:
 *   • fs_payment_success
 *   • fs_payment_failed
 *   • fs_payment_pending
 */
class PaymentStatusDialogFragment : DialogFragment() {

    // ── Binding ───────────────────────────────────────────────────────────

    private var _binding: FsPaymentStatusBinding? = null
    private val binding get() = _binding!!

    // ── State ─────────────────────────────────────────────────────────────

    /**
     * Single source of truth for the displayed state.
     * The UI observer in [onViewCreated] reacts to every emission,
     * so calling [updateStatus] — even from a background thread — is safe.
     */
    private val statusLiveData = MutableLiveData<PaymentStatus>()

    // ── Callbacks ─────────────────────────────────────────────────────────

    private var onDismissCallback: (() -> Unit)? = null

    // ── Companion (public API) ────────────────────────────────────────────

    companion object {

        /** Tag used to find the dialog later via [FragmentManager.findFragmentByTag]. */
        const val TAG = "PaymentStatusDialog"

        private const val ARG_STATUS = "arg_status"

        /**
         * Show the payment status screen from any Activity or Fragment.
         *
         * @param fragmentManager  `supportFragmentManager` (Activity) or
         *                         `parentFragmentManager` (Fragment).
         * @param initialStatus    The state to display immediately.
         * @param onDismiss        Called when the dialog is dismissed for any reason.
         * @return                 The created fragment — hold the reference to call
         *                         [updateStatus] later without going through the manager.
         */
        fun show(
            fragmentManager: FragmentManager,
            initialStatus: PaymentStatus,
            onDismiss: (() -> Unit)? = null
        ): PaymentStatusDialogFragment {
            val fragment = PaymentStatusDialogFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_STATUS, initialStatus.name)
            }
            fragment.onDismissCallback = onDismiss
            fragment.show(fragmentManager, TAG)
            return fragment
        }

        /**
         * Update an already-visible dialog without holding a direct reference.
         * Safe no-op if the dialog is not currently shown.
         *
         * @param fragmentManager  The same manager that was used in [show].
         * @param status           The new state to render.
         */
        fun updateStatus(fragmentManager: FragmentManager, status: PaymentStatus) {
            (fragmentManager.findFragmentByTag(TAG) as? PaymentStatusDialogFragment)
                ?.updateStatus(status)
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // STYLE_NO_FRAME removes the dialog chrome; AppTheme applies the app
        // colours and fonts so the screen looks like a regular Activity.
        setStyle(STYLE_NO_FRAME, R.style.AppTheme)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FsPaymentStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Seed the LiveData with the status that was passed at show-time.
        val initialStatus = PaymentStatus.valueOf(
            arguments?.getString(ARG_STATUS) ?: PaymentStatus.PENDING.name
        )
        statusLiveData.value = initialStatus

        // One observer drives the entire UI. Any future call to updateStatus()
        // emits a new value here — no manual UI wiring needed at call sites.
        statusLiveData.observe(viewLifecycleOwner) { status ->
            renderState(status)
        }

        binding.ivClose.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        // Stretch the dialog window to fill the screen.
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        onDismissCallback = null
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Update the displayed state after the dialog is already showing.
     *
     * Thread-safe — uses [MutableLiveData.postValue] internally, so this
     * can be called from a background thread (e.g. inside a polling callback)
     * without wrapping in `runOnUiThread`.
     *
     * Example — update to success after polling resolves:
     *
     *   dialog.updateStatus(PaymentStatus.SUCCESS)
     */
    fun updateStatus(status: PaymentStatus) {
        statusLiveData.postValue(status)
    }

    // ── Rendering ─────────────────────────────────────────────────────────

    private fun renderState(status: PaymentStatus) {
        when (status) {
            PaymentStatus.SUCCESS -> {
                binding.statusIv.setImageResource(R.drawable.fs_payment_success)
                binding.tvStatusTitle.text = "Payment Successful!"
                binding.tvStatusDesc.text =
                    "Your money has been added to your Delhivery wallet."
            }

            PaymentStatus.FAILURE -> {
                binding.statusIv.setImageResource(R.drawable.fs_payment_failed)
                binding.tvStatusTitle.text = "Payment failed"
                binding.tvStatusDesc.text =
                    "If money was deducted, it will be refunded by your bank soon."
            }

            PaymentStatus.PENDING -> {
                binding.statusIv.setImageResource(R.drawable.fs_payment_pending)
                binding.tvStatusTitle.text = "Payment in process"
                binding.tvStatusDesc.text =
                    "Your payment is being processed. The amount will reflect in your wallet shortly."
            }
        }
    }
}

/**
 * Represents the three possible payment outcomes.
 *
 * Pass as the initial state when calling [PaymentStatusDialogFragment.show],
 * and push updates via [PaymentStatusDialogFragment.updateStatus] at any time
 * while the dialog is visible (e.g. after polling returns a final result).
 */
enum class PaymentStatus {
    SUCCESS,
    FAILURE,
    PENDING
}
