package com.delhivery.axle.ui.profile.raterewards.fragments

import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.YourRewardsFragment
import com.delhivery.axle.ui.profile.raterewards.fragments.sharerate.ShareRateFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ShareRateGetRewardsFragmentBindingModule {
    @ContributesAndroidInjector
    internal abstract fun provideShareRateFragment(): ShareRateFragment

    @ContributesAndroidInjector
    internal abstract fun provideYourRewardsFragment(): YourRewardsFragment
}