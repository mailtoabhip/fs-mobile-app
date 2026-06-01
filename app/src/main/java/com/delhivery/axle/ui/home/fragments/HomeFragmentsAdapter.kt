package com.delhivery.axle.ui.home.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * Home view pager fragments adapter
 */
class HomeFragmentsAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(
    fragmentManager
) {
  override fun getItem(position: Int) = HomeFragmentType.pos(position)!!.fragment

  override fun getCount() = HomeFragmentType.count()

  override fun getPageTitle(position: Int) = HomeFragmentType.pos(position)!!.title
}


class HomePlacementsFragmentsAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(
    fragmentManager
){
    override fun getCount() = HomePlacementsFragmentType.count()

    override fun getItem(position: Int) = HomePlacementsFragmentType.pos(position)!!.fragment

    override fun getPageTitle(position: Int) = HomePlacementsFragmentType.pos(position)!!.title
}