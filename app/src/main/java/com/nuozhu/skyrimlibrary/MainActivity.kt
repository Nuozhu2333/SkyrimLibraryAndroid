package com.nuozhu.skyrimlibrary

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors
import com.google.android.material.snackbar.Snackbar
import com.nuozhu.skyrimlibrary.databinding.ActivityMainBinding
import com.nuozhu.skyrimlibrary.utils.BottomSheetDialogFragmentNotice
import org.json.JSONObject
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentDynamicColor: Boolean = true
    private var currentHomepageFab: String = "search"
    private lateinit var originalData: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        currentDynamicColor = sharedPref.getBoolean("dynamic_color", true)
        currentHomepageFab = sharedPref.getString("homepage_fab", resources.getStringArray(R.array.homepage_fab_value)[0]).toString()

        useDynamicColor()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        when (sharedPref.getString("homepage_fab", resources.getStringArray(R.array.homepage_fab_value)[0])) {
            resources.getStringArray(R.array.homepage_fab_value)[0] -> {
                binding.fab.setImageResource(R.drawable.search)
                binding.fab.visibility = View.VISIBLE
            }
            resources.getStringArray(R.array.homepage_fab_value)[1] -> {
                binding.fab.setImageResource(R.drawable.random)
                binding.fab.visibility = View.VISIBLE
            }
            resources.getStringArray(R.array.homepage_fab_value)[2] -> {
                binding.fab.visibility = View.GONE
            }
        }

        binding.fab.setOnClickListener {
            when (sharedPref.getString("homepage_fab", resources.getStringArray(R.array.homepage_fab_value)[0])) {
                resources.getStringArray(R.array.homepage_fab_value)[0] -> {
                    val intent = Intent()
                    intent.setClass(this, SearchActivity::class.java)
                    startActivity(intent)
                }
                resources.getStringArray(R.array.homepage_fab_value)[1] -> {
                    getRandomBook()
                }
            }
        }

        originalData = loadBooks()

        getRandomBook()
        binding.recentBook.text = sharedPref.getString("recent_book", getString(R.string.no_recent))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.random.tooltipText = getString(R.string.random_book)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.recent.tooltipText = getString(R.string.recent_book)
        }
        binding.cardviewRandomBook.setOnClickListener {
            openSelectedBook()
        }
        binding.cardviewRecentBook.setOnClickListener {
            openRecentBook()
        }
        binding.random.setOnClickListener {
            getRandomBook()
        }
        binding.recent.setOnClickListener {
            openRecentBook()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                val intent = Intent()
                intent.setClass(this, SearchActivity::class.java)
                startActivity(intent)
            }

            R.id.settings -> {
                val intent = Intent()
                intent.setClass(this, SettingsActivity::class.java)
                startActivity(intent)
            }

            R.id.notice -> {
                val bottomSheet = BottomSheetDialogFragmentNotice()
                bottomSheet.show(supportFragmentManager, bottomSheet.tag)
            }

            R.id.exit -> {
                finish()
            }
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        if (menu.javaClass.simpleName.equals("MenuBuilder", ignoreCase = true)) {
            try {
                val method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible",
                    java.lang.Boolean.TYPE
                )
                method.isAccessible = true
                method.invoke(menu, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return super.onMenuOpened(featureId, menu)
    }

    private fun getRandomBook() {
        if (::originalData.isInitialized && originalData.isNotEmpty()) {
            binding.randomBook.text = originalData.random()
        } else {
            binding.randomBook.text = getString(R.string.no_books_available)
        }
    }

    private fun openSelectedBook() {
        val intent = Intent()
        intent.putExtra("book_id", originalData.indexOf(binding.randomBook.text))
        intent.setClass(this, BooksActivity::class.java)
        startActivity(intent)
    }

    private fun openRecentBook() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val bookTitle = sharedPref.getString("recent_book", getString(R.string.no_recent))
        if (bookTitle != getString(R.string.no_recent) && bookTitle in originalData) {
            val intent = Intent()
            intent.putExtra("book_id", originalData.indexOf(bookTitle))
            intent.setClass(this, BooksActivity::class.java)
            startActivity(intent)
        } else {
            Snackbar.make(binding.snackbarRoot, R.string.no_recent, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun loadBooks(): List<String> {
        val customBooksFile = File(filesDir, "books.json")
        if (customBooksFile.exists()) {
            try {
                val jsonString = customBooksFile.readText()
                val titles = parseBooksFromJson(jsonString)
                if (titles.isNotEmpty()) {
                    return titles
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return loadBooksFromAssets()
    }

    private fun loadBooksFromAssets(): List<String> {
        return try {
            val jsonString = assets.open("books.json").bufferedReader().use { it.readText() }
            parseBooksFromJson(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseBooksFromJson(jsonString: String): List<String> {
        return try {
            val jsonObject = JSONObject(jsonString)
            val titles = mutableListOf<String>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val book = jsonObject.getJSONObject(key)
                val title = book.optString("title", "")
                if (title.isNotEmpty()) {
                    titles.add(title)
                }
            }
            titles
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun useDynamicColor() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("dynamic_color", true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this)
        }
    }

    override fun onRestart() {
        super.onRestart()
        checkDynamicColorChange()
        checkFabChange()
        // 检查书籍数据是否有更新（自定义文件可能被修改）
        reloadBooksIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        getRandomBook()
        binding.recentBook.text = PreferenceManager.getDefaultSharedPreferences(this).getString("recent_book", getString(R.string.no_recent))
    }

    private fun checkDynamicColorChange() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val newDynamicColor = sharedPref.getBoolean("dynamic_color", true)

        if (newDynamicColor != currentDynamicColor) {
            currentDynamicColor = newDynamicColor
            recreate()
        }
    }

    private fun checkFabChange() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val newHomepageFab = sharedPref.getString("homepage_fab", "search")

        if (newHomepageFab != currentHomepageFab) {
            if (newHomepageFab != null) {
                currentHomepageFab = newHomepageFab
                when (newHomepageFab) {
                    "search" -> {
                        binding.fab.setImageResource(R.drawable.search)
                        binding.fab.visibility = View.VISIBLE
                    }
                    "random_book" -> {
                        binding.fab.setImageResource(R.drawable.random)
                        binding.fab.visibility = View.VISIBLE
                    }
                    "disabled" -> {
                        binding.fab.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun reloadBooksIfNeeded() {
        val customBooksFile = File(filesDir, "books.json")
        if (customBooksFile.exists()) {
            try {
                val jsonString = customBooksFile.readText()
                val titles = parseBooksFromJson(jsonString)
                if (titles.isNotEmpty() && titles != originalData) {
                    originalData = titles
                    getRandomBook()
                    Snackbar.make(binding.snackbarRoot, R.string.book_list_updated, Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else  {
            originalData = loadBooksFromAssets()
            getRandomBook()
        }
    }
}