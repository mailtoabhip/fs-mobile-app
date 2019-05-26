package com.delhivery.orion.ui.home.fragments

import com.delhivery.orion.ui.home.fragments.alerts.HomeAlertsFragment
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsFragment
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsFragment
import com.delhivery.orion.ui.home.fragments.payment.HomePaymentFragment
import com.delhivery.orion.ui.home.fragments.profile.HomeProfileFragment
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsFragment
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
}