package com.example.vkinternshipaudionotes.presentation.view.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.alterpat.voicerecorder.db.AudioRecord
import com.example.vkinternshipaudionotes.R
import com.example.vkinternshipaudionotes.data.db.AppDatabase
import com.example.vkinternshipaudionotes.presentation.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment(), Adapter.OnItemClickListener {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: Adapter
    private lateinit var audioRecords: List<AudioRecord>
    private lateinit var db: AppDatabase
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var nbSelected = START_NB_SELECTED

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recordButton = view.findViewById<ImageButton>(R.id.record_button)

        recordButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_recordFragment)
        }


        audioRecords = emptyList()
        adapter = Adapter(audioRecords, this)

        val recyclerview = view.findViewById<RecyclerView>(R.id.recycler_records)

        recyclerview.adapter = adapter
        recyclerview.layoutManager = LinearLayoutManager(requireContext())

        db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            RECORD_CLASS_NAME
        )
            .build()

        fetchAll()
    }

    private fun fetchAll() {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                audioRecords = viewModel.getAll(db)
            }
            adapter.setData(audioRecords)
        }
    }

    override fun onItemClick(position: Int) {
        val audioRecord = audioRecords[position]

        if (adapter.isEditMode()) {
            audioRecord.isChecked = !audioRecord.isChecked
            adapter.notifyItemChanged(position)

            nbSelected = if (audioRecord.isChecked) nbSelected + ONE_STEP else nbSelected - ONE_STEP
        } else {
            viewModel.saveFilePath(audioRecord.filePath)
            viewModel.saveFileName(audioRecord.filename)
            findNavController().navigate(R.id.action_navigation_home_to_playerFragment)
        }

    }

    override fun onItemLongClick(position: Int) {
        adapter.setEditMode(true)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val audioRecord = audioRecords[position]

        audioRecord.isChecked = !audioRecord.isChecked

        nbSelected = if (audioRecord.isChecked) nbSelected + ONE_STEP else nbSelected - ONE_STEP

    }

    companion object {
        private const val START_NB_SELECTED = 0
        private const val ONE_STEP = 1
        private const val RECORD_CLASS_NAME = "audioRecords"
    }
}