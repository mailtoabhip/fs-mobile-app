package com.delhivery.axle.ui.home.activity.fuel

import com.delhivery.axle.repository.WalletRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class FuelCardViewModel @Inject constructor(
  private val walletRepository: WalletRepository,
  private val userPrefs: UserPrefs
) : BaseViewModel()