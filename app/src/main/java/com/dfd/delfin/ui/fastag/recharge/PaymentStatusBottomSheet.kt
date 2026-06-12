package com.dfd.delfin.ui.fastag.recharge

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dfd.delfin.R
import com.dfd.delfin.databinding.BottomSheetPaymentStatusBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PaymentStatusBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPaymentStatusBinding? = null
    private val binding get() = _binding!!

    private var onDismissCallback: (() -> Unit)? = null

    enum class Status { SUCCESS, FAILED, PROCESSING }

    companion object {
        private const val ARG_STATUS = "status"

        fun newInstance(status: Status): PaymentStatusBottomSheet {
            return PaymentStatusBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_STATUS, status.name)
                }
            }
        }
    }

    fun setOnDismissCallback(callback: () -> Unit): PaymentStatusBottomSheet {
        onDismissCallback = callback
        return this
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPaymentStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val status = Status.valueOf(
            arguments?.getString(ARG_STATUS) ?: Status.PROCESSING.name
        )

        setupUI(status)

        binding.ivClose.setOnClickListener { dismiss() }
    }

    private fun setupUI(status: Status) {
        when (status) {
            Status.SUCCESS -> {
                binding.ivStatusIcon.setImageResource(R.drawable.fs_payment_success)
                binding.tvStatusTitle.text = "Payment Success"
                binding.tvStatusDescription.text = "Your FASTag is recharged successfully"
            }
            Status.FAILED -> {
                binding.ivStatusIcon.setImageResource(R.drawable.fs_payment_failed)
                binding.tvStatusTitle.text = "Payment failed"
                binding.tvStatusDescription.text = "If money was deducted, it will be refunded soon."
            }
            Status.PROCESSING -> {
                binding.ivStatusIcon.setImageResource(R.drawable.fs_payment_pending)
                binding.tvStatusTitle.text = "Payment in process"
                binding.tvStatusDescription.text =
                    "Your recharge request has been received and is being processed. The FASTag balance will update shortly."
            }
        }

        binding.ivStatusIcon.alpha = 0f
        binding.ivStatusIcon.scaleX = 0.5f
        binding.ivStatusIcon.scaleY = 0.5f
        binding.ivStatusIcon.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(400).start()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}