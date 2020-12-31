package com.delhivery.axle.ui.team

import android.os.Bundle
import android.view.View
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityTeamMembersBinding
import com.delhivery.axle.ui.base.BaseActivity

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 30/12/20
 */

class TeamMembersActivity : BaseActivity<ActivityTeamMembersBinding, TeamMembersViewModel>(),
  TeamMembersRVAdapterInterface {

  init {
    hasInlineProgress = true
  }

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
      refreshData()
    }

    if (viewModel.userPrefs.isParent) {
      binding.layAddMember.visibility = View.VISIBLE
    } else {
      binding.layAddMember.visibility = View.GONE
    }

    binding.fabAddMember.setOnClickListener {

    }

    refreshData()
  }

  private fun refreshData() {
    viewModel.fetchTeamMembers()
    binding.executePendingBindings()
  }

  override fun handleAction(
    actionId: String,
    item: BaseTeamMembersRVAdapterItem<*>
  ) {
    TODO("Not yet implemented")
  }

  override fun handleAction(
    actionId: String,
    item: BaseTeamMembersRVAdapterItem<*>,
    position: Int
  ) {
    TODO("Not yet implemented")
  }

}