package com.nuozhu.skyrimlibrary

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nuozhu.skyrimlibrary.databinding.ActivityBooksBinding
import com.nuozhu.skyrimlibrary.utils.TypefaceSpan
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.regex.Pattern

class BooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBooksBinding
    val sizeMap = mapOf(
        0 to 12f,
        1 to 14f,
        2 to 16f,
        3 to 18f,
        4 to 20f
    )

    // 缓存书籍标题列表
    private lateinit var bookTitles: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("immersive_mode", true)){
            setTheme(R.style.Theme_天际书库_Immersive)
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            setTheme(R.style.Theme_天际书库)
        }

        super.onCreate(savedInstanceState)

        useDynamicColor()

        binding = ActivityBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (sharedPreferences.getBoolean("immersive_mode", true)) {
            binding.root.fitsSystemWindows = false
        } else {
            binding.root.fitsSystemWindows = true
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // 获取传入的书籍ID
        val bookId = intent.getIntExtra("book_id", 0)

        // 加载书籍标题列表
        bookTitles = loadBooks()

        // 加载并显示书籍
        loadBookFromJson(bookId)

        val fontSize = PreferenceManager.getDefaultSharedPreferences(this).getFloat("font_size", 16f)

        setTextSize(fontSize)
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

    private fun loadBookFromJson(bookId: Int) {
        try {
            val jsonString = loadBookJsonString()
            val jsonObject = JSONObject(jsonString)

            // 获取对应ID的书籍数据
            val bookData = jsonObject.optJSONObject(bookId.toString())

            if (bookData != null) {
                val title = bookData.optString("title", "")
                val author = bookData.optString("author", "")
                val language = bookData.optString("language", "")
                val description = bookData.optString("description", "")
                val content = bookData.optString("content", "")

                binding.title.text = title
                binding.author.text = author
                binding.language.text = language
                binding.description.text = description

                // 保存最近阅读的书籍名称到SharedPreferences
                saveRecentBook(title)

                // 如果有内容，使用格式化显示
                if (content.isNotEmpty()) {
                    displayFormattedContent(content)
                } else {
                    Toast.makeText(this, R.string.no_books_available, Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, R.string.no_books_available, Toast.LENGTH_SHORT).show()
                finish()
            }

        } catch (_: IOException) {
            Toast.makeText(this, R.string.no_books_available, Toast.LENGTH_SHORT).show()
            finish()
        } catch (_: Exception) {
            Toast.makeText(this, R.string.no_books_available, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadBookJsonString(): String {
        val customBooksFile = File(filesDir, "books.json")
        if (customBooksFile.exists()) {
            try {
                return customBooksFile.readText()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return assets.open("books.json").bufferedReader().use { it.readText() }
    }

    private fun saveRecentBook(bookTitle: String) {
        if (bookTitle.isNotEmpty()) {
            PreferenceManager.getDefaultSharedPreferences(this).edit {
                putString("recent_book", bookTitle)
            }
        }
    }

    private fun displayFormattedContent(content: String) {
        val spannableString = SpannableStringBuilder()
        // 别名映射
        val aliasMap = mapOf(
            "<alias=Player>" to android.preference.PreferenceManager.getDefaultSharedPreferences(this).getString("username", getString(R.string.default_username)).toString(),
            "<alias=Dungeon>" to getString(R.string.dungeon_name),
            "<alias=Location>" to getString(R.string.location_name),
            "<alias=Item>" to getString(R.string.item_name),
            "<alias=NPC>" to getString(R.string.NPC_name),
            "<alias.Pronoun=Player>" to getString(R.string.Pronoun),
            "<alias.PronounCap=Player>" to getString(R.string.Pronoun),
            "<alias.PronounPosObj=Player>" to getString(R.string.PronounPosObj),
            "<alias.PronounObj=Player>" to getString(R.string.PronounObj),
            "<alias.PronounRef=Player>" to getString(R.string.PronounRef),
            "<alias.Race=Player>" to getString(R.string.player_race),
            "<alias.Gender=Player>" to getString(R.string.player_gender),
        )
        // 字体映射
        val fontMap = mapOf(
            "daedrascript" to "daedrascript.ttf",
            "dragonscript" to "dragonscript.ttf",
            "dwemerscript" to "dwemerscript.ttf",
            "falmerscript" to "falmerscript.ttf"
        )

        var processedContent = content
        aliasMap.forEach { (alias, replacement) ->
            processedContent = processedContent.replace(alias, replacement)
        }
        // 正则表达式匹配 <font="字体名">文本</font>
        val pattern = Pattern.compile("""<font="([^"]+)">(.*?)</font>""")
        val matcher = pattern.matcher(processedContent)

        var lastIndex = 0

        // 缓存已加载的字体，避免重复加载
        val fontCache = mutableMapOf<String, Typeface>()

        while (matcher.find()) {
            val fontName = matcher.group(1)!!
            val customText = matcher.group(2)!!

            // 添加普通文本
            val beforeText = processedContent.substring(lastIndex, matcher.start())
            spannableString.append(beforeText)

            // 从映射表中获取字体文件名，如果不存在则使用原始名称
            val fontFileName = fontMap[fontName] ?: "$fontName.ttf"

            // 获取或加载字体
            val typeface = fontCache.getOrPut(fontName) {
                try {
                    Typeface.createFromAsset(assets, fontFileName)
                } catch (_: Exception) {
                    // 如果字体文件不存在，使用默认字体
                    Typeface.DEFAULT
                }
            }

            // 添加自定义字体文本
            val start = spannableString.length
            spannableString.append(customText)
            val end = spannableString.length

            // 应用自定义字体
            val customSpan = TypefaceSpan(typeface)
            spannableString.setSpan(customSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            lastIndex = matcher.end()
        }

        // 添加剩余文本
        if (lastIndex < processedContent.length) {
            spannableString.append(processedContent.substring(lastIndex))
        }

        binding.content.text = spannableString
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_books, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.info -> {
                val charCount = binding.content.text?.length ?: 0.toString()
                val bookInfo =
                    getString(R.string.book_info_title, binding.title.text) + "\n" +
                            getString(R.string.book_info_author, binding.author.text) +"\n" +
                            getString(R.string.book_info_language, binding.language.text) +"\n" +
                            getString(R.string.book_info_character_count, charCount) +"\n" +
                            getString(R.string.book_info_description, binding.description.text)
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.book_info)
                    .setMessage(bookInfo)
                    .setNegativeButton(android.R.string.ok) {
                            _, _ ->
                    }
                    .show()
            }

            R.id.previous_book -> {
                val books = bookTitles
                val currentIndex = books.indexOf(binding.title.text.toString())
                if (currentIndex <= 0) {
                    Snackbar.make(binding.snackbarRoot, R.string.no_books_available, Snackbar.LENGTH_SHORT).show()
                } else {
                    loadBookByTitle(books[currentIndex - 1])
                }
            }

            R.id.next_book -> {
                val books = bookTitles
                val currentIndex = books.indexOf(binding.title.text.toString())
                if (currentIndex == -1 || currentIndex >= books.size - 1) {
                    Snackbar.make(binding.snackbarRoot, R.string.no_books_available, Snackbar.LENGTH_SHORT).show()
                } else {
                    loadBookByTitle(books[currentIndex + 1])
                }
            }

            R.id.font_size -> {
                val fontSize = PreferenceManager.getDefaultSharedPreferences(this).getFloat("font_size",16f)
                val inflater = LayoutInflater.from(this)
                val dialogView = inflater.inflate(R.layout.dialog_seekbar, null)

                val seekBar = dialogView.findViewById<SeekBar>(R.id.seekBar)
                val progress = sizeMap.entries.associate { (key, value) -> value to key }[fontSize] ?: 2
                seekBar.progress = progress
                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        val fontSize = sizeMap[progress] ?: 16f
                        setTextSize(fontSize)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.font_size)
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok) {
                            _, _ ->
                        updateTextSize(seekBar.progress)
                    }
                    .setNegativeButton(android.R.string.cancel) {
                            _, _ ->
                        val progress = sizeMap.entries.associate { (key, value) -> value to key }[fontSize] ?: 2
                        updateTextSize(progress)
                    }
                    .setNeutralButton(R.string.restore_to_default) {
                            _, _ ->
                        updateTextSize(2)
                    }
                    .show()
            }

            R.id.share -> {
                ShareCompat.IntentBuilder(this)
                    .setType("text/plain")
                    .setText(binding.content.text.toString())
                    .startChooser()
            }
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadBookByTitle(title: String) {
        try {
            val jsonString = loadBookJsonString()
            val jsonObject = JSONObject(jsonString)

            // 遍历查找匹配标题的书籍ID
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val book = jsonObject.getJSONObject(key)
                if (book.optString("title", "") == title) {
                    loadBookFromJson(key.toInt())
                    return
                }
            }

            Toast.makeText(this, R.string.no_books_available, Toast.LENGTH_SHORT).show()
            finish()
        } catch (_: Exception) {
            Toast.makeText(this, R.string.no_books_available, Toast.LENGTH_SHORT).show()
            finish()
        }
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

    private fun setTextSize(fontSize: Float) {
        binding.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize + 6f)
        binding.author.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
        binding.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
    }

    private fun updateTextSize(progress: Int) {
        val fontSize = sizeMap[progress] ?: 16f
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putFloat(
                "font_size",
                fontSize
            )
        }
        setTextSize(fontSize)
    }

    private fun useDynamicColor() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("dynamic_color", true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this)
        }
    }
}