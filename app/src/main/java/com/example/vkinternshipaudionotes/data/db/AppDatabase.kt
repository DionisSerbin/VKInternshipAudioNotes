package com.example.vkinternshipaudionotes.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alterpat.voicerecorder.db.AudioRecord
import com.alterpat.voicerecorder.db.AudioRecordRepository

@Database(entities = [AudioRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioRecordDAO(): AudioRecordRepository
}