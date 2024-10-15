package com.kan.dev.st_042_video_to_mp3.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityPlayVideoBinding
import com.kan.dev.st_042_video_to_mp3.databinding.CustomDialogRenameBinding
import com.kan.dev.st_042_video_to_mp3.databinding.CustomDialogRingtoneBinding
import com.kan.dev.st_042_video_to_mp3.databinding.CustomeDialogDeleteBinding
import com.kan.dev.st_042_video_to_mp3.utils.Const.audioInformation
import com.kan.dev.st_042_video_to_mp3.utils.Const.currentRingtone
import com.kan.dev.st_042_video_to_mp3.utils.Const.listAudioStorage
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionAudioPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.uriPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoInfo
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import java.io.File

@UnstableApi
class PlayVideoActivity : AbsBaseActivity<ActivityPlayVideoBinding>(false) {
    override fun getFragmentID(): Int = 0

    override fun getLayoutId(): Int = R.layout.activity_play_video

    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer

    override fun init() {
        initData()
        initView()
        initAction()
    }

    private fun initData() {
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.playerView.player = exoPlayer
        binding.playerControlView.player = exoPlayer
        binding.playerControlView.showTimeoutMs = 3000  // Thiết lập thời gian hiển thị
        val mediaItem = MediaItem.fromUri(uriPlay.toString())
        exoPlayer!!.setMediaItem(mediaItem)
        exoPlayer!!.prepare()
//        exoPlayer!!.play()
        exoPlayer!!.playWhenReady = false
    }

    private fun initView() {
        binding.tvSize.text = "${videoInfo!!.sizeInMB.toString()} MB"
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initAction() {
        binding.imvBack.onSingleClick {
            finish()
        }

//        playerView = findViewById(R.id.player_view)
//        // Khởi tạo ExoPlayer
//        exoPlayer = ExoPlayer.Builder(this).build()
//        playerView.player = exoPlayer // Liên kết PlayerView với ExoPlayer
//
//        // Tạo media item từ URL video
//        val mediaItem = MediaItem.fromUri(videoInfo!!.uri)
//        exoPlayer.setMediaItem(mediaItem)
//        exoPlayer.prepare() // Chuẩn bị phát video
//
//        exoPlayer.playWhenReady = true //

        binding.lnSetas.onSingleClick {
            showDialogRingtone()
        }

        binding.imvTick.onSingleClick {
            binding.lnMenu.visibility = View.GONE
            binding.lnMenu.visibility = View.VISIBLE
        }

        binding.bgTouch.setOnTouchListener { v, event ->
            if (binding.lnMenu.visibility == View.VISIBLE) {
                binding.lnMenu.visibility = View.GONE // Ẩn LinearLayout
                true
            } else {
                false
            }
        }

        binding.playerView.setOnClickListener{
            binding.playerControlView.show()
        }

        binding.playerControlView.setOnClickListener {
            binding.playerControlView.hide()
        }


        binding.lnShare.onSingleClick {
            uriPlay?.let { shareVideoFile(this@PlayVideoActivity, it) }
        }

        binding.lnDelete.onSingleClick {
            showDialogDelete()
        }

        binding.lnRename.onSingleClick {
            showDialogRename()
        }
    }

    private fun showDialogRename() {
        SystemUtils.setLocale(this)
        val dialogBinding  = CustomDialogRenameBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialogBinding.lnOk.onSingleClick{
            dialog.dismiss()
        }
        dialogBinding.lnCancel.onSingleClick {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDialogRingtone() {
        SystemUtils.setLocale(this)
        val dialogBinding  = CustomDialogRingtoneBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawable(getDrawable(R.color.transparent))
        dialog.setContentView(dialogBinding.root)
        var position = 0
        dialog.setCancelable(false)
        dialogBinding.rdGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            for (i in 0 until radioGroup.childCount) {
                val radioButton = radioGroup.getChildAt(i) as? RadioButton
                if (radioButton?.id == checkedId) {
                    position = i
                    break
                }
            }
        }
        dialogBinding.lnSave.onSingleClick{
            when(position){
                0 -> handleWriteSettingsPermission(0)
                1 -> handleWriteSettingsPermission(1)
                2 -> handleWriteSettingsPermission(2)
            }
            currentRingtone = position
            dialog.dismiss()
        }
        dialogBinding.lnCancel.onSingleClick {
            dialog.dismiss()
        }
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun handleWriteSettingsPermission(type: Int) {
        val permission: Boolean = Settings.System.canWrite(this)
        if (permission) {
            setRingtone(type)
        } else {
            openAndroidPermissionsMenu()
        }
    }

    private fun openAndroidPermissionsMenu() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.setData(Uri.parse("package:" + this.packageName))
        startActivity(intent)
    }

    private fun setRingtone(type: Int) {
        val customRingtoneUri = Uri.parse(uriPlay.toString())
        when (type) {
            0       -> {
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, customRingtoneUri)
            }

            1 -> {
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, customRingtoneUri)
            }

            2-> {
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM, customRingtoneUri)
            }

            else                                                   -> {}
        }
        Toast.makeText(this@PlayVideoActivity, R.string.set_ringtone_successfully, Toast.LENGTH_SHORT).show()
    }


    private fun showDialogDelete() {
        SystemUtils.setLocale(this)
        val dialogBinding  = CustomeDialogDeleteBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialogBinding.tvYes.onSingleClick{
            dialog.dismiss()
        }
        dialogBinding.tvNo.onSingleClick {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun shareVideoFile(context : Context, uri: Uri) {
        val filePath = uri.toString().replace("file://", "")
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(
            this,
            "${this.packageName}.provider",  // Package của ứng dụng bạn
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (shareIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(shareIntent, "Share Audio File"))
        }
    }

    override fun onResume() {
        super.onResume()
        initData()
    }


    override fun onStop() {
        super.onStop()
        exoPlayer.release()
    }

}
