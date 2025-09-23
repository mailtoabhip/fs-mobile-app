package com.delhivery.axle.utils

import android.Manifest
import android.R.string
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.delhivery.axle.R
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.delhivery.axle.databinding.*
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.ui.dialogs.ErrorDialog
import com.delhivery.axle.ui.home.fragments.placements.LoadTypes
import com.delhivery.axle.ui.kyc.gst.DocUploadAdapter
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.RewardStartDate
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.RewardStartDateCalender
import com.delhivery.axle.utils.UiUtils
import dagger.android.support.DaggerAppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@ActivityScope
class DialogUtils @Inject constructor(
    private val activity: DaggerAppCompatActivity,
    private val uiUtils: UiUtils
) {

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
    if(minDate==RewardStartDateCalender){
      var dateStr = RewardStartDate
      var curFormater = SimpleDateFormat("dd/MM/yyyy")
      var dateObj = curFormater.parse(dateStr);
      var cal = Calendar.getInstance()
      cal .setTime(dateObj)
      picker.datePicker.minDate = cal.timeInMillis
    }else if (minDate != Int.MIN_VALUE) {
      val cal = Calendar.getInstance()
      cal.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
      cal.add(Calendar.DATE, minDate)
      cal.set(Calendar.YEAR,calendar.get(Calendar.YEAR))
      picker.datePicker.minDate = cal.timeInMillis
    }
    if(maxDate==RewardStartDateCalender){
      var cal = Calendar.getInstance()
      cal .setTime( Date())
      picker.datePicker.maxDate = cal.timeInMillis
    }else if (maxDate != Int.MAX_VALUE) {
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
  fun showConfirmDialog(
    title: String,
    message: String?,
    positiveBtnText: String,
    negativeBtnText: String?,
    positiveClickListener: OnClickListener,
    negativeClickListener: OnClickListener? = null, @ColorInt positiveBtnClr: Int, @ColorInt negativeBtnClr: Int
  ) {

    val dialog = AlertDialog.Builder(activity)
        .setTitle(title)
        .setMessage(HtmlCompat.fromHtml(message!!,FROM_HTML_MODE_LEGACY))
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

        if (!activity.isFinishing){
            dialog.show()
        }
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    /*show upload fail dialog*/
    fun showUploadRcDialog(uploadText: String,dialogUtilsInterface: DialogUtilsInterface, resetManualVerification:ResetManualVerification) {
        val dialog = Dialog(activity)
        val bindingDialog= DialogUploadRcBinding.inflate(activity.layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.buttonUploadAgain.setOnClickListener {
            val imageName = "RC_doc_" + System.currentTimeMillis()+".jpg"
            dialogUtilsInterface.captureImage(imageName, imageName)
            dialog.dismiss()
        }
        dialog.setOnDismissListener {
          resetManualVerification.resetManualData(true)
        }
        if (!activity.isFinishing){
            dialog.show()
        }
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }


    fun showConfirmRoleChangeDialog(desc: String,dialogUtilsInterface: DialogUtilsInterface,selected:String,userMode:String,visibility: Int,isKycStarted:Boolean,isUserVerified:Boolean) {
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
            if(userMode=="post_load" && (visibility==2 ||visibility==3) ){
              if(!isKycStarted){
                  dialogUtilsInterface.setAccountRoleSelection(selected)
              }else if(isUserVerified) {
                  showCompleteBusinessverificationDialog(dialogUtilsInterface)
              }
            }else{
                dialogUtilsInterface.setAccountRoleSelection(selected)
            }
            dialog.dismiss()
        }

        if (!activity.isFinishing){
            dialog.show()
        }
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }


    fun showRoleChangeDialog(visibility: Int,dialogUtilsInterface: DialogUtilsInterface,userMode:String,isKycStarted:Boolean,isUserVerified:Boolean,context: Context) {
        val dialog = Dialog(activity)
        val bindingDialog= DialogEditRoleBinding.inflate(activity.layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        if(visibility==1){
            bindingDialog.shipperSection.visibility=View.VISIBLE
            bindingDialog.transporterSection.visibility=View.GONE
            bindingDialog.bothSection.visibility=View.GONE
        }else if(visibility==2){
            bindingDialog.shipperSection.visibility=View.GONE
            bindingDialog.transporterSection.visibility=View.VISIBLE
            bindingDialog.bothSection.visibility=View.GONE
        }else if(visibility==3){
            bindingDialog.shipperSection.visibility=View.GONE
            bindingDialog.transporterSection.visibility=View.GONE
            bindingDialog.bothSection.visibility=View.VISIBLE
        }
        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.shipperLayout.setOnClickListener {
            bindingDialog.shipperLayout.isSelected = true
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.brokerLayout.isSelected = false
            bindingDialog.transporterLayout.isSelected = false
            bindingDialog.shipperLayout3.isSelected = false
            bindingDialog.brokerLayout2.isSelected = false
            bindingDialog.transporterLayout2.isSelected = false
            bindingDialog.brokerLayout3.isSelected = false
            bindingDialog.transporterLayout3.isSelected = false
            bindingDialog.ownerLayout2.isSelected = false
            bindingDialog.ownerLayout3.isSelected = false
            bindingDialog.btnProceed.isEnabled=true

        }

        bindingDialog.shipperLayout3.setOnClickListener {
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.brokerLayout.isSelected = false
            bindingDialog.transporterLayout.isSelected = false
            bindingDialog.shipperLayout3.isSelected = true
            bindingDialog.brokerLayout2.isSelected = false
            bindingDialog.transporterLayout2.isSelected = false
            bindingDialog.brokerLayout3.isSelected = false
            bindingDialog.transporterLayout3.isSelected = false
            bindingDialog.ownerLayout2.isSelected = false
            bindingDialog.ownerLayout3.isSelected = false
            bindingDialog.btnProceed.isEnabled=true
        }

        bindingDialog.brokerLayout.setOnClickListener {
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.brokerLayout.isSelected = true
            bindingDialog.transporterLayout.isSelected = false
            bindingDialog.shipperLayout3.isSelected = false
            bindingDialog.brokerLayout2.isSelected = false
            bindingDialog.transporterLayout2.isSelected = false
            bindingDialog.brokerLayout3.isSelected = false
            bindingDialog.transporterLayout3.isSelected = false
            bindingDialog.ownerLayout2.isSelected = false
            bindingDialog.ownerLayout3.isSelected = false
            bindingDialog.btnProceed.isEnabled=true
        }

        bindingDialog.brokerLayout2.setOnClickListener {
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.brokerLayout.isSelected = false
            bindingDialog.transporterLayout.isSelected = false
            bindingDialog.shipperLayout3.isSelected = false
            bindingDialog.brokerLayout2.isSelected = true
            bindingDialog.transporterLayout2.isSelected = false
            bindingDialog.brokerLayout3.isSelected = false
            bindingDialog.transporterLayout3.isSelected = false
            bindingDialog.ownerLayout2.isSelected = false
            bindingDialog.ownerLayout3.isSelected = false
            bindingDialog.btnProceed.isEnabled=true

        }

        bindingDialog.brokerLayout3.setOnClickListener {
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.brokerLayout.isSelected = false
            bindingDialog.transporterLayout.isSelected = false
            bindingDialog.shipperLayout3.isSelected = false
            bindingDialog.brokerLayout2.isSelected = false
            bindingDialog.transporterLayout2.isSelected = false
            bindingDialog.brokerLayout3.isSelected = true
            bindingDialog.transporterLayout3.isSelected = false
            bindingDialog.ownerLayout2.isSelected = false
            bindingDialog.ownerLayout3.isSelected = false
            bindingDialog.btnProceed.isEnabled=true
        }

        bindingDialog.ownerLayout2.setOnClickListener {
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.brokerLayout.isSelected = false
            bindingDialog.transporterLayout.isSelected = false
            bindingDialog.shipperLayout3.isSelected = false
            bindingDialog.brokerLayout2.isSelected = false
            bindingDialog.transporterLayout2.isSelected = false
            bindingDialog.brokerLayout3.isSelected = false
            bindingDialog.transporterLayout3.isSelected = false
            bindingDialog.ownerLayout2.isSelected = true
            bindingDialog.ownerLayout3.isSelected = false
            bindingDialog.btnProceed.isEnabled=true
        }

        bindingDialog.ownerLayout3.setOnClickListener {
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.brokerLayout.isSelected = false
            bindingDialog.transporterLayout.isSelected = false
            bindingDialog.shipperLayout3.isSelected = false
            bindingDialog.brokerLayout2.isSelected = false
            bindingDialog.transporterLayout2.isSelected = false
            bindingDialog.brokerLayout3.isSelected = false
            bindingDialog.transporterLayout3.isSelected = false
            bindingDialog.ownerLayout2.isSelected = false
            bindingDialog.ownerLayout3.isSelected = true
            bindingDialog.btnProceed.isEnabled=true

        }

        bindingDialog.transporterLayout.setOnClickListener {
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.brokerLayout.isSelected = false
            bindingDialog.transporterLayout.isSelected = true
            bindingDialog.shipperLayout3.isSelected = false
            bindingDialog.brokerLayout2.isSelected = false
            bindingDialog.transporterLayout2.isSelected = false
            bindingDialog.brokerLayout3.isSelected = false
            bindingDialog.transporterLayout3.isSelected = false
            bindingDialog.ownerLayout2.isSelected = false
            bindingDialog.ownerLayout3.isSelected = false
            bindingDialog.btnProceed.isEnabled=true
        }

        bindingDialog.transporterLayout2.setOnClickListener {
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.brokerLayout.isSelected = false
            bindingDialog.transporterLayout.isSelected = false
            bindingDialog.shipperLayout3.isSelected = false
            bindingDialog.brokerLayout2.isSelected = false
            bindingDialog.transporterLayout2.isSelected = true
            bindingDialog.brokerLayout3.isSelected = false
            bindingDialog.transporterLayout3.isSelected = false
            bindingDialog.ownerLayout2.isSelected = false
            bindingDialog.ownerLayout3.isSelected = false
            bindingDialog.btnProceed.isEnabled=true
        }

        bindingDialog.transporterLayout3.setOnClickListener {
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.shipperLayout.isSelected = false
            bindingDialog.brokerLayout.isSelected = false
            bindingDialog.transporterLayout.isSelected = false
            bindingDialog.shipperLayout3.isSelected = false
            bindingDialog.brokerLayout2.isSelected = false
            bindingDialog.transporterLayout2.isSelected = false
            bindingDialog.brokerLayout3.isSelected = false
            bindingDialog.transporterLayout3.isSelected = true
            bindingDialog.ownerLayout2.isSelected = false
            bindingDialog.ownerLayout3.isSelected = false
            bindingDialog.btnProceed.isEnabled=true
        }


        bindingDialog.btnProceed.setOnClickListener {
            //action after confirm button

            var selected = ""
            var desc =""
            if(bindingDialog.shipperLayout.isSelected ==true || bindingDialog.shipperLayout3.isSelected==true){
                selected= "Shipper"
            }else if(bindingDialog.transporterLayout.isSelected ==true || bindingDialog.transporterLayout2.isSelected==true || bindingDialog.transporterLayout3.isSelected==true){
                selected ="Transporter"
            }else if(bindingDialog.brokerLayout.isSelected ==true || bindingDialog.brokerLayout2.isSelected==true || bindingDialog.brokerLayout3.isSelected==true){
                selected ="Broker"
            }else if(bindingDialog.ownerLayout2.isSelected ==true || bindingDialog.ownerLayout3.isSelected==true){
                selected ="Fleet Owner"
            }
            if(userMode=="post_load"){
                desc= context.getString(R.string.sub_label_changing_from_post_load)
            }else if(userMode=="post_truck"){
                desc= context.getString(R.string.sub_label_changing_from_post_truck)
            }
            if(visibility==3){
                desc=context.getString(R.string.sub_label_changing_to_both)
            }
            if ((userMode=="post_load"&&visibility==1)|| (userMode=="post_truck"&&visibility==2) || (userMode=="both"&&visibility==3)){
                dialogUtilsInterface.setAccountRoleSelection(selected)
            }else{
                showConfirmRoleChangeDialog(desc,dialogUtilsInterface,selected,userMode,visibility,isKycStarted,isUserVerified)
            }

            dialog.dismiss()
        }

        if (!activity.isFinishing){
            dialog.show()
        }
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }


    //go to business verification process
    fun showCompleteBusinessverificationDialog(dialogUtilsInterface: DialogUtilsInterface) {
        val dialog = Dialog(activity)
        val bindingDialog= DialogCompleteBusinessVerificationBinding.inflate(activity.layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.buttonConfirm.setOnClickListener {
            //action after confirm button
           dialogUtilsInterface.navigateToBusinessVerification()
            dialog.dismiss()
        }

        if (!activity.isFinishing){
            dialog.show()
        }
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
        if(uploadText==  activity.getString(R.string.label_business)){
            bindingDialog.labelGst.setText(uploadText)
        }


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
          var imageName=""
          if(uploadText==  activity.getString(R.string.label_business)){
             imageName = "LR_" + System.currentTimeMillis()+".jpg"
          }else {
             imageName = "Aadhaar_" + System.currentTimeMillis() + ".jpg"
          }
            dialogUtilsInterface.captureImage(imageName, imageName)
            dialog.dismiss()
        }

        if (!activity.isFinishing){
            dialog.show()
        }
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
            val imageName = "Aadhaar_" + System.currentTimeMillis()+".jpg"
            dialogUtilsInterface.captureImage(imageName, imageName)
            dialog.dismiss()
        }

        bindingDialog.verifyOtpLayout.setOnClickListener {
            dialogUtilsInterface.getRequestAadhaarOtp()
            dialog.dismiss()
        }

        if (!activity.isFinishing){
            dialog.show()
        }
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    /*show team delete dialog*/
    fun showTeamDeleteDialog(uuid:String,teamMemberInterface: TeamMemberInterface) {
        val dialog = Dialog(activity)
        val bindingDialog= DialogConfirmDeleteBinding.inflate(activity.layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.buttonUploadAgain.setOnClickListener {
            teamMemberInterface.deleteTeamMember(uuid)
            dialog.dismiss()
        }

        if (!activity.isFinishing){
            dialog.show()
        }
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    fun showSuccessBidDialog(bidSuccessInterface: BidSuccessInterface,title: String, subTittle: String?):Dialog{
        val dialog = Dialog(activity)
        val bindingDialog= DialogBidPlacedSuccessBinding.inflate(activity.layoutInflater)
        bindingDialog.titleText.text = title
        if(subTittle!=null){
            bindingDialog.titleSubText.visibility = View.VISIBLE
            bindingDialog.titleSubText.text = subTittle
        }else{
            bindingDialog.titleSubText.visibility = View.GONE
        }
        bindingDialog.cancel.setOnClickListener {
            dialog.cancel()
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        if (!activity.isFinishing){
            dialog.show()
        }
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        return dialog
    }

    fun showDetailsSubmittedSuccessDialog(
        title: String,
        subTittle: String,
        playStoreLink: String,
        ticketId: String,
        reportingCentre: String,
        reportingTime: String,
        hindiVideoLink: String,
        englishVideoLink: String,
        dialogInterface: DetailsSubmittedSuccessInterface
    ): Dialog {
        val dialog = Dialog(activity)
        val bindingDialog = DialogDetailsSubmittedSuccessBinding.inflate(activity.layoutInflater)

        //set title
        bindingDialog.tvTitle.text = title

        //set sub-title
        bindingDialog.tvSubtitle.text = subTittle

        // Set the app link
        bindingDialog.tvAppLink.text = playStoreLink
        
        // Close button
        bindingDialog.btnClose.setOnClickListener {
            dialog.dismiss()
            dialogInterface.onDialogDismissed()
        }
        
        // Copy button
        bindingDialog.btnCopy.setOnClickListener {
            copyToClipboard(playStoreLink)
            uiUtils.showSnackbar("App link copied to clipboard")
        }
        
        // WhatsApp share button
        bindingDialog.btnShareWhatsapp.setOnClickListener {
            val shareText = generateWhatsAppShareText(
                ticketId, reportingCentre, reportingTime, 
                playStoreLink, hindiVideoLink, englishVideoLink
            )
            shareOnWhatsApp(shareText)
        }
        
        // Done button
        bindingDialog.btnDone.setOnClickListener {
            dialog.dismiss()
            dialogInterface.onDialogDismissed()
        }
        
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        if (!activity.isFinishing) {
            dialog.show()
        }
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        
        return dialog
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("App Link", text)
        clipboard.setPrimaryClip(clip)
    }
    
    private fun generateWhatsAppShareText(
        ticketId: String,
        reportingCentre: String,
        reportingTime: String,
        playStoreLink: String,
        hindiVideoLink: String,
        englishVideoLink: String
    ): String {
        return """📲 Delhivery Driver App Required !!
You've been assigned an Intracity Adhoc Ticket.

📍 Reporting Centre: $reportingCentre

⏰ Reporting Time: $reportingTime

Use the Delhivery Driver App to start your trip and mark attendance.
📥 App Link: $playStoreLink


🎥 Mark-in / Mark-out Video: 

Hindi - $hindiVideoLink

English - $englishVideoLink

✅ Mark-in & Mark-out on the Driver App must!

Thank you!"""
    }
    
    public fun shareOnWhatsApp(text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage("com.whatsapp")
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general share if WhatsApp is not installed
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            activity.startActivity(Intent.createChooser(intent, "Share via"))
        }
    }

    public fun generatePlacementWhatsappContent(
      itemData: HomePlacementsItemData

    ): String {
        val haltStop = if(itemData.haltStops().isNotEmpty()){"(${itemData.haltStops()})"}else{""}
        val intercity = itemData.loadType == LoadTypes.orionFixed.name || itemData.loadType == LoadTypes.orionSpot.name || itemData.loadType == LoadTypes.ftlRegular.name || itemData.loadType == LoadTypes.ftlAdhoc.name
        val reportingCentre  = if (intercity) {
            "🛣️ Trip: "+"${itemData.formattedOriginCity()} → ${itemData.formattedDestinationCity()} ${haltStop} "
        }else{
            "📍 Site Location: "+StringUtils.capitalize(itemData.originCenterName)
        }
        val reportingTime: String = itemData.onlyFormatReportingTime()?:""
        val vehicleAndType: String = if(itemData.vehicleNumber!=null){itemData.vehicleNumber+" | "+ itemData.vehicleType}else{itemData.vehicleType?:""}
        val origin = "${itemData.originCenterLat},${itemData.originCenterLong}"
        val destination = "${itemData.destinationCenterLat},${itemData.destinationCenterLong}"

        val waypoints = itemData.haltCenters
            ?.drop(1)                       // remove origin
            ?.dropLast(1)                   // remove destination
            ?.filter { !it.latitude.isNullOrBlank() && !it.longitude.isNullOrBlank() }
            ?.joinToString("|") { "${it.latitude},${it.longitude}" }
            .orEmpty()
// Suppose haltCenters is a list of lat/longs
        val routeMapLink=  if(!intercity){
            "📌 View Site Location on Map: "+"https://www.google.com/maps/dir/?api=1" +
            "&origin=$origin"
        }else if(waypoints.isNotEmpty()){
            "📌 View Route on Map: "+"https://www.google.com/maps/dir/?api=1" +
                    "&origin=$origin" +
                    "&destination=$destination" +
                    "&waypoints=$waypoints"
        }else{
            "📌 View Route on Map: "+"https://www.google.com/maps/dir/?api=1" +
                    "&origin=$origin" +
                    "&destination=$destination"
        }

        return """Hi, please find the trip details.
            
$reportingCentre
🚚 Vehicle: $vehicleAndType
🕒 Reporting Time: $reportingTime

 $routeMapLink"""
    }
 /* fun confirmDialog(
    homeBidsRequestItemData: HomeBidsRequestItemData,
    state: BidDetailsUserBidState_EditBid,
    context: Context,
    bidDialogInterface:BidDialogInterface,
    fromPage:String
  ) {
    val dialog = Dialog(context)
    //Designed to be changed
    val bindingDialog = DialogConfirmBulkBidBinding.inflate(activity.layoutInflater)
    val userBid = state.lowestAndUserBidPair.first
    val data = homeBidsRequestItemData
    data.numBids = state.bidsCount
    data.transactionBid = state.lowestAndUserBidPair.first
    val lowestTBid = state.lowestAndUserBidPair.second
    lowestTBid?.let {
      if (it.biddingType.compareTo(userBid?.biddingType ?: "") == 0) {
        bindingDialog.lowestBid.text = when (it) {
          null -> ""
          else -> "₹ ${
            StringUtils.formatAmount(
                it.bidAmount
            )
          }" + if (state.isPMTIndent) "/MT" else ""
        }
        data.lowestBid = when (it) {
          null -> 0.0
          else -> it.bidAmount
        }
      }
    }
    var yourBid = if (data.isPMTIndent()) {
      "₹ ${StringUtils.formatAmount(data.transactionBid?.bidAmount?:0.0)}/MT"
    } else {
      "₹ ${StringUtils.formatAmount(data.transactionBid?.bidAmount?:0.0)}"
    }
    bindingDialog.yourBid.setText(yourBid)
    var lowestAmount = if (data.isPMTIndent()) {
      "₹ ${StringUtils.formatAmount(lowestTBid?.bidAmount?:0.0)}/MT"
    } else {
      "₹ ${StringUtils.formatAmount(lowestTBid?.bidAmount?:0.0)}"
    }
    bindingDialog.lowestBid.setText(lowestAmount)
    bindingDialog.textBidInfo.setText(homeBidsRequestItemData.bidLowest())
    bindingDialog.btnEdit.setOnClickListener{
      dialog.dismiss()
      if(fromPage.equals("Bid")) {
        bidDialogInterface.bidDialog(userBid)
      }else{
        bidDialogInterface.bulkbidDialog(transaction =data )
      }
    }
    bindingDialog.btnDone.setOnClickListener{
      dialog.dismiss()
    }
    // request = data
    if (lowestTBid == userBid) {
      if (userBid?.bidAmount != null) {
        if (data.guidancePrice!=null && data.guidancePrice.compareTo(userBid?.bidAmount) < 0 ) {
          bindingDialog.textBidInfo.setCompoundDrawablesWithIntrinsicBounds(
              R.drawable.ic_higher_bid,
              0,
              0,
              0
          )
          bindingDialog.textBidInfo.setTextColor(context.resources.getColor(R.color.custom_red))
          var gPrice = if (data.isPMTIndent()) {
            "₹ ${StringUtils.formatAmount(data.guidancePrice?:0.0)}/MT"
          } else {
            "₹ ${StringUtils.formatAmount(data.guidancePrice?:0.0)}"
          }
          bindingDialog.lowestBid.setText(gPrice)
          bindingDialog.textLowestBid.setText("Suggested Price")
        } else {
          bindingDialog.textBidInfo.setCompoundDrawablesWithIntrinsicBounds(
              R.drawable.ic_green_thumb,
              0,
              0,
              0
          );
          bindingDialog.textBidInfo.setTextColor(context.resources.getColor(R.color.bid_placed_green))
        }
      }
    } else {
      bindingDialog.textBidInfo.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.ic_higher_bid,
          0,
          0,
          0
      );
      bindingDialog.textBidInfo.setTextColor(context.resources.getColor(R.color.custom_red))
    }
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(bindingDialog.root)
    dialog.window!!.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    dialog.show()

  }*/

}


interface TeamMemberInterface{
    fun deleteTeamMember(uuid: String)
}
interface BidSuccessInterface{
    fun bidPlacedSuccess(success:Boolean)
}

interface DialogUtilsInterface {

    fun getRequestAadhaarOtp()

    fun setAccountRoleSelection(selected: String)

    fun navigateToBusinessVerification()

    fun captureImage(uploadImageName:String,localImageName:String)

    fun sendDocForVerification(uploadArray:ArrayList<Pair<String, String>>)

}

interface ResetManualVerification{
  fun resetManualData(boolean: Boolean)
}

interface DetailsSubmittedSuccessInterface {
    fun onDialogDismissed()
}
