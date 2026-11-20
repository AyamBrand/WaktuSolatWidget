package my.abi.waktusolat

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.Executors

class PrayerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        updateViews(context, manager, ids, PrayerRepository.cached(context))
        refresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) refresh(context)
    }

    companion object {
        const val ACTION_REFRESH = "my.abi.waktusolat.REFRESH"
        private val executor = Executors.newSingleThreadExecutor()
        private val MY_ZONE = ZoneId.of("Asia/Kuala_Lumpur")

        fun refresh(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            val ids = manager.getAppWidgetIds(ComponentName(app, PrayerWidgetProvider::class.java))
            if (ids.isEmpty()) return
            updateViews(app, manager, ids, PrayerRepository.cached(app))
            executor.execute {
                val schedule = runCatching { PrayerRepository.fetch(app) }.getOrNull()
                if (schedule != null) updateViews(app, manager, ids, schedule)
            }
        }

        private fun updateViews(context: Context, manager: AppWidgetManager, ids: IntArray, schedule: PrayerSchedule?) {
            val now = System.currentTimeMillis()
            val views = RemoteViews(context.packageName, R.layout.prayer_widget)
            views.setTextViewText(R.id.widget_date, PrayerRepository.dateMalay())
            views.setTextViewText(R.id.widget_location, "${PrayerRepository.zone(context)}  ·  DATA JAKIM")
            val ramadanDate = LocalDate.of(2027, 2, 8)
            val daysToRamadan = ChronoUnit.DAYS.between(LocalDate.now(MY_ZONE), ramadanDate)
            val ramadanCountdown = when {
                daysToRamadan > 0 -> "$daysToRamadan hari lagi!"
                daysToRamadan == 0L -> "HARI INI!"
                else -> "TELAH BERMULA"
            }
            views.setTextViewText(R.id.widget_ramadan_date, "8 FEBRUARI 2027 = ")
            views.setTextViewText(R.id.widget_ramadan_days, ramadanCountdown)
            val timeParts = DateTimeFormatter.ofPattern("a", Locale.ENGLISH).withZone(MY_ZONE).format(java.time.Instant.ofEpochMilli(now))
            val use24Hour = android.text.format.DateFormat.is24HourFormat(context)
            views.setTextViewText(R.id.widget_ampm, if (use24Hour) "" else timeParts.lowercase(Locale.ROOT))
            views.setViewVisibility(R.id.widget_ampm, if (use24Hour) android.view.View.GONE else android.view.View.VISIBLE)
            val battery = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = battery?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = battery?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, 100) ?: 100
            views.setTextViewText(R.id.widget_battery, if (level >= 0 && scale > 0) "${level * 100 / scale}%" else "")
            val day = schedule?.day(LocalDate.now(MY_ZONE))
            if (day != null) {
                views.setTextViewText(R.id.widget_hijri, PrayerRepository.hijriMalay(day.hijri))
                val next = PrayerRepository.nextPrayer(schedule, now)
                views.setTextViewText(R.id.widget_next_name, next?.first ?: "—")
                views.setTextViewText(R.id.widget_next_time, next?.let { PrayerRepository.time(it.second / 1000L) } ?: "—")
                if (next != null) {
                    val remaining = (next.second - now).coerceAtLeast(0L)
                    views.setViewVisibility(
                        R.id.widget_countdown_hour_prefix,
                        if (remaining < 3_600_000L) android.view.View.VISIBLE else android.view.View.GONE
                    )
                    views.setChronometerCountDown(R.id.widget_countdown, true)
                    views.setChronometer(
                        R.id.widget_countdown,
                        SystemClock.elapsedRealtime() + remaining,
                        "%s",
                        true
                    )
                } else {
                    views.setViewVisibility(R.id.widget_countdown_hour_prefix, android.view.View.GONE)
                    views.setTextViewText(R.id.widget_countdown, "—")
                }
                val values = PrayerRepository.prayerValues(day)
                val idsByKey = listOf(R.id.time_fajr, R.id.time_syuruk, R.id.time_dhuhr, R.id.time_asr, R.id.time_maghrib, R.id.time_isha)
                val nameIds = listOf(R.id.name_fajr, R.id.name_syuruk, R.id.name_dhuhr, R.id.name_asr, R.id.name_maghrib, R.id.name_isha)
                values.forEachIndexed { index, (label, value) ->
                    views.setTextViewText(idsByKey[index], value)
                    val active = next?.first == label
                    views.setTextColor(nameIds[index], if (active) 0xFFD6B46B.toInt() else 0xFF929292.toInt())
                    views.setTextColor(idsByKey[index], if (active) 0xFFD6B46B.toInt() else 0xFFE8E8E8.toInt())
                }
            } else {
                views.setTextViewText(R.id.widget_hijri, "Buka aplikasi untuk memuatkan jadual")
                views.setTextViewText(R.id.widget_next_name, "—")
                views.setTextViewText(R.id.widget_next_time, "—")
                views.setViewVisibility(R.id.widget_countdown_hour_prefix, android.view.View.GONE)
                views.setTextViewText(R.id.widget_countdown, "—")
            }
            val open = PendingIntent.getActivity(context, 1, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_root, open)
            val reload = PendingIntent.getBroadcast(context, 2, Intent(context, PrayerWidgetProvider::class.java).setAction(ACTION_REFRESH), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_location, reload)
            ids.forEach { manager.updateAppWidget(it, views) }
        }
    }
}
