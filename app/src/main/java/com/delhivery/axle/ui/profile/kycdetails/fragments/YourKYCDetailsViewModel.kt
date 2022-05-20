package com.delhivery.axle.ui.profile.kycdetails.fragments

import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import java.io.File
import javax.inject.Inject

class YourKYCDetailsViewModel @Inject constructor(
        private val userRepository: UserRepository,
        private val loadboardRepository: LoadboardRepository
) : BaseViewModel(){
}