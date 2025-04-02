package com.example.ease.ui.chat

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import com.example.ease.R

class EmergencyDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_emergency_confirm, null)

        val btnStart = view.findViewById<Button>(R.id.btnStartEmergency)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        btnStart.setOnClickListener {
            dialog.dismiss()

            // ניווט למסך EmergencyChatFragment
            val navController = NavHostFragment.findNavController(
                requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!
            )
            navController.navigate(R.id.emergencyChatFragment)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}
