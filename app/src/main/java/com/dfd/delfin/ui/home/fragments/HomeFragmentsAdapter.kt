package com.dfd.delfin.ui.home.fragments

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * Home view pager fragments adapter
 */
class HomeFragmentsAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(
    fragmentManager
) {
  override fun getItem(position: Int) = HomeFragmentType.NewHomeFragment.fragment

  override fun getCount() = 1  // Only show HomeFragment

  override fun getPageTitle(position: Int) = HomeFragmentType.NewHomeFragment.title
}


class HomePlacementsFragmentsAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(
    fragmentManager
){
    override fun getCount() = HomePlacementsFragmentType.count()

    override fun getItem(position: Int) = HomePlacementsFragmentType.pos(position)!!.fragment

    override fun getPageTitle(position: Int) = HomePlacementsFragmentType.pos(position)!!.title
}