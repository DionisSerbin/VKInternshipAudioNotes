package com.alterpat.voicerecorder.db

import androidx.room.*
import com.example.vkinternshipaudionotes.domain.DataBaseUseCase

@Dao
interface AudioRecordRepository : DataBaseUseCase {
    @Query("SELECT * FROM audioRecords")
    override fun getAll(): List<AudioRecord>

    @Query("SELECT * FROM audioRecords WHERE filename LIKE :searchQuery")
    override fun searchDatabase(searchQuery: String): List<AudioRecord>

    @Query("DELETE FROM audioRecords")
    override fun deleteAll()

    @Insert
    override fun insert(vararg audioRecord: AudioRecord)

    @Delete
    override fun delete(audioRecord: AudioRecord)

    @Delete
    override fun delete(audioRecords: List<AudioRecord>)

    @Update
    override fun update(audioRecord: AudioRecord)
}