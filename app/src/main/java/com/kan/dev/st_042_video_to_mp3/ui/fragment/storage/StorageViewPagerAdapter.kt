package com.kan.dev.st_042_video_to_mp3.ui.fragment.storage

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class StorageViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment){
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VideoFragment() // Fragment cho Video
            else -> AudioFragment() // Fragment cho Audio
        }
    }
}