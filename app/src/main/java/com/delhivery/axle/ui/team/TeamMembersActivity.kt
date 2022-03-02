package com.delhivery.axle.ui.team

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.delhivery.axle.R
import com.delhivery.axle.api.request.DeleteTeamMemberAction_Delete
import com.delhivery.axle.api.request.EditTeamMemberAction_Edit
import com.delhivery.axle.api.request.TeamMemberAction_options
import com.delhivery.axle.api.request.ViewAdminMember
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.data.teammembers.WarningAction_NoMembers
import com.delhivery.axle.databinding.ActivityTeamMembersBinding
import com.delhivery.axle.databinding.DialogTeamMemberBottomOptionsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.TeamMemberInterface
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import java.io.Serializable

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 30/12/20
 */

class TeamMembersActivity : BaseActivity<ActivityTeamMembersBinding, TeamMembersViewModel>(),
  TeamMembersRVAdapterInterface, TeamMemberInterface {


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
    title = "My Team Members"
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

    binding.addTeamMemberButton.setOnClickListener{
      createTeamMember()
    }

    viewModel.progressLiveData.observe(this, ProgressObserver())

    refreshData()

    viewModel.emptyUserLiveData.observe(this, Observer {
      if(it){
        binding.addTeamMemberButton.visibility = View.GONE
      }else{
        binding.addTeamMemberButton.visibility = View.VISIBLE
      }
    })
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
        val name = data.userName
        val number = data.phoneNumber
        val dieselPreference = data.getDieselPreferences()
        val dieselCompany = data.supplierDetails?.dieselCompany?: mutableListOf()
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

      WarningAction_NoMembers ->{
        createTeamMember()
      }
    }
  }

  override fun handleAction(
    actionId: String,
    item: BaseTeamMembersRVAdapterItem<*>,
    position: Int
  ) {
    when (actionId) {
      TeamMemberAction_options -> {
        showOptionsDialog(item.data as UserModel, position)
      }
     }
  }

  private fun showOptionsDialog(data: UserModel , position: Int){
    val dialog = Dialog(this)
    val bindingDialog= DialogTeamMemberBottomOptionsBinding.inflate(layoutInflater)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(bindingDialog.root)

    bindingDialog.closeBtn.setOnClickListener{
      dialog.dismiss()
    }
    bindingDialog.editMemberLayout.setOnClickListener {
      val uuid = data.userId
      val name = data.userName
      val number = data.phoneNumber
      val dieselPreference = data.getDieselPreferences()
      val dieselCompany = data.dieselCompany?: mutableListOf()
      if (uuid.isNotNullOrEmpty() && number.isNotNullOrEmpty()) {
        if (name != null) {
          editTeamMember(uuid, name, number!!, dieselPreference, dieselCompany )
        }
        dialog.dismiss()
      }
    }
    bindingDialog.deleteMemberLayout.setOnClickListener {
      val uuid = data.userId
      if (uuid.isNotNullOrEmpty()) {
        confirmAndDelete(uuid)
        dialog.dismiss()
      }
    }

    dialog.show()
    dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
    dialog.window!!.setGravity(Gravity.BOTTOM)
  }

  /**
   * Confirm and delete team member
   */
  fun confirmAndDelete(uuid: String) {
    dialogUtils.showTeamDeleteDialog(uuid,this)
  }

  /**
   * Create team member
   */
  fun createTeamMember() {
    TeamMembersCreateDialog(this, viewModel, viewModel.userPrefs).show()
  }

  /**
   * Edit team member
   */
  fun editTeamMember(uuid: String, name: String, number: String, dieselPreference: Boolean, dieselCompany :List<String>) {
    TeamMembersEditDialog(this, uuid, name, number, dieselPreference, dieselCompany, viewModel, viewModel.userPrefs).show()
  }

  /**
   * Delete team member
   */
  override fun deleteTeamMember(uuid: String) {
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