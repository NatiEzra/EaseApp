package com.example.ease.ui.chat

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.ease.R

class EmergencyDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_emergency_confirm, null)

        val btnStart = view.findViewById<Button>(R.id.btnStartEmergency)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        btnStart.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(requireContext(), "Starting Emergency Chat...", Toast.LENGTH_SHORT).show()
            // TODO: נווט למסך חירום אם יש כזה
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}
