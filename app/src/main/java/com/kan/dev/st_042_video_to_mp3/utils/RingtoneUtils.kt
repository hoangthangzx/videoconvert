package com.kan.dev.st_042_video_to_mp3.utils

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.CustomDialogRingtoneBinding

object RingtoneUtils {
    fun showRingtoneDialog(context: Context, ringtoneUri: Uri, onRingtoneSelected: (Int) -> Unit) {
        SystemUtils.setLocale(context)
        val dialogBinding = CustomDialogRingtoneBinding.inflate(LayoutInflater.from(context))
        val dialog = Dialog(context).apply {
            window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawable(context.getDrawable(R.color.transparent))
            setContentView(dialogBinding.root)
            setCancelable(false)
        }

        var position = 0
        dialogBinding.rdGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            for (i in 0 until radioGroup.childCount) {
                val radioButton = radioGroup.getChildAt(i) as? RadioButton
                if (radioButton?.id == checkedId) {
                    position = i
                    break
                }
            }
        }

        dialogBinding.lnSave.onSingleClick {
            onRingtoneSelected(position)
            dialog.dismiss()
        }

        dialogBinding.lnCancel.onSingleClick {
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun handleWriteSettingsPermission(context: Context, type: Int, ringtoneUri: Uri) {
        val permission: Boolean = Settings.System.canWrite(context)
        if (permission) {
            setRingtone(context, type, ringtoneUri)
        } else {
            openAndroidPermissionsMenu(context)
        }
    }

    private fun openAndroidPermissionsMenu(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
    }

    private fun setRingtone(context: Context, type: Int, ringtoneUri: Uri) {
        when (type) {
            0 -> RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, ringtoneUri)
            1 -> RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, ringtoneUri)
            2 -> RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, ringtoneUri)
            else -> {}
        }
        Toast.makeText(context, R.string.successful, Toast.LENGTH_SHORT).show()
    }
}