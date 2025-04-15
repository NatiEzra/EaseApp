package com.example.ease.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import androidx.navigation.fragment.findNavController


class HomePageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_homepage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val diaryCard = view.findViewById<CardView>(R.id.cardDiary)
        val emergencyCard = view.findViewById<CardView>(R.id.cardEmergency)
        val chatCard = view.findViewById<CardView>(R.id.cardChat)

        val diaryInfo = view.findViewById<ImageView>(R.id.diaryInfo)
        val emergencyInfo = view.findViewById<ImageView>(R.id.emergencyInfo)
        val chatInfo = view.findViewById<ImageView>(R.id.chatInfo)

        diaryCard.setOnClickListener {
            openDiary()
        }

        emergencyCard.setOnClickListener {
            openEmergency()
        }

        chatCard.setOnClickListener {
            openChat()
        }

        diaryInfo.setOnClickListener {
            showHelpDialog(
                "The Diary Feature lets you log your thoughts and feelings daily.\n" +
                        "It analyzes your entries to detect emotional patterns and provide insights into your well-being."
            )
        }

        emergencyInfo.setOnClickListener {
            showHelpDialog(
                "Emergency mode connects you directly with mental health professionals or emergency services.\n" +
                        "Use this only in urgent situations when immediate help is needed."
            )
        }

        chatInfo.setOnClickListener {
            showHelpDialog(
                "The Chat feature allows you to talk with a support team for daily or non-urgent situations.\n" +
                        "Use it to share how you feel, ask questions, or simply connect with someone who listens."
            )
        }
    }
    private fun showHelpDialog(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_feature_help, null)
        dialogView.findViewById<TextView>(R.id.helpText).text = message

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val closeBtn = dialogView.findViewById<Button>(R.id.btnCloseHelp)
        closeBtn.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }



    private fun openDiary() {
        findNavController().navigate(R.id.action_homePageFragment_to_diaryFragment)
    }

    private fun openEmergency() {
        findNavController().navigate(R.id.action_homePageFragment_to_emergencyPageFragment)
    }


    private fun openChat() {
        findNavController().navigate(R.id.chatSelectorFragment)
    }

    private fun showInfo(title: String, message: String) {
        Toast.makeText(requireContext(), "$title Info: $message", Toast.LENGTH_LONG).show()
    }
}
