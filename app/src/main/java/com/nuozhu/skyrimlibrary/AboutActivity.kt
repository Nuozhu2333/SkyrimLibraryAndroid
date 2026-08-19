package com.nuozhu.skyrimlibrary

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.nuozhu.skyrimlibrary.databinding.ActivityAboutBinding
import com.nuozhu.skyrimlibrary.utils.BottomSheetDialogFragmentUpdateLog

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        useDynamicColor()

        binding = ActivityAboutBinding.inflate(layoutInflater)
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
                .replace(R.id.about, AboutFragment())
                .commit()
        }
    }

    class AboutFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.about, rootKey)
            val preferenceDownload: Preference = findPreference("download")!!
            preferenceDownload.setOnPreferenceClickListener {
                val intent = CustomTabsIntent.Builder().build()
                intent.launchUrl(requireContext(), "https://nuozhu2333.github.io/apps/SkyrimLibrary.html".toUri())
                return@setOnPreferenceClickListener true
            }

            val preferenceUpdateLog: Preference = findPreference("update_log")!!
            preferenceUpdateLog.setOnPreferenceClickListener {
                val bottomSheetFragment = BottomSheetDialogFragmentUpdateLog()
                bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
                return@setOnPreferenceClickListener true
            }

            val preferenceReferences: Preference = findPreference("references")!!
            preferenceReferences.setOnPreferenceClickListener {
                return@setOnPreferenceClickListener false
            }
        }
    }

    private fun useDynamicColor() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("dynamic_color", true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this)
        }
    }
}
