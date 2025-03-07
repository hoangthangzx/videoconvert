package com.kan.dev.st_042_video_to_mp3.ui.select_audio
import android.content.Context
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ItemAudioBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.clickItem
import com.kan.dev.st_042_video_to_mp3.utils.Const.elementCounts
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick

class SelectAudioAdapter (var context: Context): RecyclerView.Adapter<SelectAudioAdapter.ViewHolder>() {
    lateinit var mListener : onClickItemListener
    lateinit var eListener : onClickItemListenerEdt
    var data = listOf<AudioInfo>()
    fun getData(mData : List<AudioInfo>){
        data = mData
        notifyDataSetChanged()
    }
    interface onClickItemListener {
        fun onClickItem (position: Int, holder: ViewHolder)
        fun onClickPlayAudio (position: Int, holder: ViewHolder)
    }

    interface onClickItemListenerEdt {
        fun onPlusItem (position: Int, holder: ViewHolder)
        fun onMinusItem  (position: Int, holder: ViewHolder)
    }

    fun onClickEdtListener (onClickItemListenerEdt: onClickItemListenerEdt){
        eListener = onClickItemListenerEdt
    }

    fun onClickListener(onClickItemListener: onClickItemListener){
        mListener = onClickItemListener
    }

    inner class ViewHolder(val binding : ItemAudioBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int, holder: SelectAudioAdapter.ViewHolder) {
            if(Const.selectTypeAudio.equals("AudioMerger") ){
                if(elementCounts.size==0 && !data[position].active){
                    binding.tvTime.visibility = View.VISIBLE
                    binding.lnItemCount.visibility = View.INVISIBLE
                }else if(elementCounts.size >= 1 && data[position].active){
                    binding.tvTime.visibility = View.INVISIBLE
                    binding.lnItemCount.visibility = View.VISIBLE
                    elementCounts.forEach {
                        if(data[position].name.equals(it.element)){
                            binding.lnItemCount.visibility = View.VISIBLE
                            binding.tvTime.visibility = View.INVISIBLE
                            binding.edtStartTime.setText(it.count.toString())
                        }
                    }
                }
                if(!data[position].active){
                    binding.tvTime.visibility = View.VISIBLE
                    binding.lnItemCount.visibility = View.GONE
                }else{
                    binding.lnItemCount.visibility = View.VISIBLE
                    binding.tvTime.visibility = View.INVISIBLE
                }
                binding.btnMinus.setOnClickListener {
                    eListener.onMinusItem(position,holder)
                }
                binding.btnPlus.onSingleClick {
                    eListener.onPlusItem(position,holder)
                }
            }

            binding.tvTitle.isSelected = true
            binding.tvSize.text = "${data[position].sizeInMB} KB"
            binding.tvTitle.text = data[position].name
            binding.tvTime.text = data[position].date
            binding.tvDurationVideo.text = data[position].duration
            binding.tvTitle.isSelected = true
            if(!data[position].active ){
                binding.imvTick.setImageResource(R.drawable.icon_check_box)
            }else{
                binding.imvTick.setImageResource(R.drawable.icon_check_box_yes)
            }
            if(!data[position].activePl){
                binding.imvPlayVideo.setImageResource(R.drawable.imv_play_audio)
            }else{
                binding.imvPlayVideo.setImageResource(R.drawable.imv_pause_audio)
            }
            binding.root.setOnClickListener {
                mListener.onClickItem(position,holder)
            }
            holder.binding.lnBtn.onSingleClick {
                mListener.onClickPlayAudio(position,holder)
            }

        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectAudioAdapter.ViewHolder {
        val binding = ItemAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectAudioAdapter.ViewHolder, position: Int) {
        holder.bind(position,holder)
    }

    override fun getItemCount(): Int = data.size
}