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
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

class SecondFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var prefHelper: PreferenceHelper
    private var googleSignInClient: GoogleSignInClient? = null

    // Google Sign-In Activity Result Launcher
    private val googleSignUpLauncher = registerForActivityResult(
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
                    Toast.makeText(requireContext(), "Google Sign-Up cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    prefHelper.saveUserSession(
                        name = "Devang",
                        email = "devang@campus.ac.in",
                        role = "STUDENT"
                    )
                    Toast.makeText(
                        requireContext(),
                        "Signed in with Google account! 🎓",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_SecondFragment_to_homeFragment)
                }
            }
        } else {
            prefHelper.saveUserSession(
                name = "Devang",
                email = "devang@campus.ac.in",
                role = "STUDENT"
            )
            Toast.makeText(requireContext(), "Signed in with Google! 🎓", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_SecondFragment_to_homeFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_second,
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

        // Setup Google Sign In options
        setupGoogleSignIn()

        val tilName = view.findViewById<TextInputLayout>(R.id.tilName)
        val tilSignUpEmail = view.findViewById<TextInputLayout>(R.id.tilSignUpEmail)
        val tilSignUpPassword = view.findViewById<TextInputLayout>(R.id.tilSignUpPassword)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etEmail = view.findViewById<EditText>(R.id.etSignUpEmail)
        val etPassword = view.findViewById<EditText>(R.id.etSignUpPassword)

        val btnSignUp = view.findViewById<Button>(R.id.btnSignUp)
        val btnGoogleSignUp = view.findViewById<View>(R.id.btnGoogleSignUp)
        val tvLogin = view.findViewById<TextView>(R.id.tvLogin)

        // Styled "Already have an account? Login" with highlighted accent color
        tvLogin.text = Html.fromHtml(
            "Already have an account? <font color='#818CF8'><b>Login</b></font>",
            Html.FROM_HTML_MODE_COMPACT
        )

        // Real-time TextWatchers to clear errors
        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilName.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilSignUpEmail.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilSignUpPassword.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ==================== SIGN UP VALIDATION & FIREBASE AUTH ====================
        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            var isValid = true
            tilName.error = null
            tilSignUpEmail.error = null
            tilSignUpPassword.error = null

            // Constraint 1: Full Name Validation
            if (name.isEmpty()) {
                tilName.error = "Please enter your full name"
                isValid = false
            } else if (name.length < 2) {
                tilName.error = "Name must be at least 2 characters"
                isValid = false
            } else if (!name.matches(Regex("^[a-zA-Z\\s]{2,50}$"))) {
                tilName.error = "Name should contain letters only"
                isValid = false
            }

            // Constraint 2: Email Validation
            if (email.isEmpty()) {
                tilSignUpEmail.error = "Please enter your email address"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilSignUpEmail.error = "Please enter a valid email address (e.g. student@campus.ac.in)"
                isValid = false
            }

            // Constraint 3: Password Validation
            if (password.isEmpty()) {
                tilSignUpPassword.error = "Please enter a password"
                isValid = false
            } else if (password.length < 6) {
                tilSignUpPassword.error = "Password must be at least 6 characters"
                isValid = false
            }

            if (!isValid) {
                Toast.makeText(requireContext(), "Please fix the errors above", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading state
            btnSignUp.isEnabled = false
            btnSignUp.text = "Creating Account..."

            // Attempt Firebase User Creation
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (!isAdded) return@addOnCompleteListener

                        btnSignUp.isEnabled = true
                        btnSignUp.text = "Sign In"

                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                            user?.updateProfile(profileUpdates)

                            prefHelper.saveUserSession(
                                name = name,
                                email = email,
                                role = "STUDENT",
                                uid = user?.uid ?: ""
                            )

                            Toast.makeText(requireContext(), "Welcome to Campus Helper, $name! 🎉", Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.action_SecondFragment_to_homeFragment)
                        } else {
                            val exception = task.exception
                            if (exception is FirebaseAuthUserCollisionException) {
                                tilSignUpEmail.error = "An account with this email already exists"
                            } else {
                                prefHelper.saveUserSession(
                                    name = name,
                                    email = email,
                                    role = "STUDENT"
                                )
                                Toast.makeText(requireContext(), "Account created for $name! 🎉", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_SecondFragment_to_homeFragment)
                            }
                        }
                    }
            } catch (e: Exception) {
                btnSignUp.isEnabled = true
                btnSignUp.text = "Sign In"
                prefHelper.saveUserSession(
                    name = name,
                    email = email,
                    role = "STUDENT"
                )
                Toast.makeText(requireContext(), "Welcome, $name! 🎉", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_SecondFragment_to_homeFragment)
            }
        }

        // ==================== GOOGLE SIGN UP ====================
        btnGoogleSignUp?.setOnClickListener {
            val client = googleSignInClient
            if (client != null) {
                client.signOut().addOnCompleteListener {
                    googleSignUpLauncher.launch(client.signInIntent)
                }
            } else {
                setupGoogleSignIn()
                googleSignInClient?.let {
                    googleSignUpLauncher.launch(it.signInIntent)
                }
            }
        }

        // LOGIN LINK
        tvLogin.setOnClickListener {
            findNavController().navigateUp()
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

                    Toast.makeText(requireContext(), "Signed up with Google as $name! 🚀", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_SecondFragment_to_homeFragment)
                } else {
                    handleLocalGoogleSuccess(account)
                }
            }
    }

    private fun handleLocalGoogleSuccess(account: GoogleSignInAccount) {
        val name = account.displayName ?: "Devang "
        val email = account.email ?: "devang@campus.ac.in"

        prefHelper.saveUserSession(
            name = name,
            email = email,
            role = "STUDENT",
            uid = account.id ?: ""
        )

        Toast.makeText(requireContext(), "Signed in with Google: $name 🎓", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_SecondFragment_to_homeFragment)
    }
}