package com.devang.campushelper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_splash,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val logoContainer = view.findViewById<View>(R.id.logoContainer)
        val tvAppName = view.findViewById<View>(R.id.tvAppName)
        val tvTagline = view.findViewById<View>(R.id.tvTagline)

        // Initial animation state
        listOf(logoContainer, tvAppName, tvTagline).forEach {
            it.alpha = 0f
            it.translationY = 40f
        }

        // Logo animation
        logoContainer.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(700)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // App name animation
        tvAppName.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(200)
            .setDuration(700)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // Tagline animation
        tvTagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(350)
            .setDuration(700)
            .setInterpolator(DecelerateInterpolator())
            .start()

        viewLifecycleOwner.lifecycleScope.launch {

            delay(2000)

            if (isAdded) {
                findNavController().navigate(
                    R.id.action_splashFragment_to_FirstFragment
                )
            }
        }
    }
}