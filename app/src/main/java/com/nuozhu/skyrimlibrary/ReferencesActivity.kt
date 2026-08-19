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
import com.nuozhu.skyrimlibrary.databinding.ActivityReferencesBinding

class ReferencesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReferencesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        useDynamicColor()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this)
        }

        binding = ActivityReferencesBinding.inflate(layoutInflater)
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
                .replace(R.id.references, ReferencesFragment())
                .commit()
        }
    }

    class ReferencesFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.references, rootKey)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            val url = when (preference.key) {
                "bilibili_wiki" -> "https://wiki.biligame.com/skyrim/%E5%9B%BE%E4%B9%A6"
                "uesp" -> "https://en.uesp.net/wiki/Skyrim:Books"
                else -> return super.onPreferenceTreeClick(preference)
            }
            CustomTabsIntent.Builder().build().launchUrl(requireContext(), url.toUri())
            return true
        }
    }

    private fun useDynamicColor() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("dynamic_color", true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this)
        }
    }
}