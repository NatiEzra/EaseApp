package com.example.ease.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ease.R   // make sure package path is correct

class PrivacyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // layout file you pasted earlier
        return inflater.inflate(R.layout.fragment_privacy, container, false)
    }
}
