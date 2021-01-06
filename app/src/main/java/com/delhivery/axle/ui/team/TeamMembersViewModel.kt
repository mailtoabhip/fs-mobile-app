package com.delhivery.axle.ui.team

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import javax.inject.Inject

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 28/12/20
 */

class TeamMembersViewModel @Inject constructor(
  private val userRepository: UserRepository,
  val userPrefs: UserPrefs
) : BaseViewModel(), TeamMembersCreateEditDialogInterface {

  var membersLiveData = MutableLiveData<List<Pair<BaseTeamMembersRVAdapterItem<*>, DataRVAdapterOperationType>>>()
  var createUserLiveData = MutableLiveData<String>()
  var updateUserLiveData = MutableLiveData<String>()
  var deleteUserLiveData = MutableLiveData<String>()
  var total = 0

  /**
   * Fetch team members of logged in user
   */
  fun fetchTeamMembers() {

    Pair(TeamMembersProgressItem(), AddUpdate).let { membersLiveData.postValue(listOf(it)) }

    compositeDisposable += userRepository.getUserTeamMembers(0, 100, true, userRepository.userId())
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            mutableListOf<Pair<BaseTeamMembersRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {

              add(Pair(TeamMembersProgressItem(), Remove))

              if (_res.total > 0) {

                for (user in _res.users) {
                  if (user.isParent()) {
                    add(Pair(TeamMemberAdminUserItem(user), Add))
                  } else {
                    add(Pair(TeamMemberSubUserItem(user), Add))
                  }
                }
              }
            }.let {
              membersLiveData.postValue(it)
            }
          } else {
            mutableListOf<Pair<BaseTeamMembersRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(Pair(TeamMembersProgressItem(), Remove))
            }.let {
              membersLiveData.postValue(it)
            }
          }
        }
  }

  /**
   * Create team member
   */
  override fun createMember(
    name: String,
    number: String
  ) {
    val jsonObject = JsonObject()
    jsonObject.addProperty("name", name)
    jsonObject.addProperty("phone_no", number)
    jsonObject.addProperty("parent_sp_id", userRepository.userId())
    jsonObject.addProperty("originator", "axle-app")

    compositeDisposable += userRepository.createSecondaryUser(jsonObject)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            createUserLiveData.postValue(_res.message)
          } else {
            error.handle()
          }
        }
  }

  /**
   * Update team member name
   */
  override fun editMember(
    uuid: String,
    name: String) {
    val jsonObject = JsonObject()
    jsonObject.addProperty("name", name)
    jsonObject.addProperty("originator", "axle-app")

    compositeDisposable += userRepository.updateSecondaryUser(uuid, jsonObject)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            updateUserLiveData.postValue(_res)
          } else {
            error.handle()
          }
        }
  }

  /**
   * Delete team member
   */
  fun deleteMember(uuid: String) {
    val jsonObject = JsonObject()
    jsonObject.addProperty("is_deleted", true)
    jsonObject.addProperty("originator", "axle-app")

    compositeDisposable += userRepository.updateSecondaryUser(uuid, jsonObject)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            deleteUserLiveData.postValue(_res)
          } else {
            error.handle()
          }
        }
  }
}