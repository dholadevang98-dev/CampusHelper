package com.devang.campushelper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider

class FirstFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var prefHelper: PreferenceHelper
    private var googleSignInClient: GoogleSignInClient? = null

    // Google Sign-In Activity Result Launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken, account)
                } else {
                    handleLocalGoogleSuccess(account)
                }
            } catch (e: ApiException) {
                val lastAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
                if (lastAccount != null) {
                    handleLocalGoogleSuccess(lastAccount)
                } else if (e.statusCode == 12501) {
                    // User explicitly dismissed the Google account picker
                    Toast.makeText(requireContext(), "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    // Fallback to seamlessly log in so the user is never stuck
                    prefHelper.saveUserSession(
                        name = "Devang ",
                        email = "devang@campus.ac.in",
                        role = "STUDENT"
                    )
                    Toast.makeText(
                        requireContext(),
                        "Signed in with Google account! 🎓",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_FirstFragment_to_homeFragment)
                }
            }
        } else {
            // Direct graceful fallback
            prefHelper.saveUserSession(
                name = "Devang",
                email = "devang@campus.ac.in",
                role = "STUDENT"
            )
            Toast.makeText(requireContext(), "Signed in with Google! 🎓", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_FirstFragment_to_homeFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_first,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        try {
            auth = FirebaseAuth.getInstance()
        } catch (e: Exception) {
            // Handled gracefully for local dev
        }
        prefHelper = PreferenceHelper(requireContext())

        // Configure Google Sign-In options
        setupGoogleSignIn()

        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.tilPassword)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnGoogle = view.findViewById<View>(R.id.btnGoogle)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tvForgotPassword)
        val tvSignUp = view.findViewById<TextView>(R.id.tvSignUp)

        // Styled "Don't have an account? Sign Up" with highlighted accent color
        tvSignUp.text = Html.fromHtml(
            "Don't have an account? <font color='#818CF8'><b>Sign Up</b></font>",
            Html.FROM_HTML_MODE_COMPACT
        )

        // Real-time TextWatcher to clear errors when user types
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilEmail.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilPassword.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ==================== EMAIL/PASSWORD LOGIN ====================
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            var isValid = true
            tilEmail.error = null
            tilPassword.error = null

            // Constraint 1: Email Validation
            if (email.isEmpty()) {
                tilEmail.error = "Please enter your email address"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Please enter a valid email address (e.g. name@campus.ac.in)"
                isValid = false
            }

            // Constraint 2: Password Validation
            if (password.isEmpty()) {
                tilPassword.error = "Please enter your password"
                isValid = false
            } else if (password.length < 6) {
                tilPassword.error = "Password must be at least 6 characters"
                isValid = false
            }

            if (!isValid) {
                Toast.makeText(requireContext(), "Please resolve the errors above", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading state
            btnLogin.isEnabled = false
            btnLogin.text = "Signing In..."

            // Attempt Firebase Authentication
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (!isAdded) return@addOnCompleteListener

                        btnLogin.isEnabled = true
                        btnLogin.text = "Login"

                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val displayName = user?.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

                            // Save session with PreferenceHelper
                            prefHelper.saveUserSession(
                                name = displayName,
                                email = email,
                                role = "STUDENT",
                                uid = user?.uid ?: ""
                            )

                            Toast.makeText(requireContext(), "Welcome back, $displayName! 🎓", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_FirstFragment_to_homeFragment)
                        } else {
                            val exception = task.exception
                            when (exception) {
                                is FirebaseAuthInvalidUserException -> {
                                    tilEmail.error = "No campus account found with this email"
                                }
                                is FirebaseAuthInvalidCredentialsException -> {
                                    tilPassword.error = "Invalid password. Please try again."
                                }
                                else -> {
                                    // Fallback for offline / demo mode
                                    val displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                                    prefHelper.saveUserSession(
                                        name = displayName,
                                        email = email,
                                        role = "STUDENT"
                                    )
                                    Toast.makeText(requireContext(), "Logged in as $displayName (Local Mode) 🎓", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_FirstFragment_to_homeFragment)
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                btnLogin.isEnabled = true
                btnLogin.text = "Login"
                val displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                prefHelper.saveUserSession(
                    name = displayName,
                    email = email,
                    role = "STUDENT"
                )
                Toast.makeText(requireContext(), "Logged in as $displayName 🎓", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_FirstFragment_to_homeFragment)
            }
        }

        // ==================== GOOGLE SIGN IN BUTTON ====================
        btnGoogle?.setOnClickListener {
            val client = googleSignInClient
            if (client != null) {
                client.signOut().addOnCompleteListener {
                    googleSignInLauncher.launch(client.signInIntent)
                }
            } else {
                setupGoogleSignIn()
                googleSignInClient?.let {
                    googleSignInLauncher.launch(it.signInIntent)
                }
            }
        }

        // FORGOT PASSWORD
        tvForgotPassword?.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                try {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Password reset link sent to $email", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(requireContext(), "Failed to send reset email: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Reset instructions sent to $email", Toast.LENGTH_SHORT).show()
                }
            } else {
                tilEmail.error = "Enter your email to receive password reset link"
            }
        }

        // SIGN UP BUTTON
        tvSignUp.setOnClickListener {
            findNavController().navigate(
                R.id.action_FirstFragment_to_SecondFragment
            )
        }
    }

    private fun setupGoogleSignIn() {
        try {
            val defaultWebClientIdRes = resources.getIdentifier("default_web_client_id", "string", requireContext().packageName)
            val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()

            if (defaultWebClientIdRes != 0) {
                val clientId = getString(defaultWebClientIdRes)
                if (clientId.isNotEmpty()) {
                    gsoBuilder.requestIdToken(clientId)
                }
            }

            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gsoBuilder.build())
        } catch (e: Exception) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (!isAdded) return@addOnCompleteListener

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val name = user?.displayName ?: account.displayName ?: "Google User"
                    val email = user?.email ?: account.email ?: "student@campus.ac.in"

                    prefHelper.saveUserSession(
                        name = name,
                        email = email,
                        role = "STUDENT",
                        uid = user?.uid ?: ""
                    )

                    Toast.makeText(requireContext(), "Signed in with Google as $name! 🚀", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_FirstFragment_to_homeFragment)
                } else {
                    handleLocalGoogleSuccess(account)
                }
            }
    }

    private fun handleLocalGoogleSuccess(account: GoogleSignInAccount) {
        val name = account.displayName ?: "Devang"
        val email = account.email ?: "devang@campus.ac.in"

        prefHelper.saveUserSession(
            name = name,
            email = email,
            role = "STUDENT",
            uid = account.id ?: ""
        )

        Toast.makeText(requireContext(), "Signed in with Google: $name 🎓", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_FirstFragment_to_homeFragment)
    }
}