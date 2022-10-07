package nz.ac.canterbury.guessit.controller

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import nz.ac.canterbury.guessit.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
