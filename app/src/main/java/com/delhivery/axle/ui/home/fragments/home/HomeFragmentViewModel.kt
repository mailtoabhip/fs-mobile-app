package com.delhivery.axle.ui.home.fragments.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class HomeFragmentViewModel @Inject constructor(
    private val userPrefs: UserPrefs
) : BaseViewModel() {

    private val _kycUiModel = MutableLiveData<KycUiModel>()
    val kycUiModel: LiveData<KycUiModel> = _kycUiModel

    fun refreshKycStatus() {
        _kycUiModel.value = calculateKycUiModel()
    }

    private fun calculateKycUiModel(): KycUiModel {
        val isLoadBoardUser = userPrefs.isLoadBoardClient && userPrefs.isLoadBoardSupplier

        if (!isLoadBoardUser || userPrefs.isUserVerfied) {
            return KycUiModel(uiState = KycUiState.COMPLETED, progress = 100)
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
                when (userPrefs.verificationStatus) {
                    "pending" -> KycUiState.VERIFICATION_PENDING
                    "success" -> KycUiState.COMPLETED
                    "failed" -> KycUiState.REJECTED
                    else -> KycUiState.CONTINUE
                }
            }
            else -> KycUiState.CONTINUE
        }

        val progress = when (uiState) {
            KycUiState.START -> 0
            KycUiState.CONTINUE -> 50
            else -> 100
        }

        return KycUiModel(uiState = uiState, progress = progress)
    }
}
