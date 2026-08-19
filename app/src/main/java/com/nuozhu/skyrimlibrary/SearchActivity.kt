package com.nuozhu.skyrimlibrary

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.android.material.color.DynamicColors
import com.nuozhu.skyrimlibrary.databinding.ActivitySearchBinding
import org.json.JSONObject
import java.io.File
import java.io.IOException

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var originalData: List<String>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        useDynamicColor()

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // 加载书籍数据（优先使用自定义文件）
        originalData = loadBooks()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, originalData)
        binding.listView.adapter = adapter
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterData(newText.orEmpty())
                return true
            }
        })

        binding.listView.setOnItemClickListener {parent: AdapterView<*>, _: View, position:Int, _:Long ->
            val bookData = parent.getItemAtPosition(position).toString()

            val intent = Intent(this, BooksActivity::class.java)
            intent.putExtra("book_id", originalData.indexOf(bookData))
            startActivity(intent)
        }
    }

    private fun loadBooks(): List<String> {
        val customBooksFile = File(filesDir, "books.json")
        return if (customBooksFile.exists()) {
            try {
                val jsonString = customBooksFile.readText()
                parseBooksFromJson(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } else {
            loadBooksFromAssets()
        }
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

    private fun filterData(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalData
        } else {
            originalData.filter { it.contains(query, ignoreCase = true) }
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filteredList)
        binding.listView.adapter = adapter
    }

    private fun useDynamicColor() {
        val sharedPreferences = getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("dynamic_color", true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this)
        }
    }
}