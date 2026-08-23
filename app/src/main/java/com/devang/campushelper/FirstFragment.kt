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

class FirstFragment : Fragment() {

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

        // Clear error on typing
        etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilEmail?.error = null
        }
        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilPassword?.error = null
        }

        // LOGIN BUTTON
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            var hasError = false
            tilEmail?.error = null
            tilPassword?.error = null

            if (email.isEmpty()) {
                tilEmail?.error = "Please enter your email"
                hasError = true
            }

            if (password.isEmpty()) {
                tilPassword?.error = "Please enter your password"
                hasError = true
            }

            if (hasError) {
                Toast.makeText(
                    requireContext(),
                    "Please enter email and password",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                findNavController().navigate(
                    R.id.action_FirstFragment_to_homeFragment
                )
            }
        }

        // GOOGLE SIGN IN
        btnGoogle?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Google Sign-In clicked",
                Toast.LENGTH_SHORT
            ).show()
        }

        // FORGOT PASSWORD
        tvForgotPassword?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Password reset link sent to your registered email",
                Toast.LENGTH_SHORT
            ).show()
        }

        // SIGN UP BUTTON
        tvSignUp.setOnClickListener {
            findNavController().navigate(
                R.id.action_FirstFragment_to_SecondFragment
            )
        }
    }
}