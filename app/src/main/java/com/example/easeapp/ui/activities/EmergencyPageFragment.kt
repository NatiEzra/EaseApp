package com.example.ease.ui.emergency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ease.R

class EmergencyPageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_emergency_page, container, false)

        val policeCard = view.findViewById<LinearLayout>(R.id.cardPolice)
        val madaCard = view.findViewById<LinearLayout>(R.id.cardMada)
        val eranCard = view.findViewById<LinearLayout>(R.id.cardEran)
        val natalCard = view.findViewById<LinearLayout>(R.id.cardNatal)
        val ezerCard = view.findViewById<LinearLayout>(R.id.cardEzer)

        policeCard.setOnClickListener {
            EmergencyInfoDialogFragment.newInstance(
                name = "Police",
                description = "The Israel Police is part of the Israeli security system responsible for law enforcement, maintaining public order, and ensuring the internal security of the State of Israel.",
                logo = R.drawable.police_logo,
                phone = "100"
            ).show(parentFragmentManager, "policeInfo")
        }

        madaCard.setOnClickListener {
            EmergencyInfoDialogFragment.newInstance(
                name = "MDA",
                description = "Magen David Adom is Israel's national emergency medical, disaster, ambulance and blood bank service.",
                logo = R.drawable.mada_logo,
                phone = "101"
            ).show(parentFragmentManager, "madaInfo")
        }

        eranCard.setOnClickListener {
            EmergencyInfoDialogFragment.newInstance(
                name = "Eran",
                description = "Emotional first aid and mental support by trained volunteers and professionals.",
                logo = R.drawable.eran_logo,
                phone = "1201"
            ).show(parentFragmentManager, "eranInfo")
        }

        natalCard.setOnClickListener {
            EmergencyInfoDialogFragment.newInstance(
                name = "Natal",
                description = "Provides multidisciplinary treatment and support to trauma victims of war and terror.",
                logo = R.drawable.natal_logo,
                phone = "1800-363-363"
            ).show(parentFragmentManager, "natalInfo")
        }

        ezerCard.setOnClickListener {
            EmergencyInfoDialogFragment.newInstance(
                name = "Ezer Mizion",
                description = "Provides a wide range of medical and social support services in Israel.",
                logo = R.drawable.ezer_logo,
                phone = "03-6144444"
            ).show(parentFragmentManager, "ezerInfo")
        }

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}
