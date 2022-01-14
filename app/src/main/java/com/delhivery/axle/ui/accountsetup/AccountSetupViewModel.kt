package com.delhivery.axle.ui.accountsetup

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.AuthenticationRepository
import com.delhivery.axle.api.repository.NotificationRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidOTP
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidPhoneNo
import com.delhivery.axle.ui.auth.AuthenticationUIState
import com.delhivery.axle.ui.auth.AuthenticationUIState.Disabled
import com.delhivery.axle.ui.auth.AuthenticationUIState.LoadRequest
import com.delhivery.axle.ui.auth.AuthenticationUIState.LoginProgress
import com.delhivery.axle.ui.auth.AuthenticationUIState.OTP
import com.delhivery.axle.ui.auth.AuthenticationUIState.PhoneNo
import com.delhivery.axle.ui.auth.AuthenticationUIState.SelectRoute
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import retrofit2.HttpException
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

/**
 * View model for [AuthenticationActivity]
 */
class AccountSetupViewModel @Inject constructor(
  private val authenticationRepository: AuthenticationRepository,
  private val userRepository: UserRepository,
  private val notificationRepository: NotificationRepository,
  private val userPrefs: UserPrefs
) :
    BaseViewModel() {

    /* states */
    var stateLiveData = MutableLiveData<AccountSetupUIState>()
    var state: AccountSetupUIState = AccountSetupUIState.PrimaryAction
        set(value) {
            stateLiveData.postValue(value)
        }
}