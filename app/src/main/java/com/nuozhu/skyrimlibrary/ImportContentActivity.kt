package com.nuozhu.skyrimlibrary

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nuozhu.skyrimlibrary.databinding.ActivityImportContentBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ImportContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportContentBinding

    private val selectFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                val uri = result.data?.data
                if (uri != null) {
                    handleSelectedFile(uri)
                } else {
                    Snackbar.make(binding.snackbarRoot, R.string.action_failed, Snackbar.LENGTH_SHORT).show()
                }
            }
            RESULT_CANCELED -> {
                Snackbar.make(binding.snackbarRoot, R.string.action_cancelled, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        useDynamicColor()

        binding = ActivityImportContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.importContent.setOnClickListener {
            selectJsonFile()
        }

        binding.importTestBooks.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.import_test_books)
                .setMessage(R.string.test_books_des)
                .setPositiveButton(android.R.string.ok) {
                    _,_ ->
                    importTestBooks()
                }
                .setNegativeButton(android.R.string.cancel) {_,_ ->}
                .show()
        }
    }

    private fun selectJsonFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/json"))
        }
        selectFileLauncher.launch(intent)
    }

    private fun handleSelectedFile(uri: Uri) {
        try {
            val validationResult = isValidJsonFile(uri)
            if (!validationResult.isValid) {
                Snackbar.make(
                    binding.snackbarRoot,
                    validationResult.message ?: getString(R.string.action_failed),
                    Snackbar.LENGTH_SHORT
                ).show()
                return
            }

            saveFileToPrivateDirectory(uri)

            Snackbar.make(
                binding.snackbarRoot,
                R.string.import_succeed,
                Snackbar.LENGTH_SHORT
            ).show()

            getDefaultSharedPreferences(this).edit {
                remove("recent_book")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(
                binding.snackbarRoot,
                R.string.action_failed,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveFileToPrivateDirectory(uri: Uri) {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            inputStream = contentResolver.openInputStream(uri)
                ?: throw Exception("无法打开文件输入流")

            val destinationFile = File(filesDir, "books.json")

            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            outputStream = FileOutputStream(destinationFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
        } catch (e: Exception) {
            throw Exception("保存文件失败: ${e.message}", e)
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    private fun isValidJsonFile(uri: Uri): ValidationResult {
        var inputStream: InputStream? = null
        return try {
            inputStream = contentResolver.openInputStream(uri)
            val content = inputStream?.bufferedReader()?.readText() ?: return ValidationResult(false, "文件内容为空")

            // 尝试解析JSON
            val jsonObject = JSONObject(content)

            // 检查是否为空对象
            if (jsonObject.length() == 0) {
                return ValidationResult(false, getString(R.string.empty_json))
            }

            // 获取所有键
            val keys = jsonObject.keys()
            var bookCount = 0

            while (keys.hasNext()) {
                val key = keys.next()
                // 检查键是否为数字字符串
                try {
                    key.toInt()
                } catch (_: NumberFormatException) {
                    return ValidationResult(false, getString(R.string.invalid_book_index, key))
                }

                val book = jsonObject.getJSONObject(key)
                bookCount++

                // 只检查必需的字段
                val hasTitle = book.has("title") && !book.isNull("title")
                val hasContent = book.has("content") && !book.isNull("content")

                if (!hasTitle || !hasContent) {
                    return ValidationResult(false, getString(R.string.book_missing_fields, key))
                }
            }

            if (bookCount == 0) {
                return ValidationResult(false, getString(R.string.no_books_available))
            }

            ValidationResult(true, null)

        } catch (_: JSONException) {
            ValidationResult(false, getString(R.string.invalid_json))
        } catch (_: Exception) {
            ValidationResult(false, getString(R.string.unknown_error_occurred))
        } finally {
            inputStream?.close()
        }
    }

    private data class ValidationResult(val isValid: Boolean, val message: String?)

    private fun importTestBooks() {
        try {
            val inputStream = assets.open("test.json")
            val destinationFile = File(filesDir, "books.json")

            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            FileOutputStream(destinationFile).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()
            }
            inputStream.close()

            getDefaultSharedPreferences(this).edit {
                remove("recent_book")
            }

            Snackbar.make(
                binding.snackbarRoot,
                R.string.import_succeed,
                Snackbar.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(
                binding.snackbarRoot,
                R.string.action_failed,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun deleteImportedBooks() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_imported_books)
            .setMessage(R.string.delete_imported_books_confirm)
            .setPositiveButton(R.string.delete) { _, _ ->
                if (File(filesDir, "books.json").exists()) {
                    deleteFile("books.json")
                }
                getDefaultSharedPreferences(this).edit {
                    remove("recent_book")
                }
                Snackbar.make(binding.snackbarRoot, R.string.deleted, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
            }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_import_content, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_imported_books -> {
                deleteImportedBooks()
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

    private fun useDynamicColor() {
        val sharedPreferences = getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("dynamic_color", true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this)
        }
    }
}