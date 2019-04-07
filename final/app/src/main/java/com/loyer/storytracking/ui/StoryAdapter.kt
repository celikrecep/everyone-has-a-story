package com.loyer.storytracking.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * Created by celikrecep on 6.04.2019.
 */
class StoryAdapter (manager: FragmentManager): FragmentStatePagerAdapter(manager) {

    private val list = mutableListOf<Fragment>()

    override fun getItem(position: Int): Fragment = list[position]

    override fun getCount(): Int = list.size

    fun addFragment(fr: Fragment) {
        list.add(fr)
        notifyDataSetChanged()
    }
}