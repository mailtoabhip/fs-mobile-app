package com.dfd.delfin.ui.profile

import com.dfd.delfin.ui.home.fragments.placements.HomePlacementsDelayedFragment
import com.dfd.delfin.ui.home.fragments.placements.HomePlacementsExpectedFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PlacementsFragmentsBindingModule {
    @ContributesAndroidInjector
    internal abstract fun providePlacementsDelayedFragment(): HomePlacementsDelayedFragment

    @ContributesAndroidInjector
    internal abstract fun providePlacementsExpectedFragment(): HomePlacementsExpectedFragment
}
