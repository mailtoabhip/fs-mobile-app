package com.delhivery.axle.ui.team

import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 28/12/20
 */

class TeamMembersViewModel @Inject constructor(
  private val userRepository: UserRepository,
  val userPrefs: UserPrefs
) : BaseViewModel() {

  /**
   * Fetch team members of logged in user
   */
  fun fetchTeamMembers() {

    compositeDisposable += userRepository.getUserTeamMembers(0, 100, true, userRepository.userId())
        .onBackground()
        .progress()
        .subscribe { _res, error ->
          if (!error && _res != null) {
            //TODO: put data into recyclerview adapter
          } else {
            error.handle()
          }
        }
  }
}