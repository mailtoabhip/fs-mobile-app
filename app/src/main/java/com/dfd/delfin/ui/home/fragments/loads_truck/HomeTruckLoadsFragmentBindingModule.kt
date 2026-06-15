package com.dfd.delfin.ui.home.fragments.loads_truck

import com.dfd.delfin.ui.home.fragments.contracts.HomeContractsFragment
import com.dfd.delfin.ui.home.fragments.trucks.HomeTrucksFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class HomeTruckLoadsFragmentBindingModule {

    @ContributesAndroidInjector
    internal abstract fun provideTruckFragment(): HomeTrucksFragment

    @ContributesAndroidInjector
    internal abstract fun provideContractsFragment(): HomeContractsFragment

}