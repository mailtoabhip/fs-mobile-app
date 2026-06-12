package com.dfd.delfin.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.dfd.delfin.R



class DelfinTrucksAutoEditText(
        context: Context,
        attrs: AttributeSet? = null
) : AppCompatAutoCompleteTextView(context, attrs) {

    private val RightGap = context.resources.getDimension(R.dimen.size_12dp)
    private val DotsGap = context.resources.getDimension(R.dimen.size_6dp)
    private val DotRadius = context.resources.getDimension(R.dimen.size_2dp)
    private val MaxTransY = context.resources.getDimension(R.dimen.size_4dp)

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
            alpha = 100
        }
    }

    private val errorPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            alpha = 100
        }
    }

    /* animation factor */
    private var factor = 0.0f

    private var progress = false
    private var error = false
    private var isSelectionInProgress = false
    private var textBeforeSelection: String = ""

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (progress) {
            canvas.apply {
                val pos = factor.toInt()
                val transFactor = factor % 1
                for (i in 0 until DotsCount) {
                    val cx = width - RightGap - DotRadius - i * DotsGap
                    val cy = if (i == pos) {
                        height / 2f - 2f * MaxTransY * if (transFactor > 0.5f) 1f - transFactor else transFactor
                    } else {
                        height / 2f
                    }
                    drawCircle(cx, cy, DotRadius, paint)
                }
            }
        } else if (error && hasFocus()) {
            canvas?.apply {
                drawCircle(width - RightGap - DotRadius, height / 2f, DotRadius, errorPaint)
            }
        }
    }

    fun progress(start: Boolean = true) {
        if (start == progress) return
        progress = start
        if (progress) {
            _anim.start()
        } else {
            _anim.cancel()
            invalidate()
        }
    }

    private val _anim by lazy {
        ValueAnimator.ofFloat(0.0f, DotsCount.toFloat())
                .apply {
                    duration = 1000
                    repeatMode = ValueAnimator.RESTART
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener {
                        factor = (it.animatedValue as Float)
                        invalidate()
                    }
                    start()
                }
    }

    override fun showDropDown() {
        // Always save the current text before dropdown is shown
        textBeforeSelection = text.toString()
        super.showDropDown()
    }
    
    fun setItems(
            trucks: List<String>,
            selected: (String) -> Unit
    ) {
        progress(false)
        val adapter = CustomAdapter(context, R.layout.view_truck_item, trucks)
        setAdapter(adapter)
        
        setOnItemClickListener { _, _, i, _ ->
            isSelectionInProgress = true
            //check the selected value, if equals "Add New Truck" don't set in edit text view
            Log.d("Add_truck_value==>>", trucks[i])
            
            // Only set text if NOT "Add New Truck" to preserve existing input
            if(trucks[i] != "Add New Truck") {
                // Text is already set by AutoCompleteTextView, no need to set again
                // Clear saved text for next dropdown
                textBeforeSelection = ""
            } else {
                // Restore the text that was there before selection
                setText(textBeforeSelection)
                textBeforeSelection = ""
            }
            //
            selected(trucks[i])
            dismissDropDown()
            // Reset flag after a short delay
            postDelayed({
                isSelectionInProgress = false
            }, 100)
        }
        
        // Reset flag if dropdown is dismissed without selection
        setOnDismissListener {
            if (!isSelectionInProgress) {
                textBeforeSelection = ""
            }
        }
        
        if (trucks.isEmpty()) {
            error = true
            dismissDropDown()
        } else {
            error = false
        }
        invalidate()
    }

    fun errorAnimate() {
        val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
        this.startAnimation(shake)
    }
    
    // Check if selection is in progress
    fun isSelectionInProgress(): Boolean {
        return isSelectionInProgress
    }

    class CustomAdapter(context: Context?, resource: Int, items: List<String?>?) : ArrayAdapter<String?>(context!!, resource, items!!) {
        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            var convertView = view
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.view_truck_item, parent, false)
            }
            val tvVehicleNumber = convertView?.findViewById<TextView>(R.id.tvVehicleNumber)

            tvVehicleNumber?.text = getItem(position)
            if(tvVehicleNumber?.text=="Add New Truck"){
                tvVehicleNumber.setTextColor(ContextCompat.getColor(context,R.color.text_blue_v1))
                tvVehicleNumber.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.ic_plus_blue),null,null,null)
                tvVehicleNumber.background =ContextCompat.getDrawable(context,R.drawable.bg_all_round_corner_white)
            }else{
                tvVehicleNumber?.elevation = 8.0f
                tvVehicleNumber?.setTextColor(ContextCompat.getColor(context,R.color.heading_black))
                tvVehicleNumber?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.ic_search_truck),null,null,null)
            }
            return convertView!!
        }
    }
}

private const val DotsCount = 3