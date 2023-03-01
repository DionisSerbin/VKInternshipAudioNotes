package com.example.vkinternshipaudionotes.presentation.viewmodel

import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.alterpat.voicerecorder.db.AudioRecord
import com.example.vkinternshipaudionotes.data.db.AppDatabase

class MainViewModel: ViewModel() {

    private var _filePath = MutableLiveData<String?>()
    val filePath = _filePath

    private var _fileName = MutableLiveData<String?>()
    val fileName = _fileName

    private var _permissionToRecordAccepted = MutableLiveData<Boolean?>()
    val permissionToRecordAccepted = _permissionToRecordAccepted

    fun savePermissionToRecordAccepted(permissionToRecordAccepted: Boolean) {
        _permissionToRecordAccepted.value = permissionToRecordAccepted
    }

    fun saveFilePath(filePath: String) {
        _filePath.value = filePath
    }

    fun saveFileName(fileName: String) {
        _fileName.value = fileName
    }

    fun getAll(db: AppDatabase): List<AudioRecord> {
        return db.audioRecordDAO().getAll()
    }

    fun insert(db: AppDatabase, audioRecord: AudioRecord) {
        return db.audioRecordDAO().insert(audioRecord)
    }
}