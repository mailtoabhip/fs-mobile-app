package com.delhivery.axle.ui.custom

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.delhivery.axle.databinding.ViewDelhiveryOtpBinding
import com.delhivery.axle.databinding.ViewDelhiveryOtpItemBinding
import com.delhivery.axle.utils.extensions.errorVibrate

/**
 * Custom implementation of [FrameLayout] for OTP enter view
 */
class DelhiveryOTPViewSix(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var otp = arrayOfNulls<OTPViewIDValuePair>(OTPDigitsCount)

    /* view binding */
    private val binding: ViewDelhiveryOtpBinding =
        ViewDelhiveryOtpBinding.inflate(LayoutInflater.from(context), this, true)

    /* View interface */
    var otpViewInterface: DelhiveryOTPViewInterfaceSix? = null

    init {
        addOTPFields()
    }

    private fun addOTPFields() {
        binding.container.removeAllViews()
        for (i in 0 until OTPDigitsCount) {
            val otpFieldBinding =
                ViewDelhiveryOtpItemBinding.inflate(LayoutInflater.from(context), binding.container, false)
            val id = View.generateViewId()
            if (otp[i] == null) {
                otp[i] = OTPViewIDValuePair(id)
            } else {
                otp[i]!!.viewId = id
            }
            otpFieldBinding.editOtp.apply {
                this.id = id
                setText(otp[i]?.digit?.toString())
                addTextChangedListener(OTPFieldTextWatcher(i))
                setOnKeyListener(OTPFieldKeyListener(i))
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        otpFieldBinding.editOtp.setText("")
                    }
                }
            }
            binding.container.addView(otpFieldBinding.root)
        }
    }

    /**
     * OTP submitted from
     */
    private fun otpSubmitted() {
        var nullDigitIndex = -1
        for (i in 0 until OTPDigitsCount) {
            if (otp[i]?.digit == null) {
                nullDigitIndex = i
                break
            }
        }
        if (nullDigitIndex == -1) {
            /* submit as all digits are found */
            val _otp = otp.map { it!!.digit!! }
                .toCharArray()
            otpViewInterface?.otpSubmitted(_otp)
        } else {
            /* null digit found, focus back to it */
            focusOTPField(nullDigitIndex, true, true)
        }
    }

    /**
     * OTP Field Key listener for back functionality
     */
    inner class OTPFieldKeyListener(private val index: Int) : OnKeyListener {
        override fun onKey(
            v: View?,
            keyCode: Int,
            event: KeyEvent?
        ): Boolean {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                when (index > 0) {
                    true -> {
                        focusOTPField(index - 1)
                    }
                    false -> {
                        /* at position 0, delete makes no */
                    }
                }
            }
            return false
        }
    }

    /**
     * OTP FIeld Text watcher
     */
    inner class OTPFieldTextWatcher(private val index: Int) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            otp[index]?.digit = s.toString()
                .trim()
                .firstOrNull()

            when (otp[index]?.digit == null) {
                false ->  //otp digit entered
                    when (index < OTPDigitsCount - 1) {
                        true -> {
                            focusOTPField(index + 1)
                        }
                        false -> {
                            /* submit */
                            otpSubmitted()
                        }
                    }
                else ->{}
            }
        }

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
        }
    }

    /**
     * Request on OTP Field by index
     */
    private fun focusOTPField(
        index: Int,
        clearText: Boolean = true,
        animate: Boolean = false
    ) {
        viewAtIndex(index)?.let {
            it.requestFocus()
            if (clearText) {
                it.setText("")
            }
            if (animate) {
                it.errorVibrate()
            }
        }
    }

    /**
     * View at index
     */
    private fun viewAtIndex(index: Int) = otp[index]?.viewId?.let { viewId ->
        binding.container.findViewById<View>(viewId) as DelhiveryOTPViewEditText
    }

    /**
     * Clear otp fields, animated and focus
     */
    fun clear(
        focusedIndex: Int = 0,
        animate: Boolean = true
    ) {
        val animList = mutableListOf<Animator>()
        for (i in 0 until OTPDigitsCount) {
            viewAtIndex(i)?.let { otpField ->
                // add animation to chain
                if (animate) {
                    animList.add(otpField.errorVibrate(start = false))
                }
                // clear text
                otpField.setText("")
                // focus
                if (focusedIndex == i) {
                    otpField.requestFocus()
                }
            }
        }
        /* animate all */
        if (animate) {
            AnimatorSet().apply {
                playTogether(animList)
                start()
            }
        }
    }

    /**
     * OTP View ID value pair
     */
    internal data class OTPViewIDValuePair(
        var viewId: Int,
        var digit: Char? = null
    )
}

/**
 * Delhivery OTP View Interface
 */
interface DelhiveryOTPViewInterfaceSix {

    /**
     * OTP Submitted,
     * @param otp as [CharArray]
     */
    fun otpSubmitted(otp: CharArray)
}

/* OTP Digit count */
private const val OTPDigitsCount = 6