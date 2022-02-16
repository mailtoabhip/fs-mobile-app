package com.delhivery.axle.utils

import android.Manifest
import android.R.string
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.delhivery.axle.R
import com.delhivery.axle.databinding.*
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.ui.custom.DelhiveryOTPViewInterface
import com.delhivery.axle.ui.dialogs.ErrorDialog
import com.delhivery.axle.ui.kyc.gst.DocUploadAdapter
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
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
    calendar: Calendar = Calendar.getInstance(),
    minDate: Int = Int.MIN_VALUE,
    maxDate: Int = Int.MAX_VALUE
  ) {
    val picker = DatePickerDialog(
        activity, 0, listener,
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH],
        calendar[Calendar.DAY_OF_MONTH]
    )
    if (minDate != Int.MIN_VALUE) {
      val cal = Calendar.getInstance()
      cal.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
      cal.add(Calendar.DATE, minDate)
      picker.datePicker.minDate = cal.timeInMillis
    }
    if (maxDate != Int.MAX_VALUE) {
      val cal = Calendar.getInstance()
      cal.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
      cal.add(Calendar.DATE, maxDate)
      picker.datePicker.maxDate = cal.timeInMillis
    }
    picker.show()
  }

  /**
   * Show basic confirm Dialog from factory
   */
  fun showBasicConfirmDialog(
    @StringRes title: Int, @StringRes message: Int?,
    positiveAction: String = activity.getString(string.ok),
    negativeAction: String = activity.getString(string.cancel),
    positiveClickListener: (DialogInterface) -> Unit,
    negativeClickListener: ((DialogInterface) -> Unit)? = null
  ) {
    val titleStr = activity.getString(title)
    val messageStr = message?.let { activity.getString(it) }
    val actionBtnColor = ResourcesCompat.getColor(
        activity.resources, R.color.colorAccent,
        null
    )
    showConfirmDialog(
        titleStr, messageStr, positiveAction, negativeAction,
        OnClickListener { d, _ -> positiveClickListener(d) },
        OnClickListener { d, _ ->
          if (negativeClickListener != null) {
            negativeClickListener(d)
          }
        },
        actionBtnColor,
        actionBtnColor
    )
  }

  /**
   * Show Confirm Dialog
   *
   * @param title - Dialog title
   * @param message - Dialog Message
   * @param positiveBtnText - Used to set Positive Button for Dialog
   * @param negativeBtnText - Used to set Negative Button for Dialog
   * @param positiveClickListener - [android.content.DialogInterface.OnClickListener] for Positive Action
   * @param negativeClickListener - [android.content.DialogInterface.OnClickListener] for Negative Action(default is null-dismiss dialog)
   * @param positiveBtnClr - [ColorInt] Color of positive Action Button
   * @param negativeBtnClr - [ColorInt] Color of negative Action Button
   */
  private fun showConfirmDialog(
    title: String,
    message: String?,
    positiveBtnText: String,
    negativeBtnText: String?,
    positiveClickListener: OnClickListener,
    negativeClickListener: OnClickListener? = null, @ColorInt positiveBtnClr: Int, @ColorInt negativeBtnClr: Int
  ) {

    val dialog = AlertDialog.Builder(activity)
        .setTitle(title)
        .setMessage(Html.fromHtml(message))
        .setPositiveButton(positiveBtnText, positiveClickListener)
        .setNegativeButton(negativeBtnText, negativeClickListener)
        .create()

    dialog.setOwnerActivity(activity)
    dialog.setOnShowListener {
      dialog.getButton(AlertDialog.BUTTON_POSITIVE)
          .setTextColor(positiveBtnClr)
      dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
          .setTextColor(negativeBtnClr)
    }

    if (!activity.isFinishing)
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
  ) {
    val dialog = ErrorDialog(activity, error, dismissTimeout)
    dialog.setOwnerActivity(activity)
    if (!activity.isFinishing)
      dialog.show()
  }

    /*show upload fail dialog*/
     fun showUploadFailDialog(uploadText: String,dialogUtilsInterface: DialogUtilsInterface) {
        val dialog = Dialog(activity)
        val bindingDialog= DialogGstUploadErrorBinding.inflate(activity.layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.buttonUploadAgain.setOnClickListener {
            showVerifcationOptionsDialog(uploadText,dialogUtilsInterface)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    /*show upload fail dialog*/
    fun showUploadRcDialog(uploadText: String,dialogUtilsInterface: DialogUtilsInterface) {
        val dialog = Dialog(activity)
        val bindingDialog= DialogUploadRcBinding.inflate(activity.layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.buttonUploadAgain.setOnClickListener {
            val imageName = "Lr_doc_" + System.currentTimeMillis()+".jpg"
            dialogUtilsInterface.captureImage(imageName, imageName)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }


    fun showConfirmRoleChangeDialog(desc: String,dialogUtilsInterface: DialogUtilsInterface) {
        val dialog = Dialog(activity)
        val bindingDialog= DialogConfirmPermissionSwitchBinding.inflate(activity.layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        bindingDialog.labelErrorDoc.setText(desc)

        bindingDialog.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.buttonConfirm.setOnClickListener {
            //action after confirm button
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

//go to business verification process
    fun showCompleteBusinessverificationDialog(desc: String,dialogUtilsInterface: DialogUtilsInterface) {
        val dialog = Dialog(activity)
        val bindingDialog= DialogConfirmPermissionSwitchBinding.inflate(activity.layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        bindingDialog.labelErrorDoc.setText(desc)

        bindingDialog.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.buttonConfirm.setOnClickListener {
            //action after confirm button
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }




    /*show attachment dialog*/
     fun showAttachmentDialog(adapter:DocUploadAdapter,uploadArray:ArrayList<Pair<String, String>>,dialogUtilsInterface: DialogUtilsInterface,uploadText: String,awsUtils: AWSUtils) {
        val dialog = Dialog(activity)
        val bindingDialog= DialogGstAttachmentsBinding.inflate(activity.layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        adapter.setItems(uploadArray)
        bindingDialog.attachmentList.adapter = adapter
        if(uploadText==  activity.getString(R.string.upload_aadhaar_text))
        bindingDialog.labelGst.text = activity.getString(R.string.label_aadhaar_verification)
        bindingDialog.closeBtn.setOnClickListener {
           showVerifcationOptionsDialog(uploadText,dialogUtilsInterface)
            dialog.dismiss()
        }

        bindingDialog.buttonSubmit.setOnClickListener {
            dialogUtilsInterface.sendDocForVerification(uploadArray)
            dialog.dismiss()
        }

        bindingDialog.buttonUploadMore.setOnClickListener {
            val imageName = "Aadhaar_doc_" + System.currentTimeMillis()+".jpg"
            dialogUtilsInterface.captureImage(imageName, imageName)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    /*show verification options dialog*/
     fun showVerifcationOptionsDialog(uploadText: String, dialogUtilsInterface: DialogUtilsInterface) {
        val dialog = Dialog(activity)
        val bindingDialog= DialogVerifyGstBinding.inflate(activity.layoutInflater)
        bindingDialog.uploadDocText.text = uploadText
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.gstDocLayout.setOnClickListener {
            val imageName = "Aadhaar_doc_" + System.currentTimeMillis()+".jpg"
            dialogUtilsInterface.captureImage(imageName, imageName)
            dialog.dismiss()
        }

        bindingDialog.verifyOtpLayout.setOnClickListener {
            dialogUtilsInterface.getRequestAadhaarOtp()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }
}

interface DialogUtilsInterface {

    fun getRequestAadhaarOtp()

    fun captureImage(uploadImageName:String,localImageName:String)

    fun sendDocForVerification(uploadArray:ArrayList<Pair<String, String>>)
}

