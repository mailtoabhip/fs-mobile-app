package com.delhivery.axle.ui.fastag.issuance

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuyFasTagFragmentsBindingModule {
    @ContributesAndroidInjector
    internal abstract fun provideSalesCodeFragment(): SalesCodeFragment

    @ContributesAndroidInjector
    internal abstract fun provideAgentConfirmationFragment(): AgentConfirmationFragment
}
