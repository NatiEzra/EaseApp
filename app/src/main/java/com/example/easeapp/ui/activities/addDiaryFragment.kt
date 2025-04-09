package com.example.easeapp.ui.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.ease.R
import com.example.easeapp.model.DiaryRepo

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [addDiaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class addDiaryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    // check
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view=inflater.inflate(R.layout.fragment_add_diary, container, false)
        var postButton=view.findViewById<Button>(R.id.postButtonDiary)
        var diaryEditText=view.findViewById<EditText>(R.id.diaryEditText)
        progressBar=view.findViewById(R.id.addDiaryProgressBar)
        // Inflate the layout for this fragment

        postButton.setOnClickListener {
            progressBar.visibility=View.VISIBLE
            var diaryTextString=diaryEditText.text.toString()
            if(diaryTextString.isNotEmpty()){
                context?.let { it1 -> DiaryRepo(it1).addDiary(diaryTextString) }
                progressBar.visibility=View.GONE
        }
            else{
                Toast.makeText(context,"Diary empty is invalid", Toast.LENGTH_LONG).show()

            }
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment addDiaryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            addDiaryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}