package com.delhivery.axle.ui.onboarding

import com.delhivery.axle.api.repository.AuthenticationRepository
import com.delhivery.axle.api.repository.NotificationRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.prefs.GlobalPrefs
import javax.inject.Inject

class BasicDetailsViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val notificationRepository: NotificationRepository,
    private val globalPrefs: GlobalPrefs
) : BaseViewModel() {
}