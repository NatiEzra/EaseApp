package com.example.easeapp.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import com.example.easeapp.viewmodel.DiaryViewModel

class AddDiaryFragment : Fragment() {

    private val viewModel: DiaryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val diaryId = arguments?.getString("diaryId")
        val diaryText = arguments?.getString("diaryText")
        val diaryEditText = view.findViewById<EditText>(R.id.diaryEditText)
        val postButton = view.findViewById<Button>(R.id.postButtonDiary)
        val progressBar = view.findViewById<ProgressBar>(R.id.addDiaryProgressBar)
        val cancelButton= view.findViewById<Button>(R.id.cancelButton)
        postButton.setOnClickListener {
            val text = diaryEditText.text.toString()

            if (text.isNotBlank()) {
                if (diaryId != null) {
                    viewModel.updateDiaryEntry(requireContext(), diaryId, text)
                } else {
                    viewModel.addDiaryEntry(requireContext(), text)
                }
            } else {
                Toast.makeText(requireContext(), "Diary text is empty", Toast.LENGTH_SHORT).show()
            }
        }
        cancelButton.setOnClickListener {
            findNavController().navigate(R.id.diaryFragment)
        }
        diaryEditText.setText(diaryText?:"")
        postButton.text=if(diaryId.isNullOrEmpty()) "Post" else "Save"
        viewModel.diarySaved.observe(viewLifecycleOwner) { result ->
            progressBar.visibility = View.GONE
            result
                .onSuccess {
                    findNavController().navigate(R.id.diaryFragment)
                }
                .onFailure {
                    Toast.makeText(requireContext(), it.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
