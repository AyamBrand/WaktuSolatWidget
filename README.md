# Waktu Solat Widget — projek Android Studio

Projek Kotlin ini menghasilkan widget waktu solat untuk Android (min API 26, compile/target API 36). Tetapan zon awal ialah **WLY01 (Putrajaya)**. Jadual bulanan dimuatkan daripada Waktu Solat Malaysia API, yang menyatakan datanya bersumber daripada e-Solat JAKIM; respons jadual disimpan pada telefon dan boleh dipaparkan semula tanpa internet selepas pernah dimuat turun.

Widget menggunakan latar gelap kosong. Teks jam, tarikh, bateri, lokasi, solat seterusnya, kira-undur langsung yang berdetik setiap saat dan enam waktu solat kekal sebagai paparan widget sebenar. Countdown bawah sejam memaparkan awalan `00:` supaya formatnya seperti `00:14:22`. Skrin aplikasi menyediakan pautan ke [Jadual Kod Zon rasmi e-Solat JAKIM](https://www.e-solat.gov.my/index.php?siteId=24&pageId=50). Selepas pemisah terakhir, tajuk **JANGKAAN AWAL RAMADAN 1448** serta baris **8 FEBRUARI 2027 = N hari lagi!** dipusatkan; bilangan hari ditebalkan. Tarikh ini mengikut Takwim Tarikh Penting Islam JAKIM 2027; tarikh muktamad Ramadan tertakluk pengisytiharan rasmi. Kiraan Ramadan dikira daripada tarikh semasa peranti dan disegarkan apabila widget dikemas kini. Ketik kad untuk buka aplikasi; ketik label zon untuk cuba muat semula. Kod zon boleh diubah pada skrin aplikasi.

Skrin tetapan turut memaparkan kredit putih berpusat “Widget ini dibangunkan dari ❤️ oleh Muktasyaf AnNamir menggunakan Manus 2.0 Lite.” Nama Muktasyaf AnNamir membuka `https://mylink.la/annamir`, manakala Manus 2.0 Lite membuka `https://manus.im`.

Versi 1.0.2 menggantikan elemen `<View>` biasa yang tidak dibenarkan dalam susun atur `RemoteViews` dengan `TextView`; punca ini disahkan melalui Logcat pada HONOR 200 Pro.

## Bina APK menggunakan Android Studio

1. Muat turun dan pasang **Android Studio** pada komputer anda. Semasa persediaan, pastikan Android SDK Platform 36 dan Android SDK Build-Tools dipasang melalui **Tools → SDK Manager**. Android Studio biasanya akan meminta komponen SDK yang belum ada apabila projek dibuka.
2. Muat turun fail projek ZIP yang dibekalkan dan ekstrak ke satu folder biasa, contohnya `WaktuSolatWidget`.
3. Buka Android Studio. Pilih **Open**, kemudian pilih folder projek yang mengandungi `settings.gradle` dan `app/`.
4. Jika Android Studio bertanya sama ada mahu mempercayai projek, pilih **Trust Project**. Tunggu **Gradle Sync** selesai. Jika diminta, benarkan Android Studio memuat turun Gradle atau SDK Platform 36.
5. Untuk APK yang mudah dipasang bagi ujian, pilih **Build → Build Bundle(s) / APK(s) → Build APK(s)**. Tunggu notifikasi **APK(s) generated successfully**.
6. Tekan **Locate** pada notifikasi. Fail lazimnya berada di `app/build/outputs/apk/debug/app-debug.apk`. Salin fail APK itu ke telefon dan pasangkannya. Jika Android meminta kebenaran, benarkan pemasangan daripada sumber tersebut.
7. Buka aplikasi sekali dengan internet, semak zon `WLY01`, kemudian tekan **Kemas kini waktu solat**. Ini memuatkan jadual dan menyimpannya untuk paparan widget.
8. Pada skrin utama telefon, tekan lama ruang kosong → **Widgets** → cari **Waktu Solat** → tekan lama widget dan letakkan pada skrin. Besarkan widget jika ruangnya tidak mencukupi.

### APK untuk edaran

APK debug di atas sesuai untuk pemasangan dan ujian sendiri. Untuk berkongsi secara lebih rasmi, pilih **Build → Generate Signed Bundle / APK → APK**, cipta atau pilih keystore, kemudian ikuti wizard. Simpan fail keystore dan kata laluannya dengan selamat; keystore yang sama diperlukan untuk mengemas kini pemasangan yang telah ditandatangani dengannya.

## Widget skrin kunci

Aplikasi ini tidak menggunakan lapisan tindihan pada skrin kunci. Ia mendaftarkan widget Android biasa tanpa menyekat penggunaan pada skrin kunci. Jika sistem telefon menyediakan hos widget skrin kunci yang serasi, widget boleh dipilih melalui antara muka kunci/ruang widget telefon. Jika tiada pilihan untuk menambah widget pihak ketiga dalam tetapan skrin kunci telefon, aplikasi tidak boleh memaksanya melalui APK.

Sokongan itu bergantung pada versi firmware, rantau dan pelaksanaan pengeluar, bukan hanya nombor Android. Pada **HONOR 200 Pro** dan **Redmi Note 15 5G**, uji selepas telefon menerima kemas kini sistem terkini. Jika fungsi widget skrin kunci tidak tersedia pada peranti, widget skrin utama tetap boleh digunakan. Jangan anggap ia berjaya sehingga nama widget muncul dalam pemilih skrin kunci telefon.

## Had dan privasi

- Zon awal ialah WLY01. Pastikan zon yang dipilih betul untuk lokasi sebenar.
- Kemas kini jadual memerlukan internet. Aplikasi tidak meminta lokasi, akaun, kenalan atau kebenaran sensitif.
- Waktu solat disediakan oleh API pihak ketiga yang menyatakan sumbernya ialah JAKIM e-Solat. Semak waktu rasmi setempat jika berlaku perbezaan, dan jangan bergantung pada aplikasi ini sebagai satu-satunya pengesahan.
- Android dan pengeluar boleh mengehadkan kekerapan kemas kini widget di latar. Baki masa yang ditulis sebagai teks akan berubah apabila widget dikemas kini; buka aplikasi atau ketik zon untuk memuat semula jadual.

## Kandungan projek

- `app/src/main/java/` — aktiviti, pemproses jadual dan penerima widget Kotlin.
- `app/src/main/res/layout/prayer_widget.xml` — susun atur widget.
- `app/src/main/res/xml/prayer_widget_info.xml` — metadata saiz dan kemas kini widget.
- `app/build.gradle` — versi SDK, sasaran API dan identiti aplikasi.


## Kemas kini v1.2.4

Teks tajuk dan baris countdown Ramadan dibesarkan kepada 16sp, menyamai saiz teks jam waktu solat dalam jadual widget.
