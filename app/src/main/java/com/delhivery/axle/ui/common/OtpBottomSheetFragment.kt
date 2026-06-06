package com.delhivery.axle.ui.common

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.delhivery.axle.databinding.BottomSheetOtpBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OtpBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also { dialog ->
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onStart() {
        super.onStart()
        val behavior = (dialog as? BottomSheetDialog)?.behavior ?: return
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isFitToContents = true
        behavior.skipCollapsed = true
        behavior.isDraggable = false
    }

    private var _binding: BottomSheetOtpBinding? = null
    private val binding get() = _binding!!

    private var maskedNumber: String = ""
    private var countDownTimer: CountDownTimer? = null
    private var onOtpSubmit: ((String) -> Unit)? = null
    private var onResendOtp: (() -> Unit)? = null

    private lateinit var otpFields: List<EditText>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetOtpBinding.inflate(inflater, container, false)
        binding.maskedNumber = maskedNumber
        binding.isOtpFilled = false
        binding.isLoading = false
        binding.timerText = "00:30"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        otpFields = listOf(
            binding.etOtp1, binding.etOtp2, binding.etOtp3,
            binding.etOtp4, binding.etOtp5, binding.etOtp6
        )

        setupOtpInputs()
        setupClickListeners()
        startResendTimer()
    }

    private fun setupOtpInputs() {
        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // Hide error when user starts typing
                    if (s?.isNotEmpty() == true) hideOtpError()
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        otpFields[index + 1].requestFocus()
                    }
                    binding.isOtpFilled = isAllFieldsFilled()
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL
                    && event.action == android.view.KeyEvent.ACTION_DOWN
                    && editText.text.isEmpty()
                    && index > 0
                ) {
                    otpFields[index - 1].requestFocus()
                    otpFields[index - 1].text.clear()
                }
                false
            }
        }
    }

    private fun setupClickListeners() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.btnSubmit.setOnClickListener {
            val otp = getOtp()
            if (otp.length == 6) {
                onOtpSubmit?.invoke(otp)
            }
        }

        binding.tvResend.setOnClickListener {
            onResendOtp?.invoke()
            binding.tvResend.visibility = View.GONE
            binding.tvResendTimer.visibility = View.VISIBLE
            startResendTimer()
        }
    }

    private fun startResendTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.timerText = String.format("00:%02d", seconds)
            }

            override fun onFinish() {
                binding.tvResendTimer.visibility = View.GONE
                binding.tvResend.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun isAllFieldsFilled(): Boolean {
        return otpFields.all { it.text.length == 1 }
    }

    private fun getOtp(): String {
        return otpFields.joinToString("") { it.text.toString() }
    }

    fun clearOtp() {
        otpFields.forEach { it.text.clear() }
        otpFields.first().requestFocus()
        binding.isOtpFilled = false
        binding.isLoading = false
    }

    fun showLoading() {
        binding.isLoading = true
    }

    fun hideLoading() {
        binding.isLoading = false
    }

    fun showOtpError(message: String) {
        binding.tvOtpError.text = message
        binding.tvOtpError.visibility = View.VISIBLE
        // Change OTP field borders to red
        otpFields.forEach { it.setBackgroundResource(com.delhivery.axle.R.drawable.bg_edit_text_error) }
    }

    fun hideOtpError() {
        binding.tvOtpError.visibility = View.GONE
        otpFields.forEach { it.setBackgroundResource(com.delhivery.axle.R.drawable.bg_edit_text_outline) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }

    companion object {
        const val TAG = "OtpBottomSheet"

        fun newInstance(
            maskedNumber: String,
            onSubmit: (String) -> Unit,
            onResend: (() -> Unit)? = null
        ): OtpBottomSheetFragment {
            return OtpBottomSheetFragment().apply {
                this.maskedNumber = maskedNumber
                this.onOtpSubmit = onSubmit
                this.onResendOtp = onResend
            }
        }
    }
}
