package com.dfd.delfin.ui.custom
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.dfd.delfin.R

class DelfinAadhaarFormattedEditText(context: Context,
                                     attrs: AttributeSet? = null) : AppCompatEditText(context, attrs) {
    var groupSeparator = ' '
    var numberOfGroups = 3
    var groupLength = 4

    var inputLength = numberOfGroups * (groupLength + 1) - 1

    private val digitsKeyListener = DigitsKeyListener.getInstance("0123456789")

    private var separatorAndDigitsKeyListener: DigitsKeyListener

    private var initCompleted = false
    init {
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.DelfinAadhaarMaskedEditText, 0, 0)
            val separatorStr = a.getString(R.styleable.DelfinAadhaarMaskedEditText_groupSeparator)
            if (!separatorStr.isNullOrEmpty()) {
                groupSeparator = separatorStr[0]
            }
            numberOfGroups = a.getInteger(R.styleable.DelfinAadhaarMaskedEditText_numberOfGroups, numberOfGroups)
            groupLength = a.getInteger(R.styleable.DelfinAadhaarMaskedEditText_groupLength, groupLength)
        }

        inputLength = numberOfGroups * (groupLength + 1) - 1
        separatorAndDigitsKeyListener = DigitsKeyListener.getInstance("0123456789$groupSeparator")

        setSelection(text!!.length)
        inputType = InputType.TYPE_CLASS_NUMBER
        keyListener = digitsKeyListener
        initCompleted = true
    }

    /**
     * Add Length action as Text Watcher
     */
    fun lengthAction(
        length: Int,
        action: () -> Unit
    ) {
        addTextChangedListener(LengthTextWatcher(length, action))
    }
    /**
     * Length text watcher action on length
     */
    inner class LengthTextWatcher(
        private val length: Int,
        private val action: () -> Unit
    ) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            if (s == null) {
                return
            }
            if (s.length== length) {
                action()
            }
            if (s.length > inputLength) {
                while (s.length > inputLength) {
                    s.delete(s.length - 1, s.length)
                }
            } else if (s.isNotEmpty() && s.length % (groupLength + 1) == 0) {
                if (s.last() == groupSeparator) {
                    s.delete(s.length - 1, s.length)
                } else if (s.last().isDigit() && s.length < inputLength) {
                    keyListener = separatorAndDigitsKeyListener
                    s.insert(s.length - 1, groupSeparator.toString())
                    keyListener = digitsKeyListener
                }
            }

        }

    }
    override fun onSelectionChanged(start: Int, end: Int) {
        if (!initCompleted) {
            return
        }
        // cursor is always at the end of the string
        if (start != text!!.length || end != text!!.length) {
            setSelection(text!!.length)
        } else {
            super.onSelectionChanged(start, end)
        }
    }


}