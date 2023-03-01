package com.example.vkinternshipaudionotes.presentation.view.player

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.vkinternshipaudionotes.R
import com.example.vkinternshipaudionotes.presentation.customView.PlayerWaveformView
import com.example.vkinternshipaudionotes.presentation.viewmodel.MainViewModel
import com.google.android.material.chip.Chip

class PlayerFragment : Fragment() {

    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private lateinit var mediaPlayer: MediaPlayer
    private var playbackSpeed: Float = START_SPEED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mediaPlayer.stop()
                mediaPlayer.release()
                handler.removeCallbacks(runnable)
                findNavController().navigate(R.id.action_playerFragment_to_navigation_home)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chip = view.findViewById<Chip>(R.id.chip)
        val btnPlay = view.findViewById<ImageButton>(R.id.btnPlay)
        val btnBackward = view.findViewById<ImageButton>(R.id.btnBackward)
        val btnForward = view.findViewById<ImageButton>(R.id.btnForward)
        val playerView = view.findViewById<PlayerWaveformView>(R.id.playerView)
        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        val tvFilename = view.findViewById<TextView>(R.id.tvFilename)

        val mainViewModel =
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        val filePath = mainViewModel.filePath.value
        val filename = mainViewModel.fileName.value

        tvFilename.text = filename

        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(filePath)
            prepare()
        }

        seekBar.max = mediaPlayer.duration

        handler = Handler(Looper.getMainLooper())
        playPausePlayer(btnPlay, seekBar, playerView)

        mediaPlayer.setOnCompletionListener {
            stopPlayer(btnPlay)
        }

        btnPlay.setOnClickListener {
            playPausePlayer(btnPlay, seekBar, playerView)
        }

        btnForward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition + SEEKBAR_STEP)
            seekBar.progress += SEEKBAR_STEP
        }

        btnBackward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition - SEEKBAR_STEP)
            seekBar.progress -= SEEKBAR_STEP

        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) mediaPlayer.seekTo(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })


        chip.setOnClickListener {
            when (playbackSpeed) {
                0.5f -> playbackSpeed += 0.5f
                1.0f -> playbackSpeed += 0.5f
                1.5f -> playbackSpeed += 0.5f
                2.0f -> playbackSpeed = 0.5f
            }
            mediaPlayer.playbackParams = PlaybackParams().setSpeed(playbackSpeed)
            chip.text = "x $playbackSpeed"
        }
    }

    private fun playPausePlayer(btnPlay: ImageButton, seekBar: SeekBar, playerView: PlayerWaveformView) {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            btnPlay.background =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_pause_circle, requireActivity().theme)

            runnable = Runnable {
                val progress = mediaPlayer.currentPosition
                Log.d("progress", progress.toString())
                seekBar.progress = progress

                val amp = 80 + Math.random() * 300
                playerView.updateAmps(amp.toInt())

                handler.postDelayed(runnable, delay)
            }
            handler.postDelayed(runnable, delay)
        } else {
            mediaPlayer.pause()
            btnPlay.background =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, requireActivity().theme)

            handler.removeCallbacks(runnable)
        }
    }

    private fun stopPlayer(btnPlay: ImageButton) {
        btnPlay.background =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, requireActivity().theme)
        handler.removeCallbacks(runnable)
    }

    companion object {
        private const val START_SPEED = 1.0f
        private const val SEEKBAR_STEP = 1000
        private val delay = 100L
    }
}