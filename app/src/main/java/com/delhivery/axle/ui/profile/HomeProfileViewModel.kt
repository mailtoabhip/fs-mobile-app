package com.delhivery.axle.ui.profile

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.TransactionsRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.api.response.MonthlyEarning
import com.delhivery.axle.config.AWSConfig
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
  val userPrefs: UserPrefs
) : BaseViewModel() {

  var tripEarningLiveData = MutableLiveData<Map<Int, MonthlyEarning?>>()

  var userRoleLiveData = MutableLiveData<Boolean>()

  /* states */
  var stateLiveData = MutableLiveData<ProfileUIState>()
  var state: ProfileUIState = ProfileUIState.Axle
    set(value) {
      stateLiveData.postValue(value)
    }

  var userName = userPrefs.userName
  var businessName = userPrefs.companyName
  var mobile = "\u2022 ${userPrefs.phoneNumber}"
  var bName = userPrefs.bankName
  var aNum = userPrefs.accNumber
  var ifsc = userPrefs.ifscCode

  var delegationDownloadLiveData = MutableLiveData<Triple<DelegationToken, String, File>>()
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



  /**
   * Verify user has permission as can_create_secondary_sp
   */
  fun verifyRole() {
    compositeDisposable += userRepository.fetchUserRoles()
        .onBackground()
        .subscribe { res, error ->
          if (!error) {
            if (res != null && !res.roles.isNullOrEmpty()) {
              var permission = false
              res.permissions.forEach {
                if (it.name == "can_create_secondary_sp") {
                  permission = true
                }
              }
              if (permission) {
                userRoleLiveData.postValue(true)
              } else {
                userRoleLiveData.postValue(false)
              }
            }
          } else {
            userRoleLiveData.postValue(false)
          }
        }
  }

  fun logout() {
    userPrefs.clearPrefs()
  }

  fun setUserState() {
    if (userPrefs.accountSetup) {
      if (userPrefs.userMode.equals("post_load")) {
        state = ProfileUIState.PostLoad
      } else {
        state = ProfileUIState.PostTruck
      }
    } else {
      state = ProfileUIState.Axle
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