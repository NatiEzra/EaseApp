package com.example.ease.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.ease.ui.activities.LoginRegisterActivity
import com.example.ease.R
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.example.ease.repositories.AuthRepository
import com.example.ease.viewmodel.AuthViewModel


// TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private const val ARG_PARAM1 = "param1"
    private const val ARG_PARAM2 = "param2"

class loginFragment : Fragment() {


    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var newMemberLink: TextView
    private lateinit var googleLoginButton: Button
    private lateinit var googleSignInClient: GoogleSignInClient
    private var param1: String? = null
    private var param2: String? = null
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var viewModel: AuthViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        // Configure Google Sign-In
       /* val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

      /*  val googleLoginButton: Button = view.findViewById(R.id.google_login)
        googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }*/

        val newMemberTextView: TextView = view.findViewById(R.id.new_member_link)
        newMemberTextView.setOnClickListener {
            // Call the activity's method to replace the fragment
            (activity as? LoginRegisterActivity)?.onNewMemberClicked(it)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emailField = view.findViewById(R.id.email_field_login)
        passwordField = view.findViewById(R.id.password_field)
        loginButton = view.findViewById(R.id.login_button)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        viewModel.authState.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                (activity as? LoginRegisterActivity)?.navigateToHome()
            }.onFailure {
                Toast.makeText(requireContext(), it.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        }


        setupListeners()


    }




    private fun setupListeners() {

        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(email, password)
            }
        }

        /*
            private fun signInWithGoogle() {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }

            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                super.onActivityResult(requestCode, resultCode, data)

                if (requestCode == RC_SIGN_IN) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    handleSignInResult(task)
                }
            }

            private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
                try {
                    val account = completedTask.getResult(ApiException::class.java)
                    // Signed in successfully, show authenticated UI.
                } catch (e: ApiException) {
                    // Sign in failed, handle the error
                }
            }

            companion object {
                private const val RC_SIGN_IN = 9001

                @JvmStatic
                fun newInstance(param1: String, param2: String) =
                    loginFragment().apply {
                        arguments = Bundle().apply {
                            putString(ARG_PARAM1, param1)
                            putString(ARG_PARAM2, param2)
                        }
                    }

         */
    }


}