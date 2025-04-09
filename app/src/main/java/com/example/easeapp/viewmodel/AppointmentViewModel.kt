package com.example.easeapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ease.repositories.AuthRepository
import com.example.easeapp.model.requests.AppointmentRequest
import com.example.easeapp.repositories.AppointmentRepository

class AppointmentViewModel(
    private val appointRepo: AppointmentRepository = AppointmentRepository.shared
) : ViewModel() {
    private val _createAppointmentStatus = MutableLiveData<Result<Boolean>>()
    val createAppointmentStatus: LiveData<Result<Boolean>> get() = _createAppointmentStatus
    private val _appointmentDate = MutableLiveData<String?>()
    val appointmentDate: MutableLiveData<String?> get() = _appointmentDate
    private val _cancelAppointmentStatus = MutableLiveData<Result<Boolean>>()
    val cancelAppointmentStatus: LiveData<Result<Boolean>> get() = _cancelAppointmentStatus

    fun createAppointment(
        context: Context,
        patientId: String,
        doctorId: String,
        appointmentDate: String,
        isEmergency: Boolean = false,
        notes: String? = null,
    ) {
        val request = AppointmentRequest(
            patientId = patientId,
            doctorId = doctorId,
            appointmentDate = appointmentDate,
            notes = notes,
            isEmergency = isEmergency,
            initiator = "patient"
        )
        appointRepo.createAppointment(context, request) { success, _, error ->
            if (success) {
                _createAppointmentStatus.postValue(Result.success(true))
            } else {
                _createAppointmentStatus.postValue(Result.failure(Throwable(error)))
            }
        }
    }

    fun getClosestAppointment(context: Context, doctorId: String) {
        appointRepo.getClosestAppointment(context, doctorId) { success, date, error ->
            if (success && date != null) {
                _appointmentDate.postValue(date) // update the UI
            } else {
                _appointmentDate.postValue(error ?: "An error occurred")
            }
        }
    }


    fun cancelAppointment(context: Context, appointmentId: String) {
        appointRepo.cancelAppointment(context, appointmentId) { success, error ->
            if (success) {
                _cancelAppointmentStatus.postValue(Result.success(true))
            } else {
                _cancelAppointmentStatus.postValue(Result.failure(Throwable(error)))
            }
        }
    }


}