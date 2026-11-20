package my.abi.waktusolat

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var content: LinearLayout
    private lateinit var zoneInput: EditText
    private val bg = Color.rgb(8, 8, 8)
    private val white = Color.rgb(245, 245, 245)
    private val muted = Color.rgb(157, 157, 157)
    private val gold = Color.rgb(214, 180, 107)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg; window.navigationBarColor = bg
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(22), dp(22), dp(22), dp(24)); setBackgroundColor(bg) }
        setContentView(root)
        root.addView(text("WAKTU SOLAT", 13f, gold, true))
        root.addView(text("Waktu solat harian", 25f, white, true))
        root.addView(text("Widget ringkas untuk skrin utama dan peranti yang menyokong widget skrin kunci.", 14f, muted, false), margin(0, 4, 0, 18))
        val zoneRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        zoneInput = EditText(this).apply {
            setSingleLine(true); setTextColor(white); setHintTextColor(muted); hint = "Kod zon JAKIM (cth. WLY01)"
            setText(PrayerRepository.zone(this@MainActivity)); textSize = 15f; setPadding(dp(14), 0, dp(14), 0)
            background = rounded(Color.rgb(28,28,28), dp(14))
        }
        zoneRow.addView(zoneInput, LinearLayout.LayoutParams(0, dp(52), 1f))
        val save = Button(this).apply { text = "Simpan"; setTextColor(Color.BLACK); setBackgroundTintList(android.content.res.ColorStateList.valueOf(gold)) }
        zoneRow.addView(save, margin(10, 0, 0, 0)); root.addView(zoneRow)
        val zoneHelp = text("Tak tahu kod zon? Lihat Jadual Kod Zon JAKIM", 13f, gold, false).apply {
            paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            minHeight = dp(44)
            gravity = Gravity.CENTER_VERTICAL
            setOnClickListener {
                val url = "https://www.e-solat.gov.my/index.php?siteId=24&pageId=50"
                startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
            }
        }
        root.addView(zoneHelp)
        save.setOnClickListener {
            val zone = zoneInput.text.toString().trim().uppercase()
            if (zone.matches(Regex("[A-Z0-9]{3,8}"))) { PrayerRepository.setZone(this, zone); refresh(true) }
            else showMessage("Masukkan kod zon yang sah, contohnya WLY01.")
        }
        val refreshButton = Button(this).apply { text = "Kemas kini waktu solat"; setTextColor(white); setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(47,47,47))) }
        root.addView(refreshButton, margin(0, 10, 0, 10)); refreshButton.setOnClickListener { refresh(true) }
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        val credit = "Widget ini dibangunkan dari ❤️ oleh Muktasyaf AnNamir\nmenggunakan Manus 2.0 Lite."
        val creditSpannable = SpannableString(credit)
        fun addCreditLink(label: String, url: String) {
            val start = credit.indexOf(label)
            val end = start + label.length
            creditSpannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                }
                override fun updateDrawState(ds: TextPaint) {
                    ds.color = Color.WHITE
                    ds.isUnderlineText = true
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        addCreditLink("Muktasyaf AnNamir", "https://mylink.la/annamir")
        addCreditLink("Manus 2.0 Lite", "https://manus.im")
        val creditView = TextView(this).apply {
            text = creditSpannable
            setTextColor(Color.WHITE)
            textSize = 12f
            gravity = Gravity.CENTER
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
            setPadding(0, dp(12), 0, 0)
        }
        root.addView(creditView)
        root.addView(text("Sumber jadual: API Waktu Solat Malaysia (data e-Solat JAKIM). Sambungan internet diperlukan untuk kemas kini; jadual terakhir disimpan pada telefon.", 12f, muted, false), margin(0, 18, 0, 0))
        showSchedule(PrayerRepository.cached(this))
        refresh(false)
    }

    private fun refresh(showBusy: Boolean) {
        if (showBusy && ::content.isInitialized) showMessage("Mengambil jadual waktu solat…")
        executor.execute {
            try {
                val schedule = PrayerRepository.fetch(this)
                runOnUiThread {
                    showSchedule(schedule)
                    val manager = AppWidgetManager.getInstance(this)
                    val ids = manager.getAppWidgetIds(ComponentName(this, PrayerWidgetProvider::class.java))
                    if (ids.isNotEmpty()) PrayerWidgetProvider.refresh(this)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showSchedule(PrayerRepository.cached(this))
                    if (showBusy) showMessage("Tidak dapat mengemas kini. ${e.message ?: "Semak internet atau kod zon."}")
                }
            }
        }
    }

    private fun showSchedule(schedule: PrayerSchedule?) {
        content.removeAllViews()
        if (schedule == null) {
            content.addView(text("Belum ada data. Sambungkan internet dan tekan Kemas kini.", 16f, muted, false))
            return
        }
        val day = schedule.day()
        if (day == null) {
            content.addView(text("Jadual untuk hari ini belum tersedia.", 16f, muted, false)); return
        }
        val now = System.currentTimeMillis()
        val next = PrayerRepository.nextPrayer(schedule, now)
        val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(18), dp(18), dp(18)); background = rounded(Color.rgb(20,20,20), dp(22)) }
        card.addView(text(PrayerRepository.dateMalay(), 14f, muted, false))
        card.addView(text("${schedule.zone}  ·  ${PrayerRepository.hijriMalay(day.hijri)}", 13f, muted, false), margin(0, 6, 0, 14))
        card.addView(text("SOLAT SETERUSNYA", 12f, gold, true))
        card.addView(text(next?.first ?: "ISYAK", 28f, gold, true))
        card.addView(text(next?.let { PrayerRepository.time(it.second / 1000) } ?: "—", 20f, white, false))
        val left = next?.second?.minus(now)?.coerceAtLeast(0L) ?: 0L
        card.addView(text("${left / 3_600_000}j ${(left / 60_000) % 60}m lagi", 14f, muted, false), margin(0, 2, 0, 12))
        card.addView(divider())
        val values = PrayerRepository.prayerValues(day)
        for (row in values.chunked(2)) {
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.forEach { pair ->
                val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(12), dp(8), dp(12)) }
                col.addView(text(pair.first, 12f, if (next?.first == pair.first) gold else muted, true))
                col.addView(text(pair.second, 18f, white, false))
                line.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            }
            if (row.size == 1) line.addView(LinearLayout(this), LinearLayout.LayoutParams(0, 1, 1f))
            card.addView(line)
        }
        content.addView(card)
    }

    private fun showMessage(message: String) {
        if (!::content.isInitialized) return
        content.removeAllViews(); content.addView(text(message, 15f, muted, false))
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean): TextView = TextView(this).apply {
        text = value; textSize = size; setTextColor(color); if (bold) setTypeface(null, Typeface.BOLD)
    }
    private fun divider() = android.view.View(this).apply { setBackgroundColor(Color.rgb(60,60,60)); layoutParams = margin(0, 4, 0, 2).apply { height = dp(1) } }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = radius.toFloat() }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun margin(l: Int, t: Int, r: Int, b: Int) = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(l), dp(t), dp(r), dp(b)) }

    override fun onDestroy() { executor.shutdown(); super.onDestroy() }
}
