package com.ducktapedapps.updoot.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.FragmentSettingsBinding
import com.ducktapedapps.updoot.utils.ThemeType.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private val viewModel: SettingsVM by viewModels()

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        initListeners()
        observeVM()
        return binding.root
    }

    private fun initListeners() {
        binding.apply {
            singleThreadIndicatorSwitch.setOnClickListener { viewModel.toggleSingleThreadIndicator() }
            singleColorThreadIndicatorSwitch.setOnClickListener { viewModel.toggleSingleThreadColor() }
            themeSpinner.apply {
                adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,
                        listOf(
                                getString(R.string.light_theme),
                                getString(R.string.dark_theme),
                                getString(R.string.follow_system),
                        )
                )
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        viewModel.setTheme(when (position) {
                            0 -> LIGHT
                            1 -> DARK
                            else -> AUTO
                        })
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}

                }

            }
        }
    }

    private fun observeVM() {
        viewModel.apply {
            theme.asLiveData().observe(viewLifecycleOwner, {
                binding.themeSubText.text = when (it) {
                    DARK -> getString(R.string.dark_theme)
                    LIGHT -> getString(R.string.light_theme)
                    null, AUTO -> getString(R.string.follow_system)
                }
            })
            showSingleColorThread.asLiveData().observe(viewLifecycleOwner, {
                binding.apply {
                    singleColorThreadIndicatorSubText.text = getString(
                            if (it) R.string.using_single_color_thread
                            else R.string.using_multiple_color_thread
                    )
                    singleColorThreadIndicatorSwitch.isChecked = it
                }
            })
            showSingleThreadIndicator.asLiveData().observe(viewLifecycleOwner, {
                binding.apply {
                    singleThreadIndicatorSubText.text = getString(
                            if (it) R.string.using_single_thread
                            else R.string.using_multiple_threads
                    )
                    singleThreadIndicatorSwitch.isChecked = it
                }
            })
        }
    }
}