package com.aos.floney.view.mypage.alarm

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AccountPagerAdapter(fragmentActivity: FragmentActivity, private val bookKeys: List<String>) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = bookKeys.size

    override fun createFragment(position: Int): Fragment {
        return MyPageAlarmFragment.newInstance(bookKeys[position])
    }
}