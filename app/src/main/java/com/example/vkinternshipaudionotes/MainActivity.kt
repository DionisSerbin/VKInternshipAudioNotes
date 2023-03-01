package com.example.vkinternshipaudionotes


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.alterpat.voicerecorder.db.AudioRecord
import com.example.vkinternshipaudionotes.data.db.AppDatabase
import com.example.vkinternshipaudionotes.presentation.view.Adapter
import com.example.vkinternshipaudionotes.presentation.view.PlayerActivity
import com.example.vkinternshipaudionotes.presentation.view.RecordActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), Adapter.OnItemClickListener{


    private lateinit var adapter : Adapter
    private lateinit var audioRecords : List<AudioRecord>
    private lateinit var db : AppDatabase
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var nbSelected = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recordButton = findViewById<ImageButton>(R.id.record_button)

        recordButton.setOnClickListener{
            val intent = Intent(this, RecordActivity::class.java)
            startActivity(intent)
        }


        audioRecords = emptyList()
        adapter = Adapter(audioRecords, this)

        val recyclerview = findViewById<RecyclerView>(R.id.recycler_records)

        recyclerview.adapter = adapter
        recyclerview.layoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords")
            .build()

        fetchAll()
    }

    private fun fetchAll(){
        GlobalScope.launch {
            audioRecords = db.audioRecordDAO().getAll()
            adapter.setData(audioRecords)
        }
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this, PlayerActivity::class.java)
        val audioRecord = audioRecords[position]

        if(adapter.isEditMode()){
            Log.d("ITEMCHANGE", audioRecord.isChecked.toString())
            audioRecord.isChecked = !audioRecord.isChecked
            adapter.notifyItemChanged(position)

            nbSelected = if (audioRecord.isChecked) nbSelected+1 else nbSelected-1
        }else{
            intent.putExtra("filepath", audioRecord.filePath)
            intent.putExtra("filename", audioRecord.filename)
            startActivity(intent)
        }

    }

    override fun onItemLongClick(position: Int) {
        adapter.setEditMode(true)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val audioRecord = audioRecords[position]

        audioRecord.isChecked = !audioRecord.isChecked

        nbSelected = if (audioRecord.isChecked) nbSelected+1 else nbSelected-1

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)

    }

}