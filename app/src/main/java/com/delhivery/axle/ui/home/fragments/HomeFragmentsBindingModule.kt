package com.delhivery.axle.ui.home.fragments

import com.delhivery.axle.ui.home.fragments.alerts.HomeAlertsFragment
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsFragment
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFragment
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckFragment
import com.delhivery.axle.ui.home.fragments.placements.HomePlacementsFragment
import com.delhivery.axle.ui.home.fragments.placements.HomePlacementsDelayedFragment
import com.delhivery.axle.ui.home.fragments.placements.HomePlacementsExpectedFragment
import com.delhivery.axle.ui.home.fragments.pod.HomePodsFragment
import com.delhivery.axle.ui.profile.HomeProfileFragment
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsFragment
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksFragment
import com.delhivery.axle.ui.home.fragments.wallet.HomeWalletFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class HomeFragmentsBindingModule {
  @ContributesAndroidInjector
  internal abstract fun provideBidsFragment(): HomeBidsFragment

  @ContributesAndroidInjector
  internal abstract fun provideTripsFragment(): HomeTripsFragment

  @ContributesAndroidInjector
  internal abstract fun provideAlertsFragment(): HomeAlertsFragment

  @ContributesAndroidInjector
  internal abstract fun provideProfileFragment(): HomeProfileFragment

  @ContributesAndroidInjector
  internal abstract fun provideWalletFragment(): HomeWalletFragment

  @ContributesAndroidInjector
  internal abstract fun providePodFragment(): HomePodsFragment

  @ContributesAndroidInjector
  internal abstract fun provideLoadsTruckFragment(): HomeLoadsTruckFragment

  @ContributesAndroidInjector
  internal abstract fun providePlacementsFragment(): HomePlacementsFragment

  @ContributesAndroidInjector
  internal abstract fun providePlacementsDelayedFragment(): HomePlacementsDelayedFragment

  @ContributesAndroidInjector
  internal abstract fun providePlacementsExpectedFragment(): HomePlacementsExpectedFragment


}