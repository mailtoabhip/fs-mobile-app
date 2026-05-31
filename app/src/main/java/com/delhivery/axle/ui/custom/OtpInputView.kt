package com.delhivery.axle.ui.custom

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ViewOtpInputBinding
import com.delhivery.axle.databinding.ViewOtpInputCellBinding

/**
 * Full-width OTP input view with configurable equal cells.
 *
 * Normal state : grey border  (@drawable/bg_login_edit)
 * Error state  : red border   (@drawable/bg_otp_cell_error)
 *
 * Usage in XML:
 *   <com.delhivery.axle.ui.custom.OtpInputView
 *       android:id="@+id/otp_input_view"
 *       android:layout_width="match_parent"
 *       android:layout_height="wrap_content" />
 *
 * Usage in code:
 *   binding.otpInputView.onOtpComplete = { otp -> viewModel.verifyOTP(otp) }
 *   binding.otpInputView.showError(true)   // red border + shake
 *   binding.otpInputView.clear()
 */
class OtpInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    /** Called when all [digitCount] digits have been entered. */
    var onOtpComplete: ((CharArray) -> Unit)? = null

    private val binding = ViewOtpInputBinding.inflate(LayoutInflater.from(context), this, true)
    private val cells = mutableListOf<DelhiveryOTPViewEditText>()

    // Tracks whether we're in error state so focus changes don't prematurely clear it
    private var isInErrorState = false

    /** Number of OTP digits (default 4, configurable via XML). */
    private var digitCount: Int = DEFAULT_DIGIT_COUNT

    init {
        context.obtainStyledAttributes(attrs, R.styleable.OtpInputView, 0, 0).use {
            digitCount = it.getInteger(R.styleable.OtpInputView_otpDigitCount, DEFAULT_DIGIT_COUNT)
        }
        buildCells()
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Switch all cells to error (red) or normal (grey) border. */
    fun showError(error: Boolean) {
        isInErrorState = error
        cells.forEach { it.error = error }
        if (error) shakeCells()
    }

    /** Clear all cells and optionally focus the first one. */
    fun clear(focusFirst: Boolean = true) {
        isInErrorState = false
        cells.forEach { it.setText("") }
        showError(false)
        if (focusFirst) cells.firstOrNull()?.requestFocus()
    }

    /** Returns the current OTP as a string (empty chars for unfilled cells). */
    fun currentOtp(): String = cells.joinToString("") { it.text.toString() }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private fun buildCells() {
        binding.otpCellContainer.removeAllViews()
        cells.clear()
        repeat(digitCount) { index ->
            val cellBinding = ViewOtpInputCellBinding.inflate(
                LayoutInflater.from(context), binding.otpCellContainer, false
            )
            val edit = cellBinding.otpCellEdit
            edit.id = View.generateViewId()
            edit.addTextChangedListener(CellTextWatcher(index))
            edit.setOnKeyListener(CellKeyListener(index))
            edit.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) edit.setText("")
            }
            binding.otpCellContainer.addView(cellBinding.root)
            cells.add(edit)
        }
    }

    private fun focusCell(index: Int, clearText: Boolean = true) {
        cells.getOrNull(index)?.let { cell ->
            if (clearText) cell.setText("")
            cell.requestFocus()
        }
    }

    private fun submitIfComplete() {
        val digits = cells.map { it.text.toString().firstOrNull() }
        if (digits.none { it == null }) {
            onOtpComplete?.invoke(digits.map { it!! }.toCharArray())
        } else {
            // focus first empty cell
            focusCell(digits.indexOfFirst { it == null }, clearText = false)
        }
    }

    private fun shakeCells() {
        // Shake without using errorVibrate() — that extension resets error=false
        // in onAnimationEnd, which would clear the red border after ~300ms.
        val distance = resources.getDimension(com.delhivery.axle.R.dimen.distance_otp_vibrate)
        val animators = cells.map { cell ->
            ObjectAnimator.ofFloat(
                cell, "translationX",
                0f, distance, -distance, distance, -distance, 0f
            )
        }
        AnimatorSet().apply {
            playTogether(animators)
            start()
        }
    }

    // -------------------------------------------------------------------------
    // Inner classes
    // -------------------------------------------------------------------------

    private inner class CellTextWatcher(private val index: Int) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            // Any interaction clears the error state across all cells
            if (isInErrorState) {
                isInErrorState = false
                cells.forEach { it.error = false }
            }
            if (s?.isNotEmpty() == true) {
                if (index < digitCount - 1) {
                    focusCell(index + 1, clearText = false)
                } else {
                    submitIfComplete()
                }
            }
        }
    }

    private inner class CellKeyListener(private val index: Int) : OnKeyListener {
        override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if (keyCode == KeyEvent.KEYCODE_DEL && event?.action == KeyEvent.ACTION_DOWN) {
                if (cells.getOrNull(index)?.text?.isEmpty() == true && index > 0) {
                    focusCell(index - 1, clearText = true)
                }
            }
            return false
        }
    }

    companion object {
        private const val DEFAULT_DIGIT_COUNT = 4
    }
}
