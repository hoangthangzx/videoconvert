package com.kan.dev.st_042_video_to_mp3.ui.intro

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.kan.dev.st_042_video_to_mp3.databinding.ItemIntroBinding
import com.metaldetector.golddetector.finder.model.TutorialModel



class ViewPagerAdapter() : RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {
    var data = arrayListOf<TutorialModel>()
    fun getData (mData : List<TutorialModel>){
        data = mData as ArrayList<TutorialModel>
    }
    inner class ViewHolder(var binding: ItemIntroBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SuspiciousIndentation")
        fun bind(position: Int){
            binding.imvIntro.setImageResource(data[position].bg)
            val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(0, 0, 0, 0)
                binding.imvIntro.setLayoutParams(lp)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemIntroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}