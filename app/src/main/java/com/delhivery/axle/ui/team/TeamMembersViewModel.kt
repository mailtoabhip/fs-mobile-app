package com.delhivery.axle.ui.team

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.data.teammembers.TeamWarningItem_NoMember
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.json.JSONArray
import javax.inject.Inject

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 28/12/20
 */

class TeamMembersViewModel @Inject constructor(
  private val userRepository: UserRepository,
  private val loadboardRepository: LoadboardRepository,
  val userPrefs: UserPrefs
) : BaseViewModel(), TeamMembersCreateDialogInterface ,TeamMembersEditDialogInterface, AdminViewDialogInterface {

  var membersLiveData = MutableLiveData<List<Pair<BaseTeamMembersRVAdapterItem<*>, DataRVAdapterOperationType>>>()
  var createUserLiveData = MutableLiveData<Pair<String,String>>()
  var updateUserLiveData = MutableLiveData<String>()
  var deleteUserLiveData = MutableLiveData<String>()
  var updateAdminUserLiveData = MutableLiveData<String>()
  var total = 0
  var emptyUserLiveData = MutableLiveData<Boolean>()

  /**
   * Fetch team members of logged in user
   */
  fun fetchTeamMembers() {

    Pair(TeamMembersProgressItem(), AddUpdate).let { membersLiveData.postValue(listOf(it)) }

    compositeDisposable += loadboardRepository.getUserTeamMembers(userRepository.userId())
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            mutableListOf<Pair<BaseTeamMembersRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {

              add(Pair(TeamMembersProgressItem(), Remove))

              if (_res.count > 0) {
                for (user in _res.users) {
                  if (user.isParent()) {
                    add(Pair(TeamMemberAdminUserItem(user), Add))
                  } else {
                    add(Pair(TeamMemberSubUserItem(user), Add))
                  }
                }
                emptyUserLiveData.postValue(false)
              }
              else{
                emptyUserLiveData.postValue(true)
                add(Pair(TeamWarningItem_NoMember, Add))
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
      number: String,
      dieselPreference: String,
      dieselCompany: List<String>
  ) {
    val jsonObject = JsonObject()
    val jsonArray = JsonArray()
    for (i in dieselCompany){
        jsonArray.add(i)
    }
    jsonObject.addProperty("user_name", name)
    jsonObject.addProperty("phone_number", "+91"+number)
    jsonObject.addProperty("parent_uuid", userRepository.userId())
    jsonObject.addProperty("diesel_card_preference",dieselPreference)
    jsonObject.add("diesel_company", jsonArray)
    jsonObject.addProperty("parent_sp_id", userRepository.userId())
    jsonObject.addProperty("originator", "axle-app")

    compositeDisposable += loadboardRepository.createSecondaryUser(jsonObject)
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            createUserLiveData.postValue(Pair(_res,number))
          } else {
            error.handle()
            createUserLiveData.postValue(null)

          }
        }
  }

  /**
   * Update team member name
   */
  override fun editMember(
    uuid: String,
    name: String,
    dieselPreference: String,
    dieselCompany: List<String>
  ) {
    val jsonObject = JsonObject()
    val jsonArray = JsonArray()
    for (i in dieselCompany){
      jsonArray.add(i)
    }
    jsonObject.addProperty("user_name", name)
    jsonObject.addProperty("phone_number", userPrefs.phoneNumber)
    jsonObject.addProperty("parent_uuid", uuid)
    jsonObject.addProperty("diesel_card_preference",dieselPreference)
    jsonObject.add("diesel_company", jsonArray)
    jsonObject.addProperty("parent_sp_id", uuid)
    jsonObject.addProperty("originator", "axle-app")

    compositeDisposable += loadboardRepository.updateSecondaryUser(jsonObject)
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
    jsonObject.addProperty("phone_number", userPrefs.phoneNumber)
    jsonObject.addProperty("is_deleted", true)
    jsonObject.addProperty("parent_uuid", uuid)
    jsonObject.addProperty("parent_sp_id", userRepository.userId())
    jsonObject.addProperty("originator", "axle-app")

    compositeDisposable += loadboardRepository.updateSecondaryUser(jsonObject)
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

    override fun updateAdminMember(
      uuid: String,
      dieselPreference: String,
      dieselCompany: List<String>) {
      val jsonObject = JsonObject()
      val jsonArray = JsonArray()
      for (i in dieselCompany){
        jsonArray.add(i)
      }
      jsonObject.addProperty("phone_number", userPrefs.phoneNumber)
      jsonObject.addProperty("diesel_card_preference",dieselPreference)
      jsonObject.addProperty("parent_uuid", uuid)
      jsonObject.addProperty("parent_sp_id", uuid)
      jsonObject.add("diesel_company", jsonArray)
      jsonObject.addProperty("parent_sp_id", uuid)
      jsonObject.addProperty("originator", "axle-app")

      compositeDisposable += loadboardRepository.updateAdminUser(jsonObject)
          .onBackground()
          .progress()
          .subscribe{ _res , error ->
            if (!error && _res != null) {
              updateAdminUserLiveData.postValue(_res)
            } else {
              error.handle()
            }
          }
    }
}