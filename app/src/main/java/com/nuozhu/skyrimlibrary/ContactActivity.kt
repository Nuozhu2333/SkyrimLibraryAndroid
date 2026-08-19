package com.nuozhu.skyrimlibrary

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.nuozhu.skyrimlibrary.databinding.ActivityContactBinding

class ContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        useDynamicColor()

        binding = ActivityContactBinding.inflate(layoutInflater)
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
                .replace(R.id.contact, ContactFragment())
                .commit()
        }
    }

    class ContactFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.contact, rootKey)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            val url = when (preference.key) {
                "tieba" -> "https://tieba.baidu.com/home/main?un=糯竹2333"
                "bilibili" -> "https://space.bilibili.com/1021164668"
                "heybox" -> "https://www.xiaoheihe.cn/app/user/profile/63a46a876359"
                "tieba_sgjz" -> "https://tieba.baidu.com/f?kw=%E4%B8%8A%E5%8F%A4%E5%8D%B7%E8%BD%B4"
                "uesp" -> "https://en.uesp.net/wiki/Main_Page"
                "nexus" -> "https://www.nexusmods.com/"
                "thuum" -> "https://www.thuum.org/"
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
