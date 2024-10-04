package com.kan.dev.st_042_video_to_mp3.ui.language

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ItemLanguageBinding
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionLanguageOld
import com.metaldetector.golddetector.finder.model.LanguageModel

class LanguageAdapter(var context : Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onClick:((position : Int)->Unit)? = null
    var data = listOf<LanguageModel>()
    fun getData(mdata: List<LanguageModel>) {
        data = mdata
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var binding =
            ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(position)
            holder.binding.ctlItemLanguage.setOnClickListener {
                onClick!!.invoke(position)
                if (data[0].active) {
                    data[0].active = false
                    notifyItemChanged(0)
                }
                data[positionLanguageOld].active = false
                notifyItemChanged(positionLanguageOld)
                positionLanguageOld = position
                data[position].active = true
                notifyItemChanged(position)
            }
        }
    }
    inner class ViewHolder(val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
//            val textColor = ContextCompat.getColor(context, R.color.white_80)
//            val textColor_2 = ContextCompat.getColor(context, R.color.white)
            if(data[position].active){
                binding.ctlItemLanguage.setBackgroundResource(R.drawable.bg_language)
//                binding.tvLanguage.setTextColor(textColor_2)

            }else{
//                binding.tvLanguage.setTextColor(textColor)
                binding.tvLanguage.setTypeface(null, Typeface.NORMAL)
                binding.ctlItemLanguage.setBackgroundResource(R.drawable.bg_language_8dp)

            }
            binding.tvLanguage.text = data[position].name
            Glide.with(binding.imvIconFlag).load(data[position].icon)
                .into(binding.imvIconFlag)
        }
    }
}