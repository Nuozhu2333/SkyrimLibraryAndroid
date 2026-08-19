package com.nuozhu.skyrimlibrary

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.File
import java.io.IOException

class RecentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bookTitles = loadBooks()
        val recentBook = PreferenceManager.getDefaultSharedPreferences(this).getString("recent_book", getString(R.string.no_recent))

        val intent = Intent()
        intent.putExtra("book_id", bookTitles.indexOf(recentBook))
        intent.setClass(this, BooksActivity::class.java)
        startActivity(intent)

        finish()
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
}