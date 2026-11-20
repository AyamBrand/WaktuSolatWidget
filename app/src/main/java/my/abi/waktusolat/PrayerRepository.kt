package my.abi.waktusolat

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

internal data class PrayerDay(
    val date: LocalDate,
    val hijri: String,
    val fajr: Long,
    val syuruk: Long,
    val dhuhr: Long,
    val asr: Long,
    val maghrib: Long,
    val isha: Long
)

internal data class PrayerSchedule(val zone: String, val days: List<PrayerDay>) {
    fun day(date: LocalDate = LocalDate.now(ZoneId.of("Asia/Kuala_Lumpur"))): PrayerDay? = days.firstOrNull { it.date == date }
}

internal object PrayerRepository {
    private val MY_ZONE: ZoneId = ZoneId.of("Asia/Kuala_Lumpur")
    private val hijriMonths = listOf("Muharam", "Safar", "Rabiulawal", "Rabiulakhir", "Jamadilawal", "Jamadilakhir", "Rejab", "Syaaban", "Ramadan", "Syawal", "Zulkaedah", "Zulhijah")

    fun zone(context: Context): String = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        .getString("zone", "WLY01")?.uppercase(Locale.ROOT)?.trim()?.ifBlank { "WLY01" } ?: "WLY01"

    fun setZone(context: Context, value: String) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit()
            .putString("zone", value.uppercase(Locale.ROOT).trim()).apply()
    }

    fun fetch(context: Context, zone: String = zone(context)): PrayerSchedule {
        val current = LocalDate.now(MY_ZONE)
        val url = URL("https://api.waktusolat.app/v2/solat/${zone}?year=${current.year}&month=${current.monthValue}")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12000
            readTimeout = 12000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "WaktuSolatWidget/1.0 Android")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw IllegalStateException("Pelayan waktu solat memberi ralat ($code).")
            val json = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            val days = parse(root)
            if (days.isEmpty()) throw IllegalStateException("Jadual untuk zon $zone belum tersedia.")
            context.getSharedPreferences("cache", Context.MODE_PRIVATE).edit()
                .putString("${zone}_${current.year}_${current.monthValue}", json).apply()
            return PrayerSchedule(zone, days)
        } finally {
            connection.disconnect()
        }
    }

    fun cached(context: Context, zone: String = zone(context)): PrayerSchedule? {
        val current = LocalDate.now(MY_ZONE)
        val raw = context.getSharedPreferences("cache", Context.MODE_PRIVATE)
            .getString("${zone}_${current.year}_${current.monthValue}", null) ?: return null
        return runCatching { PrayerSchedule(zone, parse(JSONObject(raw))) }.getOrNull()
    }

    private fun parse(root: JSONObject): List<PrayerDay> {
        val zone = root.optString("zone", "WLY01")
        val year = root.optInt("year", LocalDate.now(MY_ZONE).year)
        val month = root.optInt("month_number", LocalDate.now(MY_ZONE).monthValue)
        val list = root.optJSONArray("prayers") ?: return emptyList()
        return (0 until list.length()).mapNotNull { index ->
            val item = list.optJSONObject(index) ?: return@mapNotNull null
            val date = runCatching { LocalDate.of(year, month, item.getInt("day")) }.getOrNull() ?: return@mapNotNull null
            PrayerDay(date, item.optString("hijri", ""), item.optLong("fajr"), item.optLong("syuruk"),
                item.optLong("dhuhr"), item.optLong("asr"), item.optLong("maghrib"), item.optLong("isha"))
        }
    }

    fun nextPrayer(schedule: PrayerSchedule, nowMillis: Long = System.currentTimeMillis()): Pair<String, Long>? {
        val today = LocalDate.now(MY_ZONE)
        val values = listOf("SUBUH" to "fajr", "ZOHOR" to "dhuhr", "ASAR" to "asr", "MAGHRIB" to "maghrib", "ISYAK" to "isha")
        val next = schedule.days.asSequence().filter { !it.date.isBefore(today) }.flatMap { day ->
            values.asSequence().mapNotNull { (label, key) ->
                val epoch = epoch(day, key)
                if (epoch > nowMillis) Triple(day.date, label, epoch) else null
            }
        }.minByOrNull { it.third } ?: return null
        return next.second to next.third
    }

    fun epoch(day: PrayerDay, key: String): Long = when (key) {
        "fajr" -> day.fajr; "syuruk" -> day.syuruk; "dhuhr" -> day.dhuhr
        "asr" -> day.asr; "maghrib" -> day.maghrib; else -> day.isha
    } * 1000L

    fun time(epochSeconds: Long): String = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        .withZone(MY_ZONE).format(Date(epochSeconds * 1000L).toInstant())

    fun timeOfDay(epochSeconds: Long): String = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        .withZone(MY_ZONE).format(Date(epochSeconds * 1000L).toInstant())

    fun hijriMalay(value: String): String {
        val parts = value.split("-")
        if (parts.size != 3) return value
        val month = parts[1].toIntOrNull()?.let { hijriMonths.getOrNull(it - 1) } ?: parts[1]
        return "${parts[2].toIntOrNull() ?: parts[2]} $month ${parts[0]}"
    }

    fun dateMalay(date: LocalDate = LocalDate.now(MY_ZONE)): String {
        val fmt = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("ms", "MY"))
        return date.format(fmt).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ms", "MY")) else it.toString() }
    }

    fun prayerValues(day: PrayerDay): List<Pair<String, String>> = listOf(
        "SUBUH" to time(day.fajr), "SYURUK" to time(day.syuruk), "ZOHOR" to time(day.dhuhr),
        "ASAR" to time(day.asr), "MAGHRIB" to time(day.maghrib), "ISYAK" to time(day.isha)
    )
}
