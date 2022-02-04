package com.delhivery.axle.ui.team

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.delhivery.axle.R
import com.delhivery.axle.api.request.DeleteTeamMemberAction_Delete
import com.delhivery.axle.api.request.EditTeamMemberAction_Edit
import com.delhivery.axle.api.request.ViewAdminMember
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.databinding.ActivityTeamMembersBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import java.io.Serializable

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 30/12/20
 */

class TeamMembersActivity : BaseActivity<ActivityTeamMembersBinding, TeamMembersViewModel>(),
  TeamMembersRVAdapterInterface {


  init {
    hasInlineProgress = true
  }

  private var userCreate: Boolean = false

  override fun getViewModelClass() = TeamMembersViewModel::class.java

  override fun layoutId() = R.layout.activity_team_members

  override fun requireConnection() = true

  private val adapter: TeamMembersRVAdapter by lazy { TeamMembersRVAdapter(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Your Team Members"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      refreshData()
    }

    userCreate = intent?.getBooleanExtra(USER_CREATE, false) ?: false


    if(userCreate == true){
      createTeamMember()
    }

    /* setup recycler view */
    binding.rvMembers.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@TeamMembersActivity)
      adapter = this@TeamMembersActivity.adapter
    }

    if (viewModel.userPrefs.isParent) {
      binding.layAddMember.visibility = View.VISIBLE
    } else {
      binding.layAddMember.visibility = View.GONE
    }

    viewModel.membersLiveData.observe(this, Observer {
      it?.let {
        _items -> adapter.operation(_items)
      }
    })

    viewModel.createUserLiveData.observe(this, Observer {

      if(it!=null) {
        refreshData()
        uiUtils.showSnackbar(it.first)
        if (userCreate) {
          val intent = Intent("custom-event-name")

          val num: String = it.second
          intent.putExtra("message", num)
          LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
          finish()
        }
      }
    })


    viewModel.updateUserLiveData.observe(this, Observer {
      refreshData()
      uiUtils.showSnackbar(it)
    })

    viewModel.deleteUserLiveData.observe(this, Observer {
      refreshData()
      uiUtils.showSnackbar(it)
    })

    viewModel.updateAdminUserLiveData.observe( this, Observer {
      refreshData()
      uiUtils.showSnackbar(it)
    })

    binding.fabAddMember.setOnClickListener {
      createTeamMember()
    }

    viewModel.progressLiveData.observe(this, ProgressObserver())

    refreshData()
  }

  override fun onBackPressed() {
    super.onBackPressed()
    if(userCreate){
      val intent = Intent("custom-event-name")
          val num: String = "0"
          intent.putExtra("message", num)
          LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
          finish()
    }
  }

  private fun refreshData() {
    adapter.resetStaticData()
    viewModel.fetchTeamMembers()
    binding.executePendingBindings()
  }


  override fun handleAction(
    actionId: String,
    item: BaseTeamMembersRVAdapterItem<*>
  ) {
    when (actionId) {
      EditTeamMemberAction_Edit -> {
        val data = item.data as UserModel
        val uuid = data.userId
        val name = data.user_name
        val number = data.phone_number
        val dieselPreference = data.getDieselPreferences()
        val dieselCompany = data.supplier_details?.dieselCompany?: mutableListOf()
        if (uuid.isNotNullOrEmpty() && number.isNotNullOrEmpty()) {
          editTeamMember(uuid, name!!, number!!, dieselPreference, dieselCompany )
        }
      }

      DeleteTeamMemberAction_Delete -> {
        val data = item.data as UserModel
        val uuid = data.userId
        if (uuid.isNotNullOrEmpty()) {
          confirmAndDelete(uuid)
        }
      }

      ViewAdminMember -> {
        AdminMemberDialog(this, item.data as UserModel , viewModel).show()
      }
    }
  }

  override fun handleAction(
    actionId: String,
    item: BaseTeamMembersRVAdapterItem<*>,
    position: Int
  ) {
    TODO("Not yet implemented")
  }

  /**
   * Confirm and delete team member
   */
  fun confirmAndDelete(uuid: String) {
    dialogUtils.showBasicConfirmDialog(
        R.string.title_dialog_delete,
        R.string.msg_dialog_delete,
        positiveAction = "Confirm",
        negativeAction = "Cancel",
        positiveClickListener = {
          it.dismiss()
          deleteTeamMember(uuid)
        }
    )
  }

  /**
   * Create team member
   */
  fun createTeamMember() {
    TeamMembersCreateDialog(this, viewModel).show()
  }

  /**
   * Edit team member
   */
  fun editTeamMember(uuid: String, name: String, number: String, dieselPreference: Boolean, dieselCompany :List<String>) {
    TeamMembersEditDialog(this, uuid, name, number, dieselPreference, dieselCompany, viewModel).show()
  }

  /**
   * Delete team member
   */
  fun deleteTeamMember(uuid: String) {
    viewModel.deleteMember(uuid)
  }

  /**
   * Progress observer
   */
  inner class ProgressObserver : Observer<Boolean> {
    override fun onChanged(t: Boolean?) {
      t?.let {
        when (t) {
          true -> uiUtils.showProgress()
          false -> uiUtils.hideProgress()
        }
      }
    }
  }

}

fun teamMembersIntent(
  context: Context,
  createUserIntent:Boolean = false

): Intent = Intent(context, TeamMembersActivity::class.java).apply {
    putExtra(USER_CREATE,createUserIntent)

}


const val USER_CREATE = "user_create"