package com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kan.dev.st_042_video_to_mp3.databinding.ItemConvertFileBinding
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.selectType
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick

class FileConvertAdapter( var context: Context) : RecyclerView.Adapter<FileConvertAdapter.ViewHolder>() {
    lateinit var mListener : onClickItemListener
    var data = listOf<VideoInfo>()
    fun getData(mData: List<VideoInfo>){
        data = mData
        notifyDataSetChanged()
    }

    interface onClickItemListener {
        fun onItemClick(position: Int)
    }

    fun onClickListener (onClickItemListener: onClickItemListener){
        mListener = onClickItemListener
    }

    inner class ViewHolder (val binding: ItemConvertFileBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(position: Int, holder: ViewHolder, mListener: onClickItemListener) {

            if(selectType.equals("VideoConvert")){
                var ex = getSupportedVideoExtension(data[position].uri, context.contentResolver)
                binding.tvSize.text = ex!!.toUpperCase()
                binding.tvDurationVideo.text = "${data[position].sizeInMB} MB"
            }else{
                binding.tvSize.text = "${data[position].sizeInMB} MB"
                binding.tvDurationVideo.text = data[position].duration
            }

            binding.tvTitle.text = data[position].name
            binding.tvTitle.isSelected = true
            Glide.with(context)
                .asBitmap()
                .load(data[position].uri)
                .into(binding.imvVideoFile)

            if(Const.selectType.equals("Video") || selectType.equals("VideoConvert")){
                binding.imvTickBox.visibility = View.GONE
            }


            binding.imvDelete.onSingleClick {
                mListener.onItemClick(position)
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FileConvertAdapter.ViewHolder {
        val binding = ItemConvertFileBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileConvertAdapter.ViewHolder, position: Int) {
        holder.bind(position,holder,mListener)
    }

    override fun getItemCount(): Int = data.size

    fun getSupportedVideoExtension(uri: Uri, contentResolver: ContentResolver): String? {
        // Lấy loại MIME của tệp từ ContentResolver
        val mimeType = contentResolver.getType(uri)

        // Nếu loại MIME không null, lấy phần mở rộng
        val extension = mimeType?.let {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(it)
        }

        // Kiểm tra xem phần mở rộng có phải là một trong những loại hỗ trợ không
        return when (extension) {
            "mp4", "avi", "mkv", "mov", "wmv" -> extension
            else -> null // Trả về null nếu không phải là loại hỗ trợ
        }
    }
}