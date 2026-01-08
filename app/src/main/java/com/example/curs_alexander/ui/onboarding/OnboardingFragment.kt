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
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingFragment : Fragment() {

    private lateinit var prefs: Prefs
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var nextButton: MaterialButton

    private lateinit var adapter: OnboardingPagerAdapter

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
        nextButton = view.findViewById(R.id.btnNext)

        // Список из 5 фич онбординга
        val items = listOf(
            OnboardingItem(
                iconRes = R.mipmap.ic_launcher, // замените на свою иконку
                title = getString(R.string.onb_feature1_title),
                description = getString(R.string.onb_feature1_desc)
            ),
            OnboardingItem(
                iconRes = R.mipmap.ic_launcher,
                title = getString(R.string.onb_feature2_title),
                description = getString(R.string.onb_feature2_desc)
            ),
            OnboardingItem(
                iconRes = R.mipmap.ic_launcher,
                title = getString(R.string.onb_feature3_title),
                description = getString(R.string.onb_feature3_desc)
            ),
            OnboardingItem(
                iconRes = R.mipmap.ic_launcher,
                title = getString(R.string.onb_feature4_title),
                description = getString(R.string.onb_feature4_desc)
            ),
            OnboardingItem(
                iconRes = R.mipmap.ic_launcher,
                title = getString(R.string.onb_feature5_title),
                description = getString(R.string.onb_feature5_desc)
            )
        )

        adapter = OnboardingPagerAdapter(items)
        viewPager.adapter = adapter

        // Простой аниматор страниц
        viewPager.setPageTransformer { page, position ->
            page.alpha = 0.3f + (1 - kotlin.math.abs(position)) * 0.7f
            val scale = 0.9f + (1 - kotlin.math.abs(position)) * 0.1f
            page.scaleX = scale
            page.scaleY = scale
        }

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        updateButtonText(0)

        nextButton.setOnClickListener {
            val nextIndex = viewPager.currentItem + 1
            if (nextIndex < adapter.itemCount) {
                viewPager.currentItem = nextIndex
            } else {
                // Последняя страница: сохраняем факт онбординга и переходим к анкете профиля
                prefs.onboardingCompleted = true
                findNavController().navigate(R.id.action_onboardingFragment_to_profileFragment)
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonText(position)
            }
        })
    }

    private fun updateButtonText(position: Int) {
        val isLast = position == adapter.itemCount - 1
        nextButton.text = if (isLast) {
            getString(R.string.onboarding_continue)
        } else {
            getString(R.string.onboarding_next)
        }
    }
}
