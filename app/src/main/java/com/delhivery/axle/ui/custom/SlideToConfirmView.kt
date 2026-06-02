package com.delhivery.axle.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.delhivery.axle.R

class SlideToConfirmView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val thumb: ImageView
    private val label: TextView
    private val track: View

    private var initialX = 0f
    private var onSlideComplete: (() -> Unit)? = null
    private var isLocked = false

    init {
        LayoutInflater.from(context).inflate(R.layout.view_slide_to_confirm, this, true)
        thumb = findViewById(R.id.slideThumb)
        label = findViewById(R.id.slideLabel)
        track = findViewById(R.id.slideTrack)

        thumb.setOnTouchListener { view, event ->
            if (isLocked) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX - view.x
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val newX = (event.rawX - initialX).coerceIn(0f, getMaxSlide())
                    view.x = newX
                    // Fade label as thumb moves
                    val progress = newX / getMaxSlide()
                    label.alpha = 1f - progress
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val currentX = view.x
                    val threshold = getMaxSlide() * 0.85f

                    if (currentX >= threshold) {
                        // Slide complete
                        animateThumbTo(getMaxSlide())
                        isLocked = true
                        onSlideComplete?.invoke()
                    } else {
                        // Reset
                        animateThumbTo(0f)
                        label.alpha = 1f
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun getMaxSlide(): Float {
        return (track.width - thumb.width).toFloat().coerceAtLeast(0f)
    }

    private fun animateThumbTo(targetX: Float) {
        val animator = ValueAnimator.ofFloat(thumb.x, targetX)
        animator.duration = 200
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            thumb.x = animation.animatedValue as Float
            val progress = thumb.x / getMaxSlide().coerceAtLeast(1f)
            label.alpha = 1f - progress
        }
        animator.start()
    }

    fun setOnSlideCompleteListener(listener: () -> Unit) {
        onSlideComplete = listener
    }

    fun setLabelText(text: String) {
        label.text = text
    }

    fun reset() {
        isLocked = false
        animateThumbTo(0f)
        label.alpha = 1f
    }
}
