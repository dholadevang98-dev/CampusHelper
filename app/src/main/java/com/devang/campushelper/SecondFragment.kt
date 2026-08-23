package com.devang.campushelper

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout

class SecondFragment : Fragment() {

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

        // Clear error on typing / focus
        etName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilName?.error = null
        }
        etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilSignUpEmail?.error = null
        }
        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilSignUpPassword?.error = null
        }

        // SIGN UP BUTTON
        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            var hasError = false
            tilName?.error = null
            tilSignUpEmail?.error = null
            tilSignUpPassword?.error = null

            if (name.isEmpty()) {
                tilName?.error = "Please enter your name"
                hasError = true
            }

            if (email.isEmpty()) {
                tilSignUpEmail?.error = "Please enter your email"
                hasError = true
            }

            if (password.isEmpty()) {
                tilSignUpPassword?.error = "Please enter your password"
                hasError = true
            } else if (password.length < 6) {
                tilSignUpPassword?.error = "Password must be at least 6 characters"
                hasError = true
            }

            if (hasError) {
                Toast.makeText(
                    requireContext(),
                    "Please fill all required fields",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Welcome, $name! Account created successfully",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigate(
                    R.id.action_SecondFragment_to_FirstFragment
                )
            }
        }

        // GOOGLE SIGN UP
        btnGoogleSignUp?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Google Sign-Up clicked",
                Toast.LENGTH_SHORT
            ).show()
        }

        // LOGIN LINK
        tvLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}