package com.example.curs_alexander.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.curs_alexander.R
import com.example.curs_alexander.data.Prefs
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingFragment : Fragment() {

    private lateinit var prefs: Prefs
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private val pages = listOf(
        R.layout.page_onboarding_1,
        R.layout.page_onboarding_2,
        R.layout.page_onboarding_3,
        R.layout.page_onboarding_4,
        R.layout.page_onboarding_5,
        R.layout.page_onboarding_6,
        R.layout.page_onboarding_7,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = view.findViewById(R.id.viewPagerOnboarding)
        tabLayout = view.findViewById(R.id.tabIndicator)

        val adapter = OnboardingPagerAdapter(
            pages,
            onNextClick = { position -> viewPager.currentItem = position + 1 },
            onFinishClick = { name, consentChecked ->
                if (consentChecked) {
                    val trimmed = name?.trim().orEmpty()
                    if (trimmed.isNotEmpty()) {
                        prefs.userName = trimmed
                    }
                    prefs.onboardingCompleted = true
                    // Переход на главный экран и очистка back stack
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        )
        viewPager.adapter = adapter

        viewPager.setPageTransformer { page, position ->
            page.alpha = 0.25f + (1 - kotlin.math.abs(position))
            val scale = 0.85f + (1 - kotlin.math.abs(position)) * 0.15f
            page.scaleX = scale
            page.scaleY = scale
        }

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
    }
}
