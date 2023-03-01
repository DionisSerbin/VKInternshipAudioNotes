package com.example.vkinternshipaudionotes.presentation.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room

import com.alterpat.voicerecorder.db.AudioRecord
import com.example.vkinternshipaudionotes.MainActivity
import com.example.vkinternshipaudionotes.R
import com.example.vkinternshipaudionotes.data.db.AppDatabase
import com.example.vkinternshipaudionotes.presentation.common.BottomSheet
import com.example.vkinternshipaudionotes.data.tools.Timer
import com.example.vkinternshipaudionotes.presentation.customView.RecorderWaveformView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class RecordActivity : AppCompatActivity(), BottomSheet.OnClickListener, Timer.OnTimerUpdateListener {

    private lateinit var fileName: String
    private lateinit var dirPath: String
    private var recorder: MediaRecorder? = null
    private var recording = false
    private var onPause = false
    private var refreshRate : Long = 60
    private lateinit var timer: Timer

    private lateinit var handler: Handler

    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private val recordBtn by lazy {findViewById<ImageButton>(R.id.record_Btn)}
    private val doneBtn by lazy {findViewById<ImageButton>(R.id.doneBtn)}
    private val deleteBtn by lazy {findViewById<ImageButton>(R.id.deleteBtn)}
    private val bacBtn by lazy {findViewById<ImageButton>(R.id.back)}
    private val timerView by lazy {findViewById<TextView>(R.id.timerView)}
    private val playerView by lazy {findViewById<RecorderWaveformView>(R.id.playerView)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        handler = Handler(Looper.myLooper()!!)

        recordBtn.setOnClickListener {

            when {
                onPause -> resumeRecording()
                recording -> pauseRecording()
                else -> startRecording()
            }
        }

        doneBtn.setOnClickListener {
            stopRecording()
            showBottomSheet()
        }

        bacBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        deleteBtn.setOnClickListener {
            stopRecording()

            File(dirPath+fileName).delete()
        }
        deleteBtn.isClickable = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    private fun showBottomSheet(){
        val bottomSheet = BottomSheet(dirPath, fileName, this)
        bottomSheet.show(supportFragmentManager, LOG_TAG)

    }

    private fun startRecording(){

        if(!permissionToRecordAccepted){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
            return
        }

        doneBtn.visibility = View.VISIBLE
        deleteBtn.isClickable = true
        deleteBtn.setImageResource(R.drawable.ic_delete_enabled)

        recording = true
        timer = Timer(this)
        timer.start()

        // format file name with date
        val pattern = "yyyy.MM.dd_hh.mm.ss"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date: String = simpleDateFormat.format(Date())

        dirPath = "${externalCacheDir?.absolutePath}/"
        fileName = "voice_record_${date}.mp3"

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(dirPath+fileName)
            try {
                prepare()
            } catch (e: IOException) {
            }

            start()
        }

        recordBtn.setImageResource(R.drawable.ic_pause)

        animatePlayerView()

    }

    private fun animatePlayerView(){
        if(recording && !onPause){
            val amp = recorder!!.maxAmplitude
            playerView.updateAmps(amp)

            handler.postDelayed(
                Runnable {
                    kotlin.run { animatePlayerView() }
                }, refreshRate
            )
        }
    }

    private fun pauseRecording(){
        onPause = true
        recorder?.apply {
            pause()
        }
        recordBtn.setImageResource(R.drawable.ic_record)
        timer.pause()

    }

    private fun resumeRecording(){
        onPause = false
        recorder?.apply {
            resume()
        }
        recordBtn.setImageResource(R.drawable.ic_pause)
        animatePlayerView()
        timer.start()
    }

    private fun stopRecording(){
        recording = false
        onPause = false
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        recordBtn.setImageResource(R.drawable.ic_record)

        doneBtn.visibility = View.GONE
        deleteBtn.isClickable = false
        deleteBtn.setImageResource(R.drawable.ic_delete_disabled)

        playerView.reset()
        try {
            timer.stop()
        }catch (e: Exception){}

        timerView.text = "00:00.00"
    }

    override fun onCancelClicked() {
        Toast.makeText(this, "Audio record deleted", Toast.LENGTH_SHORT).show()
        stopRecording()
    }

    override fun onOkClicked(filePath: String, filename: String) {
        // add audio record info to database
        val db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords").build()

        val duration = timer.format().split(".")[0]
        stopRecording()

        GlobalScope.launch {
            db.audioRecordDAO().insert(AudioRecord(filename, filePath, Date().time, duration))
        }

    }

    override fun onTimerUpdate(duration: String) {
        runOnUiThread{
            if(recording)
                timerView.text = duration
        }
    }
}