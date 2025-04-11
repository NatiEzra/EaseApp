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

        val diaryEditText = view.findViewById<EditText>(R.id.diaryEditText)
        val postButton = view.findViewById<Button>(R.id.postButtonDiary)
        val progressBar = view.findViewById<ProgressBar>(R.id.addDiaryProgressBar)

        postButton.setOnClickListener {
            val text = diaryEditText.text.toString()
            progressBar.visibility = View.VISIBLE
            viewModel.addDiaryEntry(requireContext(), text)
        }

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
