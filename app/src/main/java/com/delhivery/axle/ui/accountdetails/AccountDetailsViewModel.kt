package com.delhivery.axle.ui.accountdetails

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.FsAuthRepository
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.ui.accountaction.AccountType
import com.delhivery.axle.ui.accountrole.AccountRole
import com.delhivery.axle.ui.auth.AuthenticationUIState
import com.delhivery.axle.ui.auth.AuthenticationUIState.*
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * View model for [AccountDetailsActivity]
 */
class AccountDetailsViewModel @Inject constructor(
    private val fsAuthRepository: FsAuthRepository,
    private val userPrefs: UserPrefs
) : BaseViewModel() {

    /* states */
    var stateLiveData = MutableLiveData<AuthenticationUIState>()
    var errorAccountCreate: String? = null
    var state: AuthenticationUIState = PhoneNo
        set(value) {
            stateLiveData.postValue(value)
        }

    var createUserStatusLiveData = MutableLiveData<String>()

    /* binding vars — username maps to first_name in the new contract */
    var firstName = MutableLiveData<String>()
    var lastName = MutableLiveData<String>()
    var commConsent = MutableLiveData<Boolean>()
    var locationOption = MutableLiveData<Boolean>()

    /**
     * Saves the user's profile via PUT /api/v1/auth/profile.
     * [firstName] is used as first_name, [lastName] as last_name.
     * [commConsent] is taken from the whatsapp/comms checkbox value.
     */
    fun createAccount() {
        if (!isConnected) return

        val firstName = firstName.value?.trim()
        val lastName = lastName.value?.trim()
        val commConsent = commConsent.value ?: false

        if (firstName.isNullOrEmpty()) return

        state = LoginProgress

        viewModelScope.launch {
            when (val result = fsAuthRepository.updateProfile(
                firstName = firstName,
                lastName = lastName?.ifEmpty { null },
                commConsent = commConsent
            )) {
                is Resource.Success -> {
                    userPrefs.lastLoginTime = Date().time
                    state = HomePage
                }
                is Resource.Failure -> {
                    Log.e("AccountDetailsVM", "updateProfile failed: code=${result.errorCode}, error=${result.apiError}")
                    errorAccountCreate = "Something went wrong. Please try again."
                    state = OTP // reuses OTP state to show snackbar error in the Activity
                }
                Resource.Loading -> { /* no-op */ }
            }
        }
    }
}
