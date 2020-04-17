package com.ducktapedapps.updoot.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.utils.Constants

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
        val dropDownPreference: ListPreference? = findPreference(Constants.THEME_KEY)
        dropDownPreference?.setOnPreferenceChangeListener { _, newValue ->
            AppCompatDelegate.setDefaultNightMode(Integer.parseInt(newValue as String))
            true
        }
    }

}