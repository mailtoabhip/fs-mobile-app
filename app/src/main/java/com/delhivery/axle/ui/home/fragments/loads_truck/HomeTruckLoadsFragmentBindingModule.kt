package com.delhivery.axle.ui.home.fragments.loads_truck

import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsFragment
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class HomeTruckLoadsFragmentBindingModule {

    @ContributesAndroidInjector
    internal abstract fun provideTruckFragment(): HomeTrucksFragment

    @ContributesAndroidInjector
    internal abstract fun provideContractsFragment(): HomeContractsFragment

}