package com.kan.dev.st_042_video_to_mp3.ui



import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityPlayVideoBinding
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideo
import com.kan.dev.st_042_video_to_mp3.utils.Const.positionVideoPlay
import com.kan.dev.st_042_video_to_mp3.utils.Const.videoInfo
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity

@UnstableApi
class PlayVideoActivity : AbsBaseActivity<ActivityPlayVideoBinding>(false) {
    override fun getFragmentID(): Int = 0

    override fun getLayoutId(): Int = R.layout.activity_play_video

    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer

    override fun init() {
        initAction()
    }

    private fun initAction() {
        binding.imvBack.onSingleClick {
            finish()
        }
        playerView = findViewById(R.id.player_view)
        // Khởi tạo ExoPlayer
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer // Liên kết PlayerView với ExoPlayer

        // Tạo media item từ URL video
        val mediaItem = MediaItem.fromUri(videoInfo!!.uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare() // Chuẩn bị phát video

        exoPlayer.playWhenReady = true //
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.release()
    }

}
