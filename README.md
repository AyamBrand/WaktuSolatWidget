# Waktu Solat Widget

[![Android API 26+](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions)
[![Target SDK 36](https://img.shields.io/badge/Target%20SDK-36-4285F4)](https://developer.android.com/about/versions/16)
[![Versi](https://img.shields.io/badge/versi-1.2.4-DAA520)](https://github.com/AyamBrand/WaktuSolatWidget)

Widget Android untuk memaparkan waktu solat harian Malaysia terus pada skrin utama. Jadual mengikut kod zon e-Solat JAKIM dan dikemas kini melalui API Waktu Solat Malaysia.

## Ciri-ciri

- Jam, tarikh, paras bateri dan kod zon pada satu widget.
- Paparan solat seterusnya dengan countdown langsung yang berdetik setiap saat.
- Countdown menggunakan format jam:minit:saat; awalan `00:` dipaparkan apabila baki kurang daripada sejam.
- Enam waktu harian: Subuh, Syuruk, Zohor, Asar, Maghrib dan Isyak.
- Kiraan hari menuju jangkaan awal Ramadan 1448. Baris Ramadan diletakkan di tengah dan menggunakan fon 16sp, sama saiz dengan angka waktu solat.
- Tetapan kod zon dalam aplikasi, dengan pautan terus ke [Jadual Kod Zon rasmi e-Solat JAKIM](https://www.e-solat.gov.my/index.php?siteId=24&pageId=50).
- Latar widget gelap kosong untuk memastikan maklumat mudah dibaca.

## Keserasian

- Minimum: Android 8.0 (API 26).
- Disasarkan dan dikompil menggunakan Android API 36.
- Projek menggunakan Kotlin, Gradle Wrapper dan JDK 17.

## Dapatkan source

```bash
git clone https://github.com/AyamBrand/WaktuSolatWidget.git
cd WaktuSolatWidget
```

Buka folder projek dalam Android Studio. Apabila diminta, benarkan Android Studio melengkapkan Gradle Sync dan memasang Android SDK Platform 36 serta Build-Tools yang diperlukan.

## Bina APK debug

Dalam Android Studio, pilih **Build → Build Bundle(s) / APK(s) → Build APK(s)**. APK akan diletakkan di:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Atau bina melalui terminal:

```powershell
.\gradlew.bat assembleDebug
```

Pada Linux atau macOS:

```bash
./gradlew assembleDebug
```

Untuk memasang APK debug melalui ADB pada telefon yang sudah disambungkan dengan USB debugging:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Pilih zon waktu solat

Buka aplikasi, masukkan kod zon kawasan anda dan tekan **Simpan**. Jika tidak tahu kodnya, tekan pautan **Jadual Kod Zon JAKIM** dalam aplikasi. Kod zon bergantung pada daerah; contohnya, Wilayah Persekutuan Kuala Lumpur dan Putrajaya menggunakan `WLY01`.

Aplikasi memerlukan sambungan internet untuk mengambil jadual terkini. Respons jadual yang berjaya dimuat turun disimpan pada telefon supaya jadual terakhir masih boleh digunakan apabila tiada sambungan.

## Sumber data dan tarikh Ramadan

Data waktu solat diambil daripada [API Waktu Solat Malaysia](https://api.waktusolat.app/docs), yang menyatakan data bersumber daripada e-Solat JAKIM. Kiraan Ramadan dalam versi ini menggunakan jangkaan 1 Ramadan 1448 pada **8 Februari 2027**. Tarikh sebenar tertakluk pada pengisytiharan rasmi pihak berkuasa agama.

## Struktur projek

```text
app/src/main/
├── java/my/abi/waktusolat/
│   ├── MainActivity.kt
│   ├── PrayerRepository.kt
│   └── PrayerWidgetProvider.kt
└── res/
    ├── layout/prayer_widget.xml
    ├── drawable/widget_bg.xml
    └── xml/prayer_widget_info.xml
```

## Kredit

Dibangunkan oleh [Muktasyaf AnNamir](https://mylink.la/annamir) menggunakan [Manus 2.0 Lite](https://manus.im), Android Studio dan Kotlin.

## Lesen

Repositori ini belum menetapkan lesen sumber terbuka. Jika mahu orang lain menggunakan, mengubah suai atau mengedarkan semula kod, tambah fail lesen yang sesuai terlebih dahulu.
