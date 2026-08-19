package com.nuozhu.skyrimlibrary

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.nuozhu.skyrimlibrary.databinding.ActivityFontPreviewBinding
import com.nuozhu.skyrimlibrary.utils.TypefaceSpan
import java.util.regex.Pattern

class FontPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFontPreviewBinding
    val sizeMap = mapOf(
        0 to 12f,
        1 to 14f,
        2 to 16f,
        3 to 18f,
        4 to 20f
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        useDynamicColor()

        binding = ActivityFontPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        val fontSize = PreferenceManager.getDefaultSharedPreferences(this).getFloat("font_size",16f)
        binding.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateTextSize(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val progress = sizeMap.entries.associate { (key, value) -> value to key }[fontSize] ?: 2
        binding.seekBar.progress = progress
        displayFormattedContent(getString(R.string.font_text_content))
    }

    private fun displayFormattedContent(content: String) {
        val spannableString = SpannableStringBuilder()

        //字体映射
        val fontMap = mapOf(
            "daedrascript" to "daedrascript.ttf",
            "dragonscript" to "dragonscript.ttf",
            "dwemerscript" to "dwemerscript.ttf",
            "falmerscript" to "falmerscript.ttf"
        )

        // 正则表达式匹配 <font="字体名">文本</font>
        val pattern = Pattern.compile("""<font="([^"]+)">(.*?)</font>""")
        val matcher = pattern.matcher(content)

        var lastIndex = 0

        // 缓存已加载的字体，避免重复加载
        val fontCache = mutableMapOf<String, Typeface>()

        while (matcher.find()) {
            val fontName = matcher.group(1)!!
            val customText = matcher.group(2)!!

            // 添加普通文本
            val beforeText = content.substring(lastIndex, matcher.start())
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
        if (lastIndex < content.length) {
            spannableString.append(content.substring(lastIndex))
        }

        binding.content.text = spannableString
    }

    private fun updateTextSize(progress: Int) {
        val fontSize = sizeMap[progress] ?: 16f
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putFloat(
                "font_size",
                fontSize
            )
        }
        binding.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
    }

    private fun useDynamicColor() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("dynamic_color", true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this)
        }
    }
}