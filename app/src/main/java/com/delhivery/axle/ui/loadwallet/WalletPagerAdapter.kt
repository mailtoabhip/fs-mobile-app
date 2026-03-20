package com.delhivery.axle.ui.loadwallet

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * ViewPager adapter for wallet history tabs
 */
class WalletPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(
    fragmentManager
) {
    override fun getCount() = WalletTabType.count()

    override fun getItem(position: Int) = WalletTabType.pos(position)!!.fragment

    override fun getPageTitle(position: Int) = WalletTabType.pos(position)!!.title
}

/**
 * Wallet tab types
 */
enum class WalletTabType(
    val position: Int,
    val fragment: com.delhivery.axle.ui.base.BaseFragment<*, *>,
    val title: String
) {
    Transactions(0, WalletTransactionsFragment.newInstance(), "Transactions"),
    Recharges(1, WalletRechargesFragment.newInstance(), "Recharges");

    companion object {
        fun pos(position: Int) = values().firstOrNull { it.position == position }
        fun count() = values().size
    }
}
