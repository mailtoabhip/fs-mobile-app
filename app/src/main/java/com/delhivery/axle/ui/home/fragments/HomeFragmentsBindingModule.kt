package com.delhivery.axle.ui.home.fragments

import com.delhivery.axle.ui.home.fragments.alerts.HomeAlertsFragment
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsFragment
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFragment
import com.delhivery.axle.ui.home.fragments.payment.HomePaymentFragment
import com.delhivery.axle.ui.home.fragments.profile.HomeProfileFragment
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsFragment
import com.delhivery.axle.ui.home.fragments.wallet.HomeWalletFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class HomeFragmentsBindingModule {
  @ContributesAndroidInjector
  internal abstract fun provideBidsFragment(): HomeBidsFragment

  @ContributesAndroidInjector
  internal abstract fun provideLoadsFragment(): HomeLoadsFragment

  @ContributesAndroidInjector
  internal abstract fun provideTripsFragment(): HomeTripsFragment

  @ContributesAndroidInjector
  internal abstract fun providePaymentFragment(): HomePaymentFragment

  @ContributesAndroidInjector
  internal abstract fun provideAlertsFragment(): HomeAlertsFragment

  @ContributesAndroidInjector
  internal abstract fun provideProfileFragment(): HomeProfileFragment

  @ContributesAndroidInjector
  internal abstract fun provideWalletFragment(): HomeWalletFragment
}