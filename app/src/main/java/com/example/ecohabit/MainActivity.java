package com.example.ecohabit;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import android.widget.LinearLayout;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvHabits;
    private HabitAdapter adapter;
    private FloatingActionButton fabAdd;
    private TextView tvScore; // Score
    private android.widget.ProgressBar progressBar;

    public static ArrayList<Habit> globalHabitList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // MINTA IZIN NOTIFIKASI (Khusus Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        rvHabits = findViewById(R.id.rvHabits);
        fabAdd = findViewById(R.id.fabAdd);
        tvScore = findViewById(R.id.tvScore);
        progressBar = findViewById(R.id.progressBarLevel);
        LinearLayout layoutEmpty = findViewById(R.id.layoutEmpty);

        if (globalHabitList.isEmpty()) {
            globalHabitList.add(new Habit("Bawa Botol Minum", "Kurangi Sampah", "Hari ini, 08:00"));
            globalHabitList.add(new Habit("Matikan Lampu", "Hemat Energi", "Hari ini, 12:00"));
        }

        setupRecyclerView();

        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void setupRecyclerView() {
        // 1. Inisialisasi dulu komponen Layout Kosongnya
        android.widget.LinearLayout layoutEmpty = findViewById(R.id.layoutEmpty);

        // 2. Buat dulu list-nya (Filter yang belum selesai)
        ArrayList<Habit> activeList = new ArrayList<>();
        for (Habit h : globalHabitList) {
            if (!h.isCompleted()) {
                activeList.add(h);
            }
        }

        // 3. Baru cek apakah list kosong atau tidak
        if (activeList.isEmpty()) {
            rvHabits.setVisibility(View.GONE);     // Sembunyikan List
            if (layoutEmpty != null) {
                layoutEmpty.setVisibility(View.VISIBLE); // Munculkan Gambar Kosong
            }
        } else {
            rvHabits.setVisibility(View.VISIBLE);  // Munculkan List
            if (layoutEmpty != null) {
                layoutEmpty.setVisibility(View.GONE);    // Sembunyikan Gambar Kosong
            }
        }

        // 4. Pasang ke Adapter
        adapter = new HabitAdapter(activeList);
        rvHabits.setLayoutManager(new LinearLayoutManager(this));
        rvHabits.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupRecyclerView();
        updateScore();
    }

    private void updateScore() {
        int totalDone = 0;
        for (Habit h : globalHabitList) {
            if (h.isCompleted()) totalDone++;
        }

        int score = totalDone * 10;

        // Logika Level Up: Setiap 100 poin reset bar tapi level naik (opsional)
        // Atau buat max 100 saja.

        if (tvScore != null) {
            tvScore.setText("Level Lingkungan: " + score + " XP");
        }

        // Animasi bar
        progressBar.setProgress(score);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tambah Kebiasaan Baru");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_habit, null);
        builder.setView(view);

        EditText etTitle = view.findViewById(R.id.etDialogTitle);
        Spinner spCategory = view.findViewById(R.id.spDialogCategory);
        TextView tvDate = view.findViewById(R.id.tvSelectDate);
        TextView tvTime = view.findViewById(R.id.tvSelectTime);

        String[] categories = {"Hemat Energi", "Kurangi Sampah", "Hemat Air", "Transportasi Hijau"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapterSpinner);

        // Calendar untuk menyimpan waktu yang dipilih user
        Calendar calendar = Calendar.getInstance();
        final String[] selectedDate = {"Hari ini"};
        final String[] selectedTime = {"08:00"};

        tvDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
                selectedDate[0] = dayOfMonth + "/" + (month + 1) + "/" + year;
                tvDate.setText(selectedDate[0]);

                // Simpan ke Calendar
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        tvTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view12, hourOfDay, minute) -> {
                String formattedTime = String.format("%02d:%02d", hourOfDay, minute);
                selectedTime[0] = formattedTime;
                tvTime.setText(formattedTime);

                // Simpan ke Calendar
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String title = etTitle.getText().toString();
            String category = spCategory.getSelectedItem().toString();
            String fullDateTime = selectedDate[0] + ", " + selectedTime[0];

            if (!title.isEmpty()) {
                Habit newHabit = new Habit(title, category, fullDateTime);
                globalHabitList.add(newHabit);

                // --- SET ALARM NOTIFIKASI ---
                setAlarm(newHabit, calendar.getTimeInMillis());

                setupRecyclerView();
                Toast.makeText(this, "Pengingat diaktifkan!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Isi nama kebiasaan dulu!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // Method Setting Alarm
    private void setAlarm(Habit habit, long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);

        // Kirim Judul & Pesan ke Receiver
        intent.putExtra("TITLE", "Waktunya EcoHabit!");
        intent.putExtra("MESSAGE", "Jangan lupa: " + habit.getTitle());

        // ID Unik agar alarm tidak saling menimpa
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                habit.getId(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Validasi waktu: Jangan bunyikan alarm jika waktunya sudah lewat
        if (timeInMillis > System.currentTimeMillis()) {
            if (alarmManager != null) {
                // Set Alarm Tepat Waktu
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            }
        }
    }

    // 1. Method Simpan Data
    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("EcoHabitData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(globalHabitList);
        editor.putString("habit_list", json);
        editor.apply();
    }

    // 2. Method Muat Data
    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("EcoHabitData", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("habit_list", null);
        Type type = new TypeToken<ArrayList<Habit>>() {}.getType();

        if (json != null) {
            globalHabitList = gson.fromJson(json, type);
        }

        // Jika data kosong (pertama kali install), isi dummy
        if (globalHabitList == null || globalHabitList.isEmpty()) {
            globalHabitList = new ArrayList<>();
            globalHabitList.add(new Habit("Bawa Botol Minum", "Kurangi Sampah", "Hari ini, 08:00"));
        }
    }
}