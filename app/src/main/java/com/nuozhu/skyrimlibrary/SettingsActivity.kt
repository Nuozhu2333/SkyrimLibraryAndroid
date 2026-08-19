package com.nuozhu.skyrimlibrary

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nuozhu.skyrimlibrary.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        username = getDefaultSharedPreferences(this).getString("username",getString(R.string.default_username)).toString()

        useDynamicColor()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var preferenceHomepageFab: Preference
        private lateinit var preferenceUsername: Preference

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings, rootKey)

            preferenceHomepageFab = findPreference("homepage_fab")!!
            updateHomepageFabSummary()

            preferenceHomepageFab.setOnPreferenceClickListener {
                val currentIndex = resources.getStringArray(R.array.homepage_fab_value)
                    .indexOf(
                        getDefaultSharedPreferences(requireContext())
                            .getString("homepage_fab", "search"))

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.homepage_fab)
                    .setSingleChoiceItems(R.array.homepage_fab, currentIndex) { dialog, which ->
                        getDefaultSharedPreferences(requireContext())
                            .edit {
                                putString(
                                    "homepage_fab",
                                    resources.getStringArray(R.array.homepage_fab_value)[which]
                                )
                            }
                        updateHomepageFabSummary()
                        dialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                return@setOnPreferenceClickListener false
            }

            val preferenceDynamicColor: Preference = findPreference("dynamic_color")!!
            val preferenceDynamicColorDes: Preference = findPreference("dynamic_color_des")!!
            preferenceDynamicColor.setOnPreferenceClickListener {
                preferenceDynamicColorDes.isVisible = getDefaultSharedPreferences(requireContext())
                    .getBoolean("dynamic_color", false)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        activity?.recreate() }, 1000)
                }
                true
            }

            preferenceDynamicColorDes.isVisible = getDefaultSharedPreferences(requireContext())
                .getBoolean("dynamic_color", false)

            preferenceUsername = findPreference("username")!!
            preferenceUsername.summary = getDefaultSharedPreferences(requireContext()).getString("username",getString(R.string.default_username)).toString()

        }
        private fun updateHomepageFabSummary() {
            val sharedPreferences = getDefaultSharedPreferences(requireContext())
            val currentValue = sharedPreferences.getString("homepage_fab", "search")
            val valueArray = resources.getStringArray(R.array.homepage_fab_value)
            val displayArray = resources.getStringArray(R.array.homepage_fab)
            val index = valueArray.indexOf(currentValue)
            preferenceHomepageFab.summary = if (index >= 0) {
                displayArray[index]
            } else {
                displayArray[0]
            }
            when (currentValue) {
                resources.getStringArray(R.array.homepage_fab_value)[0] -> {
                    preferenceHomepageFab.setIcon(R.drawable.search)
                }
                resources.getStringArray(R.array.homepage_fab_value)[1] -> {
                    preferenceHomepageFab.setIcon(R.drawable.random)
                }
                resources.getStringArray(R.array.homepage_fab_value)[2] -> {
                    preferenceHomepageFab.setIcon(android.R.drawable.screen_background_light_transparent)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        username = getDefaultSharedPreferences(this).getString("username",getString(R.string.default_username)).toString()
    }

    override fun onResume() {
        super.onResume()
        if (username!=getDefaultSharedPreferences(this).getString("username",getString(R.string.default_username)).toString()) {
            recreate()
        }
    }

    private fun useDynamicColor() {
        val sharedPreferences = getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("dynamic_color", true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this)
        }
    }
}