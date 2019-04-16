package com.delhivery.orion.utils

import android.content.Context
import android.support.annotation.StringRes
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.delhivery.orion.R
import com.delhivery.orion.databinding.LayoutDelhiveryProgressBinding
import com.delhivery.orion.databinding.LayoutFabCardMenuBinding
import com.delhivery.orion.databinding.LayoutProgressBinding
import com.delhivery.orion.injection.scope.ActivityScope
import com.delhivery.orion.ui.custom.DelhiveryFabCardMenuInterface
import com.delhivery.orion.ui.custom.DelhiveryFabCardMenuItem
import com.delhivery.orion.utils.extensions.consumeTouch
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

@ActivityScope
class UiUtils @Inject constructor(private val activity: DaggerAppCompatActivity) {

  // Activity root view
  private val activityRoot: ViewGroup by lazy {
    activity.findViewById<ViewGroup>(android.R.id.content)
  }

  //Progress View binding
  private val progressBinding: LayoutProgressBinding by lazy {
    val binding = LayoutProgressBinding.inflate(LayoutInflater.from(activity), activityRoot, false)
    binding.root.consumeTouch()
    binding
  }

  /* delhivery progress binding */
  private val delhiveryProgressBinding: LayoutDelhiveryProgressBinding by lazy {
    val binding =
      LayoutDelhiveryProgressBinding.inflate(LayoutInflater.from(activity), activityRoot, false)
    binding.root.consumeTouch()
    binding
  }

  // Snackbar instance
  private var snackbar: Snackbar? = null

  /**
   * Show Simple Toast with [Toast.LENGTH_SHORT] duration
   *
   * @param msg Toast message
   */
  fun showToast(msg: String) {
    Toast.makeText(activity, msg, Toast.LENGTH_SHORT)
        .show()
  }

  /**
   * Replica of [showToast] with [StringRes]
   *
   * @param resId String resource Id
   */
  fun showToast(@StringRes resId: Int) {
    activity.getString(resId)
        .let {
          showToast(it)
        }
  }

  /**
   * Hide in-line progress
   * Remove [LayoutProgressBinding] Root view from [activityRoot]
   */
  fun hideProgress() {
    activity.runOnUiThread {
      activityRoot.removeView(progressBinding.root)
    }
  }

  /**
   * Show in-line progress
   * Check and remove if already showing and then add and set progress message
   *
   * @param progressMsg Message showing under progress bar
   */
  fun showProgress(progressMsg: String? = null) {
    activity.runOnUiThread {
      toggleKeyboard()
      //remove progress layout if exist
      progressBinding.root.parent?.let {
        (it as ViewGroup).removeView(progressBinding.root)
      }

      activityRoot.addView(progressBinding.root)
      progressBinding.text = progressMsg
    }
  }

  /**
   * Show delhivery progress
   */
  fun showDelhiveryProgress(
    title: String,
    message: String,
    proTip: String
  ) {
    activity.runOnUiThread {
      toggleKeyboard()
      //remove progress layout if exist
      delhiveryProgressBinding.root.parent?.let {
        (it as ViewGroup).removeView(progressBinding.root)
      }

      activityRoot.addView(delhiveryProgressBinding.root)
      delhiveryProgressBinding.title = title
      delhiveryProgressBinding.message = message
      delhiveryProgressBinding.tip = proTip

      delhiveryProgressBinding.progress.startAnim()
    }
  }

  /**
   * Hide delhivery progress
   */
  fun hideDelhiveryProgress() {
    activity.runOnUiThread {
      delhiveryProgressBinding.progress.stopAnim()
      activityRoot.removeView(delhiveryProgressBinding.root)
    }
  }

  /**
   * Show Snackbar with string message
   *
   * @param msg String message
   * @param duration Duration of snackbar, by default is [Snackbar.LENGTH_SHORT]
   */
  fun showSnackbar(
    msg: String,
    duration: Int = Snackbar.LENGTH_SHORT
  ) {
//        if (snackbar != null) {
//            if (!snackbar!!.isShownOrQueued) {
//                snackbar!!.show()
//            }
//        } else {
    snackbar = Snackbar.make(activityRoot, msg, duration)
    snackbar!!.show()
//        }
  }

  /**
   * Show Snackbar with [StringRes] id
   *
   * @param resId [StringRes] id
   * @param duration Duration of snackbar, by default is [Snackbar.LENGTH_SHORT]
   */
  fun showSnackbar(@StringRes resId: Int, duration: Int = Snackbar.LENGTH_SHORT) {
    showSnackbar(activity.getString(resId), duration)
  }

  /**
   * Show no internet Indefinite snackbar
   */
  fun showNoInternetSnackbar() {
    showSnackbar(R.string.error_no_internet, Snackbar.LENGTH_INDEFINITE)
  }

  /**
   * Dismiss snackbar and assign null
   */
  fun dismissSnackbar() {
    snackbar?.let {
      it.dismiss()
      snackbar = null
    }
  }

  /**
   * Toggle soft keyboard visibility
   *
   * @param hide [Boolean] flag to hide/show soft keyboard, default is set to hide keyboard
   */
  fun toggleKeyboard(hide: Boolean = true) {
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    (activity.findViewById(android.R.id.content) as View?)?.windowToken?.let {
      when (hide) {
        true -> imm.hideSoftInputFromWindow(it, 0)
        false -> imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
      }
    }
  }

  /**
   * Show fab card menu
   */
  fun fabCardMenu(
    fab: FloatingActionButton,
    items: List<DelhiveryFabCardMenuItem>,
    itemSelected: (DelhiveryFabCardMenuItem) -> Unit
  ) {
    val parent = (fab.parent as ViewGroup)
    val cardMenuBinding =
      LayoutFabCardMenuBinding.inflate(LayoutInflater.from(activity), parent, false)

    parent.addView(cardMenuBinding.root)

    /* setup overlay */
    cardMenuBinding.viewOverlay.setOnClickListener {
      cardMenuBinding.viewOverlay.animate()
          .alpha(0f)
      cardMenuBinding.fabMenu.animateClose {
        parent.removeView(cardMenuBinding.root)
      }
    }

    /* set fab menu */
    cardMenuBinding.fabMenu.apply {
      this.anchorView = fab
      setMenuItems(items)
      /* menu interface - close + pass action */
      menuInterface = object : DelhiveryFabCardMenuInterface {
        override fun onItemSelected(item: DelhiveryFabCardMenuItem) {
          cardMenuBinding.fabMenu.animateClose {
            parent.removeView(cardMenuBinding.root)
            itemSelected(item)
          }
        }
      }
      post {
        cardMenuBinding.viewOverlay.animate()
            .alpha(1f)
        cardMenuBinding.fabMenu.animateOpen()
      }
    }
  }
}