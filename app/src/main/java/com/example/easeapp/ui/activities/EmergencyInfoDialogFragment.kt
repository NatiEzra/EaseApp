package com.example.ease.ui.emergency

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.ease.R

class EmergencyInfoDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_emergency_info, null)

        val orgName = arguments?.getString("name") ?: ""
        val orgDesc = arguments?.getString("description") ?: ""
        val orgLogo = arguments?.getInt("logo") ?: R.drawable.ic_emergency
        val phoneNumber = arguments?.getString("phone") ?: ""

        view.findViewById<TextView>(R.id.titleText).text = orgName
        view.findViewById<TextView>(R.id.descriptionText).text = orgDesc
        view.findViewById<ImageView>(R.id.logoImage).setImageResource(orgLogo)

        view.findViewById<Button>(R.id.callButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
            dismiss()
        }
        view.findViewById<Button>(R.id.backButton).setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    companion object {
        fun newInstance(name: String, description: String, logo: Int, phone: String): EmergencyInfoDialogFragment {
            val fragment = EmergencyInfoDialogFragment()
            val args = Bundle().apply {
                putString("name", name)
                putString("description", description)
                putInt("logo", logo)
                putString("phone", phone)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
