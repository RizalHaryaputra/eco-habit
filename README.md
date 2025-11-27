# EcoHabit 🌱

> **Bangun kebiasaan kecil untuk dampak besar bagi bumi.**

## 📖 Tentang Proyek

**EcoHabit** adalah aplikasi *mobile* berbasis Android yang dirancang untuk membantu pengguna memulai dan konsisten menjalani gaya hidup ramah lingkungan. Aplikasi ini berfungsi sebagai pengingat pribadi dan pelacak kebiasaan (*habit tracker*) harian.

Proyek ini dikembangkan sebagai tugas akhir mata kuliah **Pengembangan Aplikasi Mobile** oleh Kelompok Seriyo. Aplikasi ini berjalan sepenuhnya secara **offline** (lokal) dengan fokus pada kesederhanaan dan kemudahan penggunaan.

---

## ✨ Fitur Utama

Aplikasi ini memiliki beberapa fitur unggulan untuk mendukung penggunanya:

### 1. 📝 Pencatatan Kebiasaan (Habit Tracking)
Pengguna dapat menambahkan target kebiasaan baru dengan detail:
* Judul Kebiasaan.
* Kategori (Contoh: Hemat Energi, Kurangi Sampah, Transportasi Hijau).
* Waktu Pengingat (Tanggal dan Jam spesifik).

### 2. ✅ Sistem Checklist Harian
Halaman utama menampilkan daftar kebiasaan aktif hari ini. Pengguna cukup mencentang (*checklist*) tugas yang sudah diselesaikan.

### 3. 🕰️ Riwayat Aktivitas & Manajemen Data
Kebiasaan yang sudah selesai otomatis pindah ke halaman **Riwayat**. Pengguna dapat:
* Melihat daftar pencapaian masa lalu.
* Menghapus permanen riwayat yang tidak diinginkan (dengan konfirmasi).

### 4. 🔔 Notifikasi Pengingat Otomatis
Aplikasi menggunakan `AlarmManager` untuk mengirimkan notifikasi *push* tepat pada waktu yang telah diatur pengguna, memastikan tidak ada kebiasaan yang terlewat.

### 5. 💾 Penyimpanan Data Permanen (Anti-Lupa)
Menggunakan kombinasi **SharedPreferences** dan library **Gson** untuk menyimpan data secara lokal di perangkat. Data tidak akan hilang meskipun aplikasi ditutup total atau HP direstart.

### 6. 🏆 Gamifikasi Sederhana (Skor XP)
Untuk memotivasi pengguna, aplikasi menghitung total Poin (XP) berdasarkan jumlah kebiasaan yang telah diselesaikan.

### 7. 💡 Tips Lingkungan Harian
Menampilkan fakta unik atau tips singkat tentang lingkungan setiap kali aplikasi dibuka di halaman depan.

---

## 🛠️ Teknologi yang Digunakan

Aplikasi ini dibangun menggunakan teknologi standar pengembangan Android:

* **Bahasa Pemrograman:** Java
* **Minimum SDK:** API 24 (Android 7.0 Nougat)
* **Target SDK:** API 34 (Android 14)
* **Arsitektur UI:** XML Layouts & Material Design 3 Components
* **Local Database:** SharedPreferences
* **Library Tambahan:**
    * `com.google.code.gson:gson:2.10.1` (Untuk serialisasi data objek ke JSON)
* **Fitur Android Core:**
    * `RecyclerView` (Untuk menampilkan list data)
    * `AlarmManager` & `BroadcastReceiver` (Untuk notifikasi terjadwal)
    * `NotificationManager`

---

## 💻 Cara Menjalankan Proyek (Installation)

Untuk menjalankan proyek ini di komputer lokal Anda:

1.  **Prasyarat:** Pastikan Anda telah menginstal Android Studio terbaru.
2.  **Clone Repository:**
    ```bash
    git clone https://github.com/RizalHaryaputra/eco-habit.git
    ```
3.  **Buka di Android Studio:**
    * Buka Android Studio, pilih **Open**.
    * Arahkan ke folder proyek `EcoHabit` yang baru di-clone.
4.  **Sync Gradle:** Tunggu hingga Android Studio selesai mengunduh dependensi dan melakukan sinkronisasi Gradle.
5.  **Run:**
    * Sambungkan perangkat Android fisik (pastikan USB Debugging aktif) atau gunakan Android Emulator.
    * Klik tombol **Run** (ikon segitiga hijau ▶️) di toolbar Android Studio.

---

## 👥 Tim Pengembang

Proyek ini dikembangkan oleh **Kelompok Seriyo**:

1.  **Widyasena Aryatama** (NIM: 23051130009)
2.  **Rizal Haryaputra** (NIM: 23051130013)
3.  **Yoktan Nathanael Setyadi** (NIM: 23051130039)

---

## 🔜 Rencana Pengembangan Masa Depan

* [ ] Integrasi Database Cloud (Firebase) untuk sinkronisasi antar perangkat.
* [ ] Menambahkan grafik statistik mingguan/bulanan.
* [ ] Fitur "Dark Mode" (Mode Gelap).
* [ ] Menambahkan fitur berbagi pencapaian ke media sosial.

---
*Dibuat untuk memenuhi tugas mata kuliah Pengembangan Aplikasi Mobile, 2025.*
