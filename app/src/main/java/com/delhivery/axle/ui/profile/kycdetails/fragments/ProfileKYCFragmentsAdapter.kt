package com.delhivery.axle.ui.profile.kycdetails.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.delhivery.axle.R
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty


class ProfileKYCFragmentsAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(
        fragmentManager
) {
    override fun getItem(position: Int) = ProfileKYCFragmentType.pos(position)!!.fragment

    override fun getCount() = ProfileKYCFragmentType.count()

    override fun getPageTitle(position: Int) = ProfileKYCFragmentType.pos(position)!!.title

    fun getTabView(position: Int, context:Context, issuecount:String?): View? {
       val v: View = LayoutInflater.from(context).inflate(R.layout.kyc_tab, null)
        val tv = v.findViewById(R.id.custom_tab_textView_count) as TextView
        tv.setText(getPageTitle(position))
        val tv2 = v.findViewById(R.id.issues) as TextView

        if(position == 1) {
            if (issuecount.isNotNullOrEmpty()) {
                tv2.setText(issuecount)
                tv2.visibility = View.VISIBLE
            } else {
                tv2.visibility = View.GONE
            }
        }else{
            tv2.visibility = View.GONE
        }
        return v
    }
}