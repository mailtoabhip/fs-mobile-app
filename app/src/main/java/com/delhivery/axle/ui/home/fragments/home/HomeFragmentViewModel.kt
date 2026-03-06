package com.delhivery.axle.ui.home.fragments.home

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.R
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class HomeFragmentViewModel @Inject constructor(
    private val userPrefs: UserPrefs
) : BaseViewModel() {

    private val _kycUiModel = MutableLiveData<KycUiModel>()
    val kycUiModel: LiveData<KycUiModel> = _kycUiModel

    enum class KycUiState {
        START,
        CONTINUE,
        COMPLETED,
        REJECTED,
        VERIFICATION_PENDING
    }

    data class KycUiColors(
        val cardColorLight: Int,
        val cardColorDark: Int,
        val progressColor: Int,
        val progressBgColor: Int
    )

    data class KycUiModel(
        @field:StringRes val headingResId: Int,
        @field:StringRes val messageResId: Int,
        @field:StringRes val buttonTextResId: Int,
        val progress: Int,
        val uiState: KycUiState,
        val colors: KycUiColors
    )

    companion object {
        private val DEFAULT_COLORS = KycUiColors(
            cardColorLight = R.color.cardboard_surface_light,
            cardColorDark = R.color.cardboard_surface_dark,
            progressColor = R.color.progress_copper,
            progressBgColor = R.color.bg_light_grey_v2
        )

        private val COMPLETED_COLORS = KycUiColors(
            cardColorLight = R.color.surface_success_dark,
            cardColorDark = R.color.surface_success_light,
            progressColor = R.color.bid_placed_green,
            progressBgColor = R.color.light_green
        )

        private val REJECTED_COLORS = KycUiColors(
            cardColorLight = R.color.surface_failed_dark,
            cardColorDark = R.color.surface_failed_light,
            progressColor = R.color.progress_red,
            progressBgColor = R.color.light_red
        )
    }

    private fun getColorsForState(state: KycUiState): KycUiColors {
        return when (state) {
            KycUiState.START, KycUiState.CONTINUE,KycUiState.VERIFICATION_PENDING -> DEFAULT_COLORS
            KycUiState.COMPLETED -> COMPLETED_COLORS
            KycUiState.REJECTED -> REJECTED_COLORS
        }
    }

    fun refreshKycStatus() {
        _kycUiModel.value = calculateKycUiModel()
    }

    private fun calculateKycUiModel(): KycUiModel {
        val currentVerificationStatus = userPrefs.verificationStatus
        val isLoadBoardUser = userPrefs.isLoadBoardClient && userPrefs.isLoadBoardSupplier

        if (!isLoadBoardUser || userPrefs.isUserVerfied) {
            return KycUiModel(
                headingResId = R.string.kyc_heading_completed,
                messageResId = R.string.kyc_message_completed,
                buttonTextResId = R.string.kyc_button_completed,
                progress = 100,
                uiState = KycUiState.COMPLETED,
                colors = COMPLETED_COLORS
            )
        }

        val steps = listOf(
            userPrefs.pancard.isNotEmpty(),
            userPrefs.aadhaarNumber.isNotNullOrEmpty() ||
                    userPrefs.gstNumber.isNotNullOrEmpty() ||
                    userPrefs.cinNumber.isNotNullOrEmpty() ||
                    userPrefs.shopNumber.isNotNullOrEmpty() ||
                    userPrefs.udyogNumber.isNotNullOrEmpty(),
            userPrefs.businessAddress.isNotEmpty(),
            if (userPrefs.userMode.equals("post_load", true))
                true
            else
                userPrefs.rcNumber.isNotEmpty() || userPrefs.isTruckingDocumentUploaded,
            userPrefs.ifscCode.isNotEmpty() &&
                    userPrefs.accNumber.isNotEmpty() &&
                    !userPrefs.accNumber.equals("Not Available", true),
            userPrefs.vendorPolicyAccepted
        )

        val totalSteps = steps.size
        val completedSteps = steps.count { it }

        val uiState = when (completedSteps) {
            0 -> KycUiState.START
            totalSteps -> {
                when (currentVerificationStatus) {
                    "pending" -> KycUiState.VERIFICATION_PENDING
                    "success" -> KycUiState.COMPLETED
                    "failed" -> KycUiState.REJECTED
                    else -> KycUiState.CONTINUE
                }
            }
            else -> KycUiState.CONTINUE
        }

        return when (uiState) {
            KycUiState.START -> KycUiModel(
                headingResId = R.string.kyc_heading_start,
                messageResId = R.string.kyc_message_start,
                buttonTextResId = R.string.kyc_button_start,
                progress = 0,
                uiState = uiState,
                colors = getColorsForState(uiState)
            )
            KycUiState.CONTINUE -> KycUiModel(
                headingResId = R.string.kyc_heading_continue,
                messageResId = R.string.kyc_message_continue,
                buttonTextResId = R.string.kyc_button_continue,
                progress = 50,
                uiState = uiState,
                colors = getColorsForState(uiState)
            )
            KycUiState.COMPLETED -> KycUiModel(
                headingResId = R.string.kyc_heading_completed,
                messageResId = R.string.kyc_message_completed,
                buttonTextResId = R.string.kyc_button_completed,
                progress = 100,
                uiState = uiState,
                colors = getColorsForState(uiState)
            )
            KycUiState.VERIFICATION_PENDING -> KycUiModel(
                headingResId = R.string.kyc_heading_pending,
                messageResId = R.string.kyc_message_pending,
                buttonTextResId = R.string.kyc_button_pending,
                progress = 100,
                uiState = uiState,
                colors = getColorsForState(uiState)
            )
            KycUiState.REJECTED -> KycUiModel(
                headingResId = R.string.kyc_heading_rejected,
                messageResId = R.string.kyc_message_rejected,
                buttonTextResId = R.string.kyc_button_rejected,
                progress = 100,
                uiState = uiState,
                colors = getColorsForState(uiState)
            )
        }
    }
}
