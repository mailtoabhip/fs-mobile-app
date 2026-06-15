package com.dfd.delfin.ui.profile.kycdetails.fragments

import com.dfd.delfin.api.repository.LoadboardRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.ui.base.BaseViewModel
import javax.inject.Inject

class YourKYCDetailsViewModel @Inject constructor(
        private val userRepository: UserRepository,
        private val loadboardRepository: LoadboardRepository
) : BaseViewModel(){
}