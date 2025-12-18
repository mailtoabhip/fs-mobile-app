package com.delhivery.axle.ui.profile.profiledetails

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.UpdateUserRequest
// Removed DelegationToken and AWSConfig imports - no longer needed
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

    var statusLiveData = MutableLiveData<Boolean>()
    var imagePath = ""
    var imageUrl = ""

    var userUpdateLiveData = MutableLiveData<Boolean>()

    // Removed delegation token logic - direct upload now handled in Activity
    
    // Download functionality
    var documentListLiveData = MutableLiveData<List<com.delhivery.axle.api.response.DocumentFile>>()
    var documentListErrorLiveData = MutableLiveData<String>()
    
    fun loadProfileImages() {
        // This method can be called from Activity to trigger profile image loading
        // The actual API call is handled by DocumentUtils in the Activity
        documentListLiveData.postValue(emptyList()) // Initialize empty list
    }

    fun updateUserDetails() {
        if (!isConnected) return
            compositeDisposable += loadboardRepository.updateUser(UpdateUserRequest(phoneNumber= userPrefs.phoneNumber.toString(), userName = userName.value?.trim(), businessName = businessName.value?.trim(), profileImageUrl = imageUrl))
                    .onBackground()
                    .progress()
                    .subscribe { _res, error ->
                        if (!error) {
                            userPrefs.companyName = businessName.value?:""
                            userPrefs.userName = userName.value?:""
                            userPrefs.profileImageUrl = imageUrl?:""
                            userUpdateLiveData.postValue(true)
                        } else{
                            error.handle()
                            userUpdateLiveData.postValue(false)
                        }
                    }

    }

    // Removed download delegation token logic - new API only supports upload

}