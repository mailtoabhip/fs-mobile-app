package com.delhivery.axle.ui.profile.profiledetails

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import javax.inject.Inject

class ProfileDetailsViewModel @Inject constructor(
        private val userRepository: UserRepository,
        private val loadboardRepository: LoadboardRepository,
        val userPrefs: UserPrefs
): BaseViewModel() {

    var userName = MutableLiveData<String>()
    var businessName = MutableLiveData<String>()
    var mobile = userPrefs.phoneNumber?.replace("+91","")
    var userMode = userPrefs.userMode
    var userRole = userPrefs.userRole
    var loadSwitch = userPrefs.canViewThirdPartyLoads
    var changeRoleDialogVisibility =0

    var delegationLiveData = MutableLiveData<Pair<DelegationToken, File>>()
    var delegationDownloadLiveData = MutableLiveData<Triple<DelegationToken, String, File>>()
    var statusLiveData = MutableLiveData<Boolean>()
    var imagePath = ""
    var imageUrl = ""

    var userUpdateLiveData = MutableLiveData<Boolean>()

    /**
     * Get delegation token for AWS
     */
    fun getDelegationToken(file: File) {
        compositeDisposable += userRepository.getDelegationToken(AWSConfig.Target.value())
                .onBackground()
                .progress()
                .subscribe { _res, error ->
                    if (!error) {
                        delegationLiveData.postValue(Pair(_res.delegationToken, file))
                    } else
                        error.handle()
                }
    }

    fun updateUserDetails() {
        if (!isConnected) return
            compositeDisposable += loadboardRepository.updateUser(UpdateUserRequest(phoneNumber= userPrefs.phoneNumber.toString(), userName = userName.value, businessName = businessName.value, userMode = userMode, userRole = userRole, profileImageUrl = imageUrl, canViewThirdPartyLoads = loadSwitch))
                    .onBackground()
                    .progress()
                    .subscribe { _res, error ->
                        if (!error) {
                            userPrefs.companyName = businessName.value?:""
                            userPrefs.userName = userName.value?:""
                            userPrefs.profileImageUrl = imageUrl?:""
                            userPrefs.userRole = userRole?:""
                            userPrefs.userMode = userMode?:""
                            userPrefs.canViewThirdPartyLoads = loadSwitch?:false
                            userUpdateLiveData.postValue(true)
                        } else{
                            error.handle()
                            userUpdateLiveData.postValue(false)
                        }
                    }

    }

    fun getDownloadDelegationToken(
            awsPath: String,
            file: File
    ) {
        compositeDisposable += userRepository.getDelegationToken(AWSConfig.Target.value())
                .onBackground()
                .subscribe { _res, error ->
                    if (!error) {
                        delegationDownloadLiveData.postValue(Triple(_res.delegationToken, awsPath, file))
                    } else
                        error.handle()
                }
    }

}