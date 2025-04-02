package com.example.ease.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ease.R

class ChatSelectorFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.homePageFragment)
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnRoutine = view.findViewById<Button>(R.id.btnRoutine)
        val btnEmergency = view.findViewById<Button>(R.id.btnEmergency)
        val backIcon = view.findViewById<ImageView>(R.id.backIcon)

        btnRoutine.setOnClickListener {
            findNavController().navigate(R.id.chatRoutineFragment)

        }

        btnEmergency.setOnClickListener {
            val dialog = EmergencyDialogFragment()
            dialog.show(childFragmentManager, "emergencyDialog")
        }


        backIcon.setOnClickListener {
            findNavController().navigate(R.id.homePageFragment)
        }
    }

}
