package com.kan.dev.st_042_video_to_mp3.ui

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ActivityPermissionBinding
import com.kan.dev.st_042_video_to_mp3.databinding.CustomDialogPermissionBinding
import com.kan.dev.st_042_video_to_mp3.utils.Const
import com.kan.dev.st_042_video_to_mp3.utils.Const.REQUEST_CODE_NOTIFICATION_POLICY
import com.kan.dev.st_042_video_to_mp3.utils.Const.STORAGE_PERMISSION_CODE
import com.kan.dev.st_042_video_to_mp3.utils.SystemUtils
import com.kan.dev.st_042_video_to_mp3.utils.applyGradient
import com.kan.dev.st_042_video_to_mp3.utils.applyGradientWidth
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import com.metaldetector.golddetector.finder.AbsBaseActivity
import com.metaldetector.golddetector.finder.SharedPreferenceUtils

class PermissionActivity : AbsBaseActivity<ActivityPermissionBinding>(false) {
 override fun getLayoutId(): Int = R.layout.activity_permission
    lateinit var providerSharedPreference: SharedPreferenceUtils
    private var isFirstTimeRequestCamera = true
    private var isFirstTimeRequestNoty = true
    var firstPick :String? = null
    override fun init() {
        initView()
        initData()
        initAction()
    }
    private fun initView() {
        providerSharedPreference = SharedPreferenceUtils.getInstance(this@PermissionActivity)
        firstPick = providerSharedPreference.getStringValue("firstPick")
        Log.d("check_firstPick", "initView: "+ firstPick)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.lnNotification.visibility = View.VISIBLE
        } else {
            binding.lnNotification.visibility = View.GONE
        }
        val colors = intArrayOf(
            ContextCompat.getColor(this@PermissionActivity, R.color.color_1),
            ContextCompat.getColor(this@PermissionActivity, R.color.color_2)
        )
        binding.tvContinue.applyGradientWidth(this@PermissionActivity,colors)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    private fun initData() {
        val sharedPref = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("per_state", "true")
        editor.apply()
    }
    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
           val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
            val notyPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            return  (notyPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED )
        }else{
            val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            val notyPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            return  (notyPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED)
        }
//        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
    }
    private fun initAction() {
        binding.tvContinue.onSingleClick {
            if(checkPermissions()){
//                SharedPreferenceUtils.getInstance(this).putBooleanValue(Const.PERMISSION,true)
                SharedPreferenceUtils.getInstance(this).putStringValue("firstPick","false")
                startActivity(Intent(this@PermissionActivity, MainActivity::class.java))
            }
            else{
                Toast.makeText(this, getString(R.string.you_must_grant_permission), Toast.LENGTH_SHORT).show()
            }
        }

        binding.lnNotification.onSingleClick {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                updateNotyPermissionStatus()
                binding.lnNotification.isClickable = false
            } else {
                requestNotificationPolicyPermission()
            }
        }

        binding.lnStorage.onSingleClick {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                Log.d("check_per", "initAction: 12...")
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) {
                    updateStoragePermissionStatus()
                    binding.lnStorage.isClickable = false
                } else {
                    requestStoragePermission()
                }
            }else{
                Log.d("check_per", "initAction: 9...")
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    updateStoragePermissionStatus()
                    binding.lnStorage.isClickable = false
                } else {
                    requestStoragePermission()
                }
            }
        }
    }

    private fun requestNotificationPolicyPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            updateNotyPermissionStatus()
        } else {
            if (isFirstTimeRequestNoty) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATION_POLICY)
                isFirstTimeRequestNoty = false
            } else {
                showSettingsDialog()
            }
        }
    }
    private fun updateNotyPermissionStatus() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            binding.imvSwitchOffMedia.visibility = View.GONE
            binding.imvSwitchOnMedia.visibility = View.VISIBLE
        }else{
            binding.imvSwitchOffMedia.visibility = View.VISIBLE
            binding.imvSwitchOnMedia.visibility = View.GONE
        }
    }

    private fun updateStoragePermissionStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED)  {
                binding.imvSwitchOffStorage.visibility = View.GONE
                binding.imvSwitchOnStorage.visibility = View.VISIBLE
            }else{
                binding.imvSwitchOffStorage.visibility = View.VISIBLE
                binding.imvSwitchOnStorage.visibility = View.GONE
            }
        }else{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)  {
                binding.imvSwitchOffStorage.visibility = View.GONE
                binding.imvSwitchOnStorage.visibility = View.VISIBLE
            }else{
                binding.imvSwitchOffStorage.visibility = View.VISIBLE
                binding.imvSwitchOnStorage.visibility = View.GONE
            }
        }

    }
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                updateStoragePermissionStatus()
            } else {
                if (isFirstTimeRequestCamera) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO), STORAGE_PERMISSION_CODE)
                    isFirstTimeRequestCamera = false
                } else {
                    showSettingsDialog()
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                updateStoragePermissionStatus()
            } else {
                if (isFirstTimeRequestCamera) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
                    isFirstTimeRequestCamera = false
                } else {
                    showSettingsDialog()
                }
            }
        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)  {
//            updateRecoedStoragePermissionStatus()
//        } else {
//            if (isFirstTimeRequestCamera) {
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
//                isFirstTimeRequestCamera = false
//            } else {
//                showSettingsDialog()
//            }
//        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_NOTIFICATION_POLICY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateNotyPermissionStatus()
                } else {
                    showSettingsDialog()
                }
            }
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateStoragePermissionStatus()
                } else {
                    showSettingsDialog()
                }
            }
        }
    }
    private fun showSettingsDialog() {
        SystemUtils.setLocale(this)
        val dialogBinding = CustomDialogPermissionBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(getDrawable(R.color.transparent))
        dialog.setContentView(dialogBinding.root)
        dialog.show()

        dialogBinding.tvYes.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
            dialog.dismiss()
        }
        dialogBinding.tvNo.setOnClickListener {
            dialog.dismiss() // Đóng dialog
        }
    }
    override fun onResume() {
        super.onResume()
        updateStoragePermissionStatus()
        updateNotyPermissionStatus()
    }

    override fun getFragmentID(): Int  = 0

}