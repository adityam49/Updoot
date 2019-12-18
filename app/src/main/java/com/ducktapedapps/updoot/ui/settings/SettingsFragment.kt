package com.ducktapedapps.updoot.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.ducktapedapps.updoot.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
    }

}