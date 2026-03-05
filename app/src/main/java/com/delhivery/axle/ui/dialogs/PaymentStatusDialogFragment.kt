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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/** Bottom sheet showing payment result (SUCCESS / FAILURE / PENDING). UI updates reactively via [statusLiveData]. */
class PaymentStatusDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FsPaymentStatusBinding? = null
    private val binding get() = _binding!!

    // Drives the entire UI — call updateStatus() to change state after showing.
    private val statusLiveData = MutableLiveData<PaymentStatus>()

    private var onDismissCallback: (() -> Unit)? = null

    companion object {

        const val TAG = "PaymentStatusDialog"
        private const val ARG_STATUS = "arg_status"

        /** Show the payment status bottom sheet. Returns the fragment so you can call [updateStatus] later. */
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

        /** Update an already-visible dialog without holding a reference. No-op if not shown. */
        fun updateStatus(fragmentManager: FragmentManager, status: PaymentStatus) {
            (fragmentManager.findFragmentByTag(TAG) as? PaymentStatusDialogFragment)
                ?.updateStatus(status)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialog)
        isCancelable = true
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

        val initialStatus = PaymentStatus.valueOf(
            arguments?.getString(ARG_STATUS) ?: PaymentStatus.PENDING.name
        )
        statusLiveData.value = initialStatus
        statusLiveData.observe(viewLifecycleOwner) { renderState(it) }

        binding.ivClose.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        val behavior = (dialog as? BottomSheetDialog)?.behavior ?: return
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
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

    /** Update the displayed state while the sheet is visible. Thread-safe. */
    fun updateStatus(status: PaymentStatus) {
        statusLiveData.postValue(status)
    }

    private fun renderState(status: PaymentStatus) {
        when (status) {
            PaymentStatus.SUCCESS -> {
                binding.statusIv.setImageResource(R.drawable.fs_payment_success)
                binding.tvStatusTitle.text = "Payment Successful!"
                binding.tvStatusDesc.text = "Your money has been added to your Delhivery wallet."
            }
            PaymentStatus.FAILURE -> {
                binding.statusIv.setImageResource(R.drawable.fs_payment_failed)
                binding.tvStatusTitle.text = "Payment failed"
                binding.tvStatusDesc.text = "If money was deducted, it will be refunded by your bank soon."
            }
            PaymentStatus.PENDING -> {
                binding.statusIv.setImageResource(R.drawable.fs_payment_pending)
                binding.tvStatusTitle.text = "Payment in process"
                binding.tvStatusDesc.text = "Your payment is being processed. The amount will reflect in your wallet shortly."
            }
        }
    }
}

/** Three possible payment outcomes passed to [PaymentStatusDialogFragment]. */
enum class PaymentStatus {
    SUCCESS,
    FAILURE,
    PENDING
}
