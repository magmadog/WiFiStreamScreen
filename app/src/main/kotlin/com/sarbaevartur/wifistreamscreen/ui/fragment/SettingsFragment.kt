package com.sarbaevartur.wifistreamscreen.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.sarbaevartur.wifistreamscreen.R
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vp_fragment_settings.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment =
                when (position) {
                    0 -> SettingsInterfaceFragment()
                    1 -> SettingsSecurityFragment()
                    else -> throw IllegalArgumentException("FragmentStateAdapter.getItem: unexpected position: $position")
                }
        }

        TabLayoutMediator(tl_fragment_settings, vp_fragment_settings) { tab, position ->
            tab.text = when (position) {
                0 -> requireContext().getString(R.string.pref_settings)
                1 -> requireContext().getString(R.string.pref_settings_security)
                else -> throw IllegalArgumentException("TabLayoutMediator: unexpected position: $position")
            }
        }.attach()
    }
}