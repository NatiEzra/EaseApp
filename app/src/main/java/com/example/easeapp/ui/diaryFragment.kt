package com.example.easeapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R
import com.example.easeapp.viewmodel.DiaryViewModel
import com.example.easeapp.ui.adapters.DiaryAdapter


class DiaryFragment : Fragment() {

    private val viewModel: DiaryViewModel by viewModels()
    private lateinit var diaryAdapter: DiaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // כפתור הוספת יומן
        val addDiaryButton = view.findViewById<ImageButton>(R.id.addDiary)
        addDiaryButton.setOnClickListener {
            findNavController().navigate(R.id.addDiaryFragment)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.fragment_feed_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        diaryAdapter = DiaryAdapter()
        recyclerView.adapter = diaryAdapter
        val shareYourThought = view.findViewById<TextView>(R.id.noPostsTextView)

        viewModel.userDiaries.observe(viewLifecycleOwner) { diaries ->
            if(!diaries.isEmpty()){
                shareYourThought.visibility = View.GONE
            }
            else{
                shareYourThought.visibility = View.VISIBLE
            }
            diaryAdapter.submitList(diaries)
        }

        // טעינת היומנים
        viewModel.loadUserDiaries(requireContext())
    }
}
