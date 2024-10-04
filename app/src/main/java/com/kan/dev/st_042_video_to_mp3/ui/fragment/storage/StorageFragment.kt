package com.kan.dev.st_042_video_to_mp3.ui.fragment.storage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.FragmentStorageBinding


class StorageFragment : Fragment() {
    lateinit var binding : FragmentStorageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStorageBinding.inflate(layoutInflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPager.adapter = StorageViewPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager){ tab, position ->
            when (position) {
                0 -> tab.text = "Video"
                1 -> tab.text = "Audio"
            }
        }.attach()
    }

}