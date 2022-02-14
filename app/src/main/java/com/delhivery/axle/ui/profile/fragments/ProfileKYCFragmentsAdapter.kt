package com.delhivery.axle.ui.profile.fragments

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ProfileKYCFragmentsAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(
    fragmentManager
) {
    override fun getItem(position: Int) =ProfileKYCFragmentType.pos(position)!!.fragment

    override fun getCount() = ProfileKYCFragmentType.count()

    override fun getPageTitle(position: Int) = ProfileKYCFragmentType.pos(position)!!.title
}