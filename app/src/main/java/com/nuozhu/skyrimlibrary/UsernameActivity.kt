package com.nuozhu.skyrimlibrary

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nuozhu.skyrimlibrary.databinding.ActivityUsernameBinding

class UsernameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsernameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        useDynamicColor()

        binding = ActivityUsernameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        binding.EditText.setText(sharedPreferences.getString("username", getString(R.string.default_username)))

        binding.apply.setOnClickListener {
            if (binding.EditText.text.toString()=="") {
                sharedPreferences.edit {
                    putString("username", getString(R.string.default_username))
                }
                finish()
            } else {
                sharedPreferences.edit {
                    putString("username", binding.EditText.text.toString())
                }
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_username, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.tips -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.tips)
                    .setMessage(R.string.username_tips)
                    .setNegativeButton(android.R.string.ok) { _,_ -> }
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun useDynamicColor() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("dynamic_color", true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this)
        }
    }
}