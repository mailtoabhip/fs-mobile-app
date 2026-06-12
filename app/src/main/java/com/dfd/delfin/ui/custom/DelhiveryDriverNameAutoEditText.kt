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
import androidx.core.content.res.ResourcesCompat
import com.dfd.delfin.R
import com.dfd.delfin.api.response.DriverDataResponse


class DelhiveryDriverNameAutoEditText(
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
                //drawCircle(width - RightGap - DotRadius, height / 2f, DotRadius, errorPaint)
            }
        }
    }

    fun progress(start: Boolean = true) {
        if (start == progress) return
        progress = start
        if (progress) {
            Log.d("Anime::progress", "start")
            _anim.start()
        } else {
            Log.d("Anime::progress", "cancel")
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

    fun setItems(
        driverName: List<DriverDataResponse>,
        selected: (String) -> Unit
    ) {
        // Always set up the adapter and listener, regardless of isPerformingCompletion
        progress(false)
        val adapter = CustomAdapter(context, R.layout.view_driver_name_item, driverName)
        setAdapter(adapter)
        setOnItemClickListener { _, _, i, _ ->
            setText(driverName[i].driverName)
            driverName[i].driverName?.let { selected(it) }
            dismissDropDown()
        }
        
        if (driverName.isEmpty()) {
            error = true
            dismissDropDown()
        } else {
            error = false
            // Only show dropdown if not currently performing completion
            if (!isPerformingCompletion) {
                showDropDown()
            } else {
                // Delay showing dropdown to avoid conflicts
                post {
                    if (!isPerformingCompletion) {
                        showDropDown()
                    }
                }
            }
        }
        invalidate()
    }

    // Simple method that returns full DriverDataResponse
    fun setItemsWithData(
        driverData: List<DriverDataResponse>,
        selected: (DriverDataResponse) -> Unit
    ) {
        progress(false)
        val adapter = CustomAdapter(context, R.layout.view_driver_name_item, driverData)
        setAdapter(adapter)
        setOnItemClickListener { _, _, i, _ ->
            isSelectionInProgress = true
            val selectedDriverName = driverData[i].driverName ?: ""
            setText(selectedDriverName)
            dismissDropDown()
            selected(driverData[i])
            // Reset flag after a short delay
            postDelayed({
                isSelectionInProgress = false
            }, 100)
        }
        
        if (driverData.isEmpty()) {
            error = true
            dismissDropDown()
        } else {
            error = false
            // Don't automatically show dropdown to avoid focus issues
            // showDropDown()
        }
        invalidate()
    }
    
    // Manual method to show dropdown when needed
    fun showDropdownIfNeeded() {
        if (adapter != null && adapter!!.count > 0) {
            showDropDown()
        }
    }
    fun hideDropdownIfNeeded() {
        isSelectionInProgress = true
    }
    // Check if selection is in progress
    fun isSelectionInProgress(): Boolean {
        return isSelectionInProgress
    }


    fun errorAnimate() {
        val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
        this.startAnimation(shake)
    }

    class CustomAdapter(context: Context?, resource: Int, items: List<DriverDataResponse?>?) : ArrayAdapter<DriverDataResponse?>(context!!, resource, items!!) {
        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            var convertView = view
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.view_driver_name_item, parent, false)
            }
            val tvDriverName = convertView?.findViewById<TextView>(R.id.tvDriverName)
            val tvDriverNumber = convertView?.findViewById<TextView>(R.id.tvDriverNumber)

            tvDriverName?.text = getItem(position)?.driverName
            tvDriverNumber?.text = getItem(position)?.driverPhone


            return convertView!!
        }
    }
}

private const val DotsCount = 3