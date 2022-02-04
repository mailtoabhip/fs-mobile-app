package com.delhivery.axle.ui.accountrole

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.ui.accountaction.AccountActionActivity
import com.delhivery.axle.ui.accountaction.AccountType
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.util.*
import javax.inject.Inject

/**
 * View model for [AccountRoleActivity]
 */
class AccountRoleViewModel @Inject constructor(
        private val loadboardRepository: LoadboardRepository,
        private val userPrefs: UserPrefs
) :
    BaseViewModel() {

    var modeLiveData = MutableLiveData<AccountType>()
    var roleLiveData = MutableLiveData<AccountRole>()
    var uiStateLiveData = MutableLiveData<RoleUIState>()
    var updateUserStatusLiveData = MutableLiveData<String>()

    /**
     * Update user
     */
    fun updateUser(updateUserRequest: UpdateUserRequest) {
        if (!isConnected) return
        updateUserRequest.phone_number = userPrefs.phoneNumber.toString()
        compositeDisposable += loadboardRepository.updateUser(updateUserRequest)
                .onBackground()
                .subscribe { _res, error ->
                    if (!error && _res != null) {
                        userPrefs.userRole = updateUserRequest.user_role?:""
                        updateUserStatusLiveData.postValue(_res)
                    } else {
                        error.handle()
                    }
                }
    }

}