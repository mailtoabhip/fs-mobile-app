package com.delhivery.orion.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.delhivery.orion.databinding.DialogErrorBinding
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.safeDispose
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Show delhivery style error dialog
 */
class ErrorDialog(
  context: Context,
  private val errorMessage: String,
  private val dismissTimeout: Long
) : AlertDialog(context) {

  /* dialog binding */
  private lateinit var binding: DialogErrorBinding

  /* dismiss timeout disposable */
  private var timeoutDisposable: Disposable? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    /* dialog binding */
    binding = DialogErrorBinding.inflate(layoutInflater)
    setContentView(binding.root)

    /* Dismiss dialog timer */
    if (dismissTimeout > 0) {
      timeoutDisposable = Observable.interval(0L, 1L, TimeUnit.SECONDS)
          .onBackground()
          .subscribe({
            val timeLeft = dismissTimeout - it
            if (timeLeft >= 0) {
              binding.btnDismiss.text = "Dismiss ($timeLeft)"
            } else {
              dismissDialog()
            }
          }, {
            dismissDialog()
          })
    }

    /* bind data to layout */
    binding.apply {
      error = errorMessage
      btnClose.setOnClickListener { dismissDialog() }
      btnDismiss.setOnClickListener { dismissDialog() }
    }

    /* dispose on dialog dismiss */
    setOnDismissListener { timeoutDisposable.safeDispose() }
  }

  /**
   * Dismiss dialog
   */
  private fun dismissDialog() {
    timeoutDisposable.safeDispose()
    if (isShowing) {
      dismiss()
    }
  }
}