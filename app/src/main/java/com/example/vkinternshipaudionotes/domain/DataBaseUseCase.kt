package com.example.vkinternshipaudionotes.domain

import com.alterpat.voicerecorder.db.AudioRecord

interface DataBaseUseCase {
    fun getAll(): List<AudioRecord>

    fun searchDatabase(searchQuery: String): List<AudioRecord>

    fun deleteAll()

    fun insert(vararg audioRecord: AudioRecord)

    fun delete(audioRecord: AudioRecord)

    fun delete(audioRecords: List<AudioRecord>)

    fun update(audioRecord: AudioRecord)
}