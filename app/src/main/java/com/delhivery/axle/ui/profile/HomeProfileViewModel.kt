package com.delhivery.axle.ui.profile

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.request.UpdateUserRequest
// Removed DelegationToken import - no longer needed
import com.delhivery.axle.api.response.KYCDetailResponse
import com.delhivery.axle.api.response.MonthlyEarning
// Removed AWSConfig import - no longer needed
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import javax.inject.Inject

class HomeProfileViewModel @Inject constructor(
  private val transactionsRepository: TransactionsRepository,
  private val userRepository: UserRepository,
  private val loadboardRepository: LoadboardRepository,
  val userPrefs: UserPrefs
) : BaseViewModel() {

  var tripEarningLiveData = MutableLiveData<Map<Int, MonthlyEarning?>>()

  var getUserLiveData = MutableLiveData<Boolean>()

  var verificationStatus = MutableLiveData<String>()


  var accountDeleteLiveData = MutableLiveData<Boolean>(false)


  /* states */
  var stateLiveData = MutableLiveData<ProfileUIState>()
  var state: ProfileUIState = ProfileUIState.Axle
    set(value) {
      stateLiveData.postValue(value)
    }

  var userName = userPrefs.userName
  var businessName = userPrefs.companyName
  var mobile = "\u2022 "+ userPrefs.phoneNumber?.replace("+91","")
  var bName = userPrefs.bankName
  var aNum = userPrefs.accNumber
  var ifsc = userPrefs.ifscCode

  var imagePath = ""
  var imageUrl = ""

  fun fetchTripMeter() {
    compositeDisposable += transactionsRepository.transactionTripMeter()
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error) {
            val earningMap = mutableMapOf<Int, MonthlyEarning?>().apply {
              put(1, _res.jan)
              put(2, _res.feb)
              put(3, _res.mar)
              put(4, _res.apr)
              put(5, _res.may)
              put(6, _res.jun)
              put(7, _res.jul)
              put(8, _res.aug)
              put(9, _res.sep)
              put(10, _res.oct)
              put(11, _res.nov)
              put(12, _res.dec)
            }
                .filter { it.value != null }
                .toSortedMap()
            tripEarningLiveData.postValue(earningMap)
          } else {
            tripEarningLiveData.postValue(null)
          }
        }
  }


  fun logout() {
    userPrefs.lastLoggedInUserId = userPrefs.userId()
    Log.i("LoggedInUser", userPrefs.lastLoggedInUserId)
    userPrefs.clearPrefs()
  }

  fun setUserState(){
    if(userPrefs.isLoadBoardClient == false || userPrefs.isLoadBoardSupplier == false){
      state = ProfileUIState.Axle
    }else{
      if(userPrefs.userMode.equals("post_load")){
        state = ProfileUIState.PostLoad
      }else{
        state = ProfileUIState.PostTruck
      }
    }
  }

  // Removed download delegation token logic - new API only supports upload

  // Download functionality
  var documentListLiveData = MutableLiveData<List<com.delhivery.axle.api.response.DocumentFile>>()
  var documentListErrorLiveData = MutableLiveData<String>()

  fun loadProfileImages() {
    // This method can be called from Activity to trigger profile image loading
    // The actual API call is handled by DocumentUtils in the Activity
    documentListLiveData.postValue(emptyList()) // Initialize empty list
  }

  fun getUser() {
    compositeDisposable += userRepository.getUser(false)
        .onBackground()
        .subscribe { _user, error ->
          if (!error) {
            getUserLiveData.postValue(true)
          } else {
            error.handle()
          }
        }
  }

  fun deleteAccountRequest() {
    compositeDisposable += loadboardRepository.updateUser(UpdateUserRequest(phoneNumber = userPrefs.phoneNumber!!, requestedDeletion = true))
      .progress()
      .onBackground()
      .subscribe { _res, error ->
        if (!error) {
          accountDeleteLiveData.postValue(true)
        } else
          error.handle()
      }
  }

}