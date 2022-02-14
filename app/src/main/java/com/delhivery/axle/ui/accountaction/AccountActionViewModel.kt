package com.delhivery.axle.ui.accountaction

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.util.*
import javax.inject.Inject

/**
 * View model for [AccountActionActivity]
 */
class AccountActionViewModel @Inject constructor(
        private val loadboardRepository: LoadboardRepository,
        private val userPrefs: UserPrefs
) : BaseViewModel() {

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
                        userPrefs.userMode = updateUserRequest.userMode?:""
                        updateUserStatusLiveData.postValue(_res)
                    } else {
                        error.handle()
                    }
                }
    }

}