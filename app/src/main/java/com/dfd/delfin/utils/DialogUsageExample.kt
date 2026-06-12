package com.dfd.delfin.utils

import android.app.Dialog
import com.dfd.delfin.R
import com.dfd.delfin.injection.scope.ActivityScope
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Example usage of the showDetailsSubmittedSuccessDialog function
 * This is just for demonstration purposes
 */
@ActivityScope
class DialogUsageExample @Inject constructor(
    private val activity: DaggerAppCompatActivity,
    private val dialogUtils: DialogUtils
) {

    /**
     * Example function showing how to use the new dialog
     */
    fun showSuccessDialogExample() {
        // Example data - replace with actual data from your variables
        val title = activity.getString(R.string.title_dialog_success)
        val subTittle = activity.getString(R.string.sub_title_dialog_success)
        val playStoreLink = activity.getString(R.string.driver_app_link)
        val hindiVideoLink = activity.getString(R.string.hindi_video_link)
        val englishVideoLink = activity.getString(R.string.english_video_link)
        val ticketId = "TKT123456"
        val reportingCentre = "https://maps.google.com/?q=Mumbai+MIDC"
        val reportingTime = "09:00 AM"
        //val hindiVideoLink = "https://youtube.com/watch?v=hindi_video_id"
        //val englishVideoLink = "https://youtube.com/watch?v=english_video_id"

        // Show the dialog
        val dialog = dialogUtils.showDetailsSubmittedSuccessDialog(
            title = title,
            subTittle = subTittle,
            playStoreLink = playStoreLink,
            ticketId = ticketId,
            reportingCentre = reportingCentre,
            reportingTime = reportingTime,
            hindiVideoLink = hindiVideoLink,
            englishVideoLink = englishVideoLink,
            dialogInterface = object : DetailsSubmittedSuccessInterface {
                override fun onDialogDismissed() {
                    // Handle dialog dismissal if needed
                    println("Dialog dismissed")
                }
            }
        )
    }

    /**
     * Example function showing how to use the dialog with dynamic data
     */
    fun showSuccessDialogWithDynamicData(
        title: String,
        subTittle: String,
        ticketId: String,
        reportingCentre: String,
        reportingTime: String,
        playStoreLink: String,
        hindiVideoLink: String,
        englishVideoLink: String
    ): Dialog {
        return dialogUtils.showDetailsSubmittedSuccessDialog(
            title = title,
            subTittle = subTittle,
            ticketId = ticketId,
            reportingCentre = reportingCentre,
            reportingTime = reportingTime,
            playStoreLink = playStoreLink,
            hindiVideoLink = hindiVideoLink,
            englishVideoLink = englishVideoLink,
            dialogInterface = object : DetailsSubmittedSuccessInterface {
                override fun onDialogDismissed() {
                    // Handle dialog dismissal
                    println("Dialog dismissed")
                }
            }
        )
    }
} 