package com.dfd.delfin.ui.home.fragments

import com.dfd.delfin.ui.home.fragments.alerts.HomeAlertsFragment
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsFragment
import com.dfd.delfin.ui.home.fragments.home.HomeFragment
import com.dfd.delfin.ui.home.fragments.loads_truck.HomeLoadsTruckFragment
import com.dfd.delfin.ui.home.fragments.pod.HomePodsFragment
import com.dfd.delfin.ui.home.fragments.pod.HomeNewPodFragment
import com.dfd.delfin.ui.home.fragments.pod.PendingPodTabFragment
import com.dfd.delfin.ui.home.fragments.pod.SubmittedPodTabFragment
import com.dfd.delfin.ui.profile.HomeProfileFragment
import com.dfd.delfin.ui.home.fragments.trips.HomeTripsFragment
import com.dfd.delfin.ui.home.fragments.wallet.HomeWalletFragment
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
  internal abstract fun provideHomeNewPodFragment(): HomeNewPodFragment

  @ContributesAndroidInjector
  internal abstract fun providePendingPodTabFragment(): PendingPodTabFragment

  @ContributesAndroidInjector
  internal abstract fun provideSubmittedPodTabFragment(): SubmittedPodTabFragment

  @ContributesAndroidInjector
  internal abstract fun provideHomeFragment(): HomeFragment

}