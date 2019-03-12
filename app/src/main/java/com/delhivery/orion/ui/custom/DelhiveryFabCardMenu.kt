package com.delhivery.orion.ui.custom

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.LinearLayoutCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.databinding.LayoutFabCardMenuItemBinding

class DelhiveryFabCardMenu(
  context: Context,
  attrs: AttributeSet? = null
) : LinearLayoutCompat(context, attrs) {

  init {
    orientation = VERTICAL
  }

  /* anchor view */
  var anchorView: FloatingActionButton? = null
  /* fab card menu interface */
  var menuInterface: DelhiveryFabCardMenuInterface? = null

  /* menu items list */
  private var menuItems: List<DelhiveryFabCardMenuItem> = listOf()

  /* draw params */
  private var factor = 0.0f

  /* Final card radius */
  private val CardRadius by lazy { resources.getDimension(R.dimen.size_8dp) }

  /* card paint */
  private val cardPaint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      style = Paint.Style.FILL
      setShadowLayer(30f, 0f, 0f, Color.BLACK)
    }
  }

  private val cardBg by lazy { ResourcesCompat.getDrawable(resources, R.drawable.bg_card, null) }

  /**
   * Add Menu items
   */
  fun setMenuItems(_menuItems: List<DelhiveryFabCardMenuItem>) {
    this.menuItems = _menuItems
    menuItems.mapIndexed { index, item ->
      val itemBinding = LayoutFabCardMenuItemBinding.inflate(
          LayoutInflater.from(context), this@DelhiveryFabCardMenu, false
      )

      /* set binding params */
      itemBinding.isTitle = item.id == FabMenuCardMenuTitleItem
      itemBinding.text = item.text
      if (item.id != FabMenuCardMenuTitleItem) {
        itemBinding.root.setOnClickListener {
          it.postDelayed({ menuInterface?.onItemSelected(item) }, 300)
        }
      }
      addView(itemBinding.root, index)
    }
    invalidate()
  }

  override fun dispatchDraw(canvas: Canvas?) {
    if (canvas == null || anchorView == null) {
      return
    }

    canvas.apply {
      val fabRad = Math.max(anchorView!!.height, anchorView!!.width) / 2f
      val cy = height - fabRad
      val cx = width - fabRad

      val left = cx + ((0 - cx) * factor)
      val right = cx + (fabRad * factor)
      val top = cy + ((0 - cy) * factor)
      val bottom = cy + (fabRad * factor)

      val radius = CardRadius + ((fabRad - CardRadius) * (1f - factor))
      drawRoundRect(RectF(left, top, right, bottom), radius, radius, cardPaint)
    }
    super.dispatchDraw(canvas)
  }

  /**
   * animate open card menu
   */
  fun animateOpen() {
    anchorView?.hide()
    viewsVisiblity(View.INVISIBLE)
    animate(0f, 1f) {
      viewsVisiblity(View.VISIBLE)
    }
  }

  /**
   * Animate close card menu
   */
  fun animateClose(endAction: () -> Unit) {
    viewsVisiblity(View.INVISIBLE)
    animate(1f, 0f) {
      endAction()
      anchorView?.show()
    }
  }

  /**
   * Change all views visiblity
   */
  private fun viewsVisiblity(visibility: Int) {
    background = if (visibility == View.VISIBLE) {
      cardBg
    } else {
      null
    }
    for (i in 0 until childCount) {
      getChildAt(i).visibility = visibility
    }
  }

  /**
   * Animate as per values
   */
  private fun animate(
    vararg values: Float,
    endAction: () -> Unit
  ) {
    ValueAnimator.ofFloat(*values)
        .apply {
          duration = 500
          interpolator = FastOutSlowInInterpolator()
          addListener(object : AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
              endAction()
            }

            override fun onAnimationCancel(animation: Animator?) {
              endAction()
            }

            override fun onAnimationStart(animation: Animator?) {
            }
          })
          addUpdateListener {
            factor = (it.animatedValue as Float)
            invalidate()
          }
          start()
        }
  }
}

/* Title item id */
const val FabMenuCardMenuTitleItem = -1

/**
 * Fab card menu item
 */
data class DelhiveryFabCardMenuItem(
  val id: Int,
  val text: String
)

/**
 * Card menu interface
 */
interface DelhiveryFabCardMenuInterface {
  fun onItemSelected(item: DelhiveryFabCardMenuItem)
}