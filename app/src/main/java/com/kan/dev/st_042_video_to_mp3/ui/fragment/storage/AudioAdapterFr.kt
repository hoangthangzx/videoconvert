package com.kan.dev.st_042_video_to_mp3.ui.fragment.storage

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ItemAudioBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.ui.select_audio.SelectAudioAdapter
import com.kan.dev.st_042_video_to_mp3.utils.Const

class AudioAdapterFr (var context: Context): RecyclerView.Adapter<AudioAdapterFr.ViewHolder>() {
    lateinit var mListener : onClickItemListener
    var isLongPress = false
    var data = listOf<AudioInfo>()
    fun getData(mData : List<AudioInfo>){
        data = mData
        notifyDataSetChanged()
    }

    interface onClickItemListener {
        fun onClickItem (position: Int, holder: ViewHolder)
        fun onTouchEven(position: Int)
    }

    fun onClickListener(onClickItemListener: onClickItemListener){
        mListener = onClickItemListener
    }

    inner class ViewHolder(val binding : ItemAudioBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("ClickableViewAccessibility")
        fun bind(position: Int, holder: AudioAdapterFr.ViewHolder) {
            binding.tvTitle.isSelected = true
            binding.tvSize.text = "${data[position].sizeInMB} KB"
            binding.tvTitle.text = data[position].name
            binding.tvTime.text = data[position].date
            binding.tvDurationVideo.text = data[position].duration
            binding.tvTitle.isSelected = true
            if(Const.checkType == false){
                holder.binding.imvTick.visibility = View.VISIBLE
            }else{
                holder.binding.imvTick.visibility = View.GONE
            }

            if(!data[position].active ){
                binding.imvTick.setImageResource(R.drawable.icon_check_box)
            }else{
                binding.imvTick.setImageResource(R.drawable.icon_check_box_yes)
            }
            binding.imvTick.setOnClickListener {
                mListener.onClickItem(position,holder)
            }
            binding.root.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isLongPress = false
                        holder.binding.root.isPressed = true

                        holder.binding.root.postDelayed({
                            if (holder.binding.root.isPressed) {
                                isLongPress = true
                                mListener.onTouchEven(position)
                            }
                        }, 1000)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        holder.binding.root.isPressed = false
                        holder.binding.root.removeCallbacks(null) // Hủy bỏ mọi callback
                        if (!isLongPress) {
                            mListener.onClickItem(position, holder) // Gọi sự kiện click
                        }
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        holder.binding.root.isPressed = false
                        holder.binding.root.removeCallbacks(null) // Hủy bỏ mọi callback
                        true
                    }
                    else -> false
                }
            }

            holder.itemView.setOnClickListener {
                if (!isLongPress) { // Kiểm tra nếu không phải long press
                    mListener.onClickItem(position, holder)
                }
            }

        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AudioAdapterFr.ViewHolder {
        val binding = ItemAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioAdapterFr.ViewHolder, position: Int) {
        holder.bind(position,holder)
    }

    override fun getItemCount(): Int = data.size
}