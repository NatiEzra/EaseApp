package com.example.easeapp.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
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
    private lateinit var progessbar: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        progessbar = view.findViewById(R.id.feedProgressBar)
        super.onViewCreated(view, savedInstanceState)
        // כפתור הוספת יומן
        val addDiaryButton = view.findViewById<ImageButton>(R.id.addDiary)
        addDiaryButton.setOnClickListener {
            findNavController().navigate(R.id.addDiaryFragment)
        }

//        diaryAdapter = DiaryAdapter { item ->
//            AlertDialog.Builder(requireContext())
//                .setTitle("Delete Post")
//                .setMessage("Are you sure you want to delete this post?")
//                .setPositiveButton("Yes") { dialog, _ ->
//                    context?.let { viewModel.deleteDiaryEntry(it,item._id?:"") } // צריך לממש את זה ב־ViewModel
//                    dialog.dismiss()
//                }
//                .setNegativeButton("Cancel") { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .show()
//        }


        val shareYourThought = view.findViewById<TextView>(R.id.noPostsTextView)
        val recyclerView = view.findViewById<RecyclerView>(R.id.fragment_feed_recycler_view)

        diaryAdapter = DiaryAdapter(
            onDeleteClick = { item ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Diary")
                    .setMessage("Are you sure you want to delete this diary?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        context?.let { viewModel.deleteDiaryEntry(it, item._id ?: "") }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            },
            onEditClick = { item ->
                // Handle edit action here, for example, navigate to an edit screen
                findNavController().navigate(
                    R.id.addDiaryFragment,
                    Bundle().apply {
                        putString("diaryId", item._id)
                        putString("diaryText", item.context)
                    }
                )
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = diaryAdapter
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progessbar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.userDiaries.observe(viewLifecycleOwner) { diaries ->
            //limit the context to 10 words and then see ...


            shareYourThought.visibility = if (diaries.isEmpty()) View.VISIBLE else View.GONE

            diaryAdapter.submitList(diaries)
        }

        viewModel.loadUserDiaries(requireContext())


    }
}
