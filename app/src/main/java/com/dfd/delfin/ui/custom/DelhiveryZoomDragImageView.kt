package com.dfd.delfin.ui.custom

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView

/**
 * Custom implementation of [AppCompatImageView] with zoom/drag/rotate features
 */
class DelhiveryZoomDragImageView(
  internal var context: Context,
  attr: AttributeSet
) : AppCompatImageView(context, attr) {

  var lastEvent: FloatArray? = null

  var d = 0f
  var newRot = 0f

  private var isZoomAndRotate: Boolean = false
  private var isOutSide: Boolean = false

  private val NONE = 0
  private val DRAG = 1
  private val ZOOM = 2
  private var mode = NONE

  private var start = PointF()
  private var mid = PointF()
  var oldDist = 1f

  private var xCoOrdinate: Float = 0.toFloat()
  private var yCoOrdinate: Float = 0.toFloat()

  init {
    super.setClickable(true)

    setOnTouchListener { v, event ->
      val view = v as AppCompatImageView
      view.bringToFront()
      viewTransformation(view, event)
      true
    }
  }

  private fun viewTransformation(
    view: View,
    event: MotionEvent
  ) {
    when (event.action and MotionEvent.ACTION_MASK) {
      MotionEvent.ACTION_DOWN -> {
        xCoOrdinate = view.x - event.rawX
        yCoOrdinate = view.y - event.rawY

        start.set(event.x, event.y)
        isOutSide = false
        mode = DRAG
        lastEvent = null
      }
      MotionEvent.ACTION_POINTER_DOWN -> {
        oldDist = spacing(event)
        if (oldDist > 10f) {
          midPoint(mid, event)
          mode = ZOOM
        }

        lastEvent = FloatArray(4)
        lastEvent!![0] = event.getX(0)
        lastEvent!![1] = event.getX(1)
        lastEvent!![2] = event.getY(0)
        lastEvent!![3] = event.getY(1)
        d = rotation(event)
      }
      MotionEvent.ACTION_UP -> {
        isZoomAndRotate = false
        if (mode == DRAG) {
          val x = event.x
          val y = event.y
        }
        isOutSide = true
        mode = NONE
        lastEvent = null
        mode = NONE
        lastEvent = null
      }
      MotionEvent.ACTION_OUTSIDE -> {
        isOutSide = true
        mode = NONE
        lastEvent = null
        mode = NONE
        lastEvent = null
      }
      MotionEvent.ACTION_POINTER_UP -> {
        mode = NONE
        lastEvent = null
      }
      MotionEvent.ACTION_MOVE -> if (!isOutSide) {
        if (mode == DRAG) {
          isZoomAndRotate = false
          view.animate()
              .x(event.rawX + xCoOrdinate)
              .y(event.rawY + yCoOrdinate)
              .setDuration(0)
              .start()
        }
        if (mode == ZOOM && event.pointerCount == 2) {
          val newDist1 = spacing(event)
          if (newDist1 > 10f) {
            val scale = newDist1 / oldDist * view.scaleX
            view.scaleX = scale
            view.scaleY = scale
          }
          if (lastEvent != null) {
            newRot = rotation(event)
            view.rotation = (view.rotation + (newRot - d))
          }
        }
      }
    }
  }

  private fun rotation(event: MotionEvent): Float {
    val delta_x = (event.getX(0) - event.getX(1)).toDouble()
    val delta_y = (event.getY(0) - event.getY(1)).toDouble()
    val radians = Math.atan2(delta_y, delta_x)
    return Math.toDegrees(radians)
        .toFloat()
  }

  private fun spacing(event: MotionEvent): Float {
    val x = event.getX(0) - event.getX(1)
    val y = event.getY(0) - event.getY(1)
    return Math.sqrt((x * x + y * y).toDouble())
        .toInt()
        .toFloat()
  }

  private fun midPoint(
    point: PointF,
    event: MotionEvent
  ) {
    val x = event.getX(0) + event.getX(1)
    val y = event.getY(0) + event.getY(1)
    point.set(x / 2, y / 2)
  }
}