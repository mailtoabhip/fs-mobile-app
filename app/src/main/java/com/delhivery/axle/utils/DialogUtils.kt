package com.delhivery.axle.utils

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.appcompat.app.AlertDialog
import android.text.Html
import com.delhivery.axle.R
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.ui.dialogs.ErrorDialog
import dagger.android.support.DaggerAppCompatActivity
import java.util.Calendar
import javax.inject.Inject

@ActivityScope
class DialogUtils @Inject constructor(private val activity: DaggerAppCompatActivity) {

  /**
   * Show Simple date picker and pass callback via [DatePickerDialog.OnDateSetListener]
   *
   * @param listener On Date Set listener [DatePickerDialog.OnDateSetListener]
   * @param calendar Default Selected calendar(date), by default set to today
   */
  fun datePicker(
    listener: DatePickerDialog.OnDateSetListener,
    calendar: Calendar = Calendar.getInstance()
  ) {
    DatePickerDialog(
        activity.baseContext, -1, listener,
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH],
        calendar[Calendar.DAY_OF_MONTH]
    ).show()
  }

  /**
   * Show basic confirm Dialog from factory
   */
  fun showBasicConfirmDialog(
    @StringRes title: Int, @StringRes message: Int?,
    positiveAction: String = activity.getString(android.R.string.ok),
    negativeAction: String = activity.getString(android.R.string.cancel),
    positiveClickListener: (DialogInterface) -> Unit
  ) {
    val titleStr = activity.getString(title)
    val messageStr = message?.let { activity.getString(it) }
    val actionBtnColor = ResourcesCompat.getColor(
        activity.resources, R.color.colorAccent,
        null
    )
    showConfirmDialog(
        activity, titleStr, messageStr, positiveAction, negativeAction,
        DialogInterface.OnClickListener { d, _ -> positiveClickListener(d) }, null, actionBtnColor,
        actionBtnColor
    )
  }

  /**
   * Show Confirm Dialog
   *
   * @param context - Activity Context
   * @param title - Dialog title
   * @param message - Dialog Message
   * @param positiveBtnText - Used to set Positive Button for Dialog
   * @param negativeBtnText - Used to set Negative Button for Dialog
   * @param positiveClickListener - [android.content.DialogInterface.OnClickListener] for Positive Action
   * @param negativeClickListener - [android.content.DialogInterface.OnClickListener] for Negative Action(default is null-dismiss dialog)
   * @param positiveBtnClr - [ColorInt] Color of positive Action Button
   * @param negativeBtnClr - [ColorInt] Color of negative Action Button
   */
  fun showConfirmDialog(
    context: Context,
    title: String,
    message: String?,
    positiveBtnText: String,
    negativeBtnText: String?,
    positiveClickListener: OnClickListener,
    negativeClickListener: OnClickListener? = null, @ColorInt positiveBtnClr: Int, @ColorInt negativeBtnClr: Int
  ) {

    val dialog = AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(Html.fromHtml(message))
        .setPositiveButton(positiveBtnText, positiveClickListener)
        .setNegativeButton(negativeBtnText, negativeClickListener)
        .create()

    dialog.setOnShowListener {
      dialog.getButton(AlertDialog.BUTTON_POSITIVE)
          .setTextColor(positiveBtnClr)
      dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
          .setTextColor(negativeBtnClr)
    }

    dialog.show()
  }

  /**
   * Show error dialog
   *
   * @param error Error message
   * @param dismissTimeout Ms after dismiss dialog
   */
  fun showErrorDialog(
    error: String,
    dismissTimeout: Long = -1
  ) =
    ErrorDialog(
      activity, error, dismissTimeout
  ).show()
}