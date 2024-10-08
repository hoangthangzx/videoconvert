package com.kan.dev.st_042_video_to_mp3.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // Sử dụng MutableLiveData để lưu trữ sự kiện
    private val _eventTriggered = MutableLiveData<Boolean>()
    val eventTriggered: LiveData<Boolean> get() = _eventTriggered

    // Phương thức để kích hoạt sự kiện
    fun triggerEvent() {
        _eventTriggered.value = true
    }

    fun resetEvent() {
        _eventTriggered.value = false
    }
}