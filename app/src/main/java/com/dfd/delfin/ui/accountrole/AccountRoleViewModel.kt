package com.dfd.delfin.ui.accountrole

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.LoadboardRepository
import com.dfd.delfin.api.request.UpdateUserRequest
import com.dfd.delfin.ui.accountaction.AccountType
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
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
        updateUserRequest.phoneNumber = userPrefs.phoneNumber.toString()
        compositeDisposable += loadboardRepository.updateUser(updateUserRequest)
                .onBackground()
                .subscribe { _res, error ->
                    if (!error && _res != null) {
                        userPrefs.userRole = updateUserRequest.userRole?:""
                        updateUserStatusLiveData.postValue(_res)
                    } else {
                        error.handle()
                    }
                }
    }

}