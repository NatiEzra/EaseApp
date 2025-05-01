package com.example.ease.ui.chat

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import com.example.ease.model.local.AppDatabase
import com.example.easeapp.model.requests.AppointmentRequest
import com.example.easeapp.repositories.AppointmentRepository
import com.example.easeapp.viewmodel.AppointmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Date

class EmergencyDialogFragment : DialogFragment() {
    private val appointmentViewModel: AppointmentViewModel by viewModels()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_emergency_confirm, null)

        val btnStart = view.findViewById<Button>(R.id.btnStartEmergency)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        btnStart.setOnClickListener {
            // We’ll dismiss after navigation succeeds
            // Capture for use inside lambdas
            val fragment = this@EmergencyDialogFragment
            val ctx      = fragment.requireContext()

            fragment.lifecycleScope.launch {
                // 1) Load current user off the main thread
                val user = withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(ctx)
                        .userDao()
                        .getCurrentUser()
                }
                val patientId = user?._id ?: return@launch

                // 2) Build your appointment request
                val request = AppointmentRequest(
                    patientId      = patientId,
                    doctorId       = null,
                    appointmentDate = Instant.now().toString(),
                    notes          = null,
                    isEmergency    = true,
                    initiator      = "patient"
                )

                // 3) Fire off createAppointment, passing ctx and our callback
                AppointmentRepository.shared.createAppointment(
                    ctx,
                    request
                ) { success, details, errorMessage ->
                    if (!success || details == null) {
                        // Show an error on the main thread
                        fragment.lifecycleScope.launch {
                            Toast.makeText(
                                ctx,
                                "Failed to start emergency chat: ${errorMessage ?: "unknown error"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@createAppointment
                    }

                    // 4) Navigate using the correct Safe-Args directions
                    fragment.lifecycleScope.launch {
                        val action = ChatRoutineFragmentDirections
                            .actionRoutineFragmentToMeetingChatFragment(details._id)
                        fragment.findNavController().navigate(action)
                        // 5) Now that we’ve navigated, dismiss the dialog
                        dialog.dismiss()
                    }
                }
            }
        }


        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}
