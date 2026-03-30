package com.delhivery.axle.ui.customviews

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.delhivery.axle.R
import com.delhivery.axle.api.response.FieldType
import com.delhivery.axle.api.response.FormField
import com.delhivery.axle.databinding.ViewDynamicTextInputBinding

/**
 * Custom view for rendering TEXT, NUMBER, and TEXTAREA fields dynamically
 */
class DynamicTextInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewDynamicTextInputBinding
    private var valueChangedListener: ((String) -> Unit)? = null
    private var currentField: FormField? = null

    init {
        binding = ViewDynamicTextInputBinding.inflate(LayoutInflater.from(context), this, true)
        setupTextWatcher()
        setupFocusListener()
    }

    /**
     * Setup focus listener to show cursor when focused
     */
    private fun setupFocusListener() {
        binding.etInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.etInput.isCursorVisible = true
            }
        }
    }

    /**
     * Configure the view based on FormField configuration
     */
    fun setFieldConfig(field: FormField) {
        currentField = field

        binding.tvLabel.text = field.displayLabel

        // Set placeholder
        field.placeholder?.let {
            binding.etInput.hint = it
        }

        // Set help text with info icon
        field.helpText?.let {
            binding.tvHelpText.text = it
            binding.llHelpText.visibility = VISIBLE
        } ?: run {
            binding.llHelpText.visibility = GONE
        }

        // Configure input type based on field type
        when (field.fieldTypeEnum) {
            FieldType.NUMBER -> {
                binding.etInput.inputType = InputType.TYPE_CLASS_NUMBER or 
                    InputType.TYPE_NUMBER_FLAG_DECIMAL or 
                    InputType.TYPE_NUMBER_FLAG_SIGNED
            }
            FieldType.TEXTAREA -> {
                binding.etInput.inputType = InputType.TYPE_CLASS_TEXT or 
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or 
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                // Fixed height for TEXTAREA - use fixed lines with scrolling
                binding.etInput.setLines(5)
                binding.etInput.isVerticalScrollBarEnabled = true
                binding.etInput.setHorizontallyScrolling(false)

                // Allow parent ScrollView to scroll when TEXTAREA content doesn't need scrolling
                binding.etInput.setOnTouchListener { v, event ->
                    if (v.canScrollVertically(1) || v.canScrollVertically(-1)) {
                        v.parent.requestDisallowInterceptTouchEvent(true)
                        if (event.action == MotionEvent.ACTION_UP) {
                            v.parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    false
                }
            }
            FieldType.TEXT -> {
                binding.etInput.inputType = InputType.TYPE_CLASS_TEXT or 
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                binding.etInput.setSingleLine(true)
            }
            else -> {
                binding.etInput.inputType = InputType.TYPE_CLASS_TEXT
            }
        }

        // Set max length filter
        field.maxLength?.let { maxLen ->
            binding.etInput.filters = arrayOf(InputFilter.LengthFilter(maxLen))
        }
    }

    /**
     * Get current input value
     */
    fun getValue(): String {
        return binding.etInput.text?.toString() ?: ""
    }

    /**
     * Set input value
     */
    fun setValue(value: String) {
        binding.etInput.setText(value)
    }

    /**
     * Set error message
     */
    fun setError(message: String?) {
        binding.tilInput.error = message
        binding.tilInput.isErrorEnabled = message != null
    }

    /**
     * Clear error message
     */
    fun clearError() {
        binding.tilInput.error = null
        binding.tilInput.isErrorEnabled = false
    }

    /**
     * Set listener for value changes
     */
    fun setOnValueChangedListener(listener: (String) -> Unit) {
        valueChangedListener = listener
    }

    /**
     * Setup text watcher for real-time updates
     */
    private fun setupTextWatcher() {
        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                
                // Update character counter for TEXTAREA
                currentField?.let { field ->
                    if (field.fieldTypeEnum == FieldType.TEXTAREA) {
                        field.maxLength?.let { maxLen ->
                            updateCharCounter(text.length, maxLen)
                        }
                    }
                }
                
                // Notify listener
                valueChangedListener?.invoke(text)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     * Update character counter display
     */
    private fun updateCharCounter(current: Int, max: Int) {
        binding.tvCharCounter.text = "$current / $max"
        
        // Change color if approaching limit
        val color = if (current > max * 0.9) {
            ContextCompat.getColor(context, R.color.colorAccent)
        } else {
            ContextCompat.getColor(context, R.color.font_labels)
        }
        binding.tvCharCounter.setTextColor(color)
    }
}
