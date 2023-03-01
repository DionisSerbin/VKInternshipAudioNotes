package com.example.vkinternshipaudionotes.presentation.common

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import com.example.vkinternshipaudionotes.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import java.io.File


class BottomSheet(
    private var dirPath: String,
    private var filename: String,
    private var listener: OnClickListener
) : BottomSheetDialogFragment() {

    interface OnClickListener {
        fun onCancelClicked()
        fun onOkClicked(filePath: String, filename: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet, container)
        val editText = view.findViewById<TextInputEditText>(R.id.filenameInput)


        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        filename = filename.split(MP3_STR)[0]
        editText.setText(filename)

        showKeyboard(editText)

        view.findViewById<Button>(R.id.okBtn).setOnClickListener {
            hideKeyboard(view)

            val updatedFilename = editText.text.toString()
            if(updatedFilename != filename){
                val newFile = File("$dirPath$updatedFilename$MP3_STR")
                File(dirPath+filename).renameTo(newFile)
            }

            dismiss()

            listener.onOkClicked("$dirPath$updatedFilename$MP3_STR", updatedFilename)
        }

        view.findViewById<Button>(R.id.cancelBtn).setOnClickListener {
            hideKeyboard(view)
            File(dirPath+filename).delete()

            dismiss()

            listener.onCancelClicked()
        }

        return view

    }

    private fun showKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        private const val MP3_STR = ".mp3"
    }
}