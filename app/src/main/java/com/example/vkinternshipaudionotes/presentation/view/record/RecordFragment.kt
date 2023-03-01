package com.example.vkinternshipaudionotes.presentation.view.record

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.alterpat.voicerecorder.db.AudioRecord
import com.example.vkinternshipaudionotes.R
import com.example.vkinternshipaudionotes.data.db.AppDatabase
import com.example.vkinternshipaudionotes.data.tools.Timer
import com.example.vkinternshipaudionotes.presentation.common.BottomSheet
import com.example.vkinternshipaudionotes.presentation.customView.RecorderWaveformView
import com.example.vkinternshipaudionotes.presentation.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_record.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class RecordFragment : Fragment(), BottomSheet.OnClickListener, Timer.OnTimerUpdateListener {

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var fileName: String
    private lateinit var dirPath: String
    private var recorder: MediaRecorder? = null
    private var recording = false
    private var onPause = false
    private var refreshRate: Long = REFRESH_RATE
    private lateinit var timer: Timer

    private lateinit var handler: Handler
    private lateinit var recordBtn: ImageButton

    private lateinit var timerView: TextView
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recordBtn = view.findViewById(R.id.record_Btn)
        val doneBtn = view.findViewById<ImageButton>(R.id.doneBtn)
        val deleteBtn = view.findViewById<ImageButton>(R.id.deleteBtn)
        val bacBtn = view.findViewById<ImageButton>(R.id.back)
        timerView = view.findViewById(R.id.timerView)
        val playerView = view.findViewById<RecorderWaveformView>(R.id.playerView)

        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions,
            REQUEST_RECORD_AUDIO_PERMISSION
        )

        handler = Handler(Looper.myLooper()!!)

        recordBtn.setOnClickListener {

            when {
                onPause -> resumeRecording(recordBtn, playerView)
                recording -> pauseRecording(recordBtn)
                else -> startRecording(doneBtn, deleteBtn, recordBtn, playerView)
            }
        }

        doneBtn.setOnClickListener {
            stopRecording()
            showBottomSheet()
        }

        bacBtn.setOnClickListener {
            findNavController().navigate(R.id.action_recordFragment_to_navigation_home)
        }

        deleteBtn.setOnClickListener {
            stopRecording()

            File(dirPath + fileName).delete()
        }
        deleteBtn.isClickable = false
    }

    private fun showBottomSheet() {
        val bottomSheet = BottomSheet(dirPath, fileName, this)
        bottomSheet.show(requireActivity().supportFragmentManager, LOG_TAG)

    }

    private fun startRecording(
        doneBtn: ImageButton,
        deleteBtn: ImageButton,
        recordBtn: ImageButton,
        playerView: RecorderWaveformView
    ) {

        if (!viewModel.permissionToRecordAccepted.value!!) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions,
                REQUEST_RECORD_AUDIO_PERMISSION
            )
            return
        }

        doneBtn.visibility = View.VISIBLE
        deleteBtn.isClickable = true
        deleteBtn.setImageResource(R.drawable.ic_delete_enabled)

        recording = true
        timer = Timer(this)
        timer.start()

        val simpleDateFormat = SimpleDateFormat(DATETIME_PATTERN)
        val date: String = simpleDateFormat.format(Date())

        dirPath = "${requireActivity().externalCacheDir?.absolutePath}/"
        fileName = "voice_record_${date}.mp3"

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(dirPath + fileName)
            try {
                prepare()
            } catch (e: IOException) {
            }

            start()
        }

        recordBtn.setImageResource(R.drawable.ic_pause)

        animatePlayerView(playerView)

    }

    private fun animatePlayerView(playerView: RecorderWaveformView) {
        if (recording && !onPause) {
            val amp = recorder!!.maxAmplitude
            playerView.updateAmps(amp)

            handler.postDelayed(
                {
                    kotlin.run { animatePlayerView(playerView) }
                }, refreshRate
            )
        }
    }

    private fun pauseRecording(recordBtn: ImageButton) {
        onPause = true
        recorder?.apply {
            pause()
        }
        recordBtn.setImageResource(R.drawable.ic_record)
        timer.pause()

    }

    private fun resumeRecording(recordBtn: ImageButton, playerView: RecorderWaveformView) {
        onPause = false
        recorder?.apply {
            resume()
        }
        recordBtn.setImageResource(R.drawable.ic_pause)
        animatePlayerView(playerView)
        timer.start()
    }

    private fun stopRecording() {
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
        } catch (e: Exception) {
        }

        timerView.text = TIME_ZERO
    }

    override fun onCancelClicked() {
        Toast.makeText(requireActivity(), RECORD_DELETED, Toast.LENGTH_SHORT).show()
        stopRecording()
    }

    override fun onOkClicked(filePath: String, filename: String) {
        val db = Room.databaseBuilder(
            requireActivity(),
            AppDatabase::class.java,
            RECORD_CLASS_NAME
        ).build()

        val duration = timer.format().split(DOT_DELIMITER)[0]
        stopRecording()

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                viewModel.insert(db, AudioRecord(filename, filePath, Date().time, duration))
            }
        }
    }

    override fun onTimerUpdate(duration: String) {
        requireActivity().runOnUiThread {
            if (recording)
                timerView.text = duration
        }
    }

    companion object {
        private const val REFRESH_RATE: Long = 60
        private const val RECORD_CLASS_NAME = "audioRecords"
        private const val RECORD_DELETED = "Запись удалена"
        private const val DOT_DELIMITER = "."
        private const val DATETIME_PATTERN = "yyyy.MM.dd_hh.mm.ss"
        private const val TIME_ZERO ="00:00.00"
        private const val LOG_TAG = "AudioRecordTest"
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}