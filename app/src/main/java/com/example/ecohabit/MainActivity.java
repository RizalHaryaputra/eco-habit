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
import android.widget.CheckBox;
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

        loadData();

        if (globalHabitList.isEmpty()) {
            globalHabitList.add(new Habit("Bawa Botol Minum", "Kurangi Sampah", 8, 0, true));
            globalHabitList.add(new Habit("Matikan Lampu", "Hemat Energi", 12, 0, true));
        }

        setupRecyclerView();
        updateScoreUI();

        fabAdd.setOnClickListener(v -> showAddDialog(null));
    }

    private void setupRecyclerView() {
        // 1. Inisialisasi dulu komponen Layout Kosongnya
        LinearLayout layoutEmpty = findViewById(R.id.layoutEmpty);

        // 2. Buat dulu list-nya (Filter yang aktif dan belum selesai)
        ArrayList<Habit> activeList = new ArrayList<>(globalHabitList);
//        for (Habit h : globalHabitList) {
//            if (h.isActive() && !h.isCompletedForToday()) {
//                activeList.add(h);
//            }
//        }

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
        refreshData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // SAVE AUTOMATICALLY ON PAUSE
        saveData();
    }

    public void refreshData() {
        saveData();
        setupRecyclerView();
        updateScoreUI();
    }

    public void updateAlarmState(Habit habit) {
        if (habit.isActive()) {
            setAlarm(habit);
            Toast.makeText(this, "Pengingat Aktif", Toast.LENGTH_SHORT).show();
        } else {
            cancelAlarm(habit);
            Toast.makeText(this, "Pengingat Mati", Toast.LENGTH_SHORT).show();
        }
        saveData();
    }

    private void updateScoreUI() {
        int totalDone = 0;
        for (Habit h : globalHabitList) {
            if (h.isCompletedForToday()) totalDone++;
        }

//        int score = totalDone * 10;
//
//        // Logika Level Up: Setiap 100 poin reset bar tapi level naik (opsional)
//        // Atau buat max 100 saja.
//
//        if (tvScore != null) {
//            tvScore.setText("Level Lingkungan: " + score + " XP");
//        }

        if (tvScore != null) {
            tvScore.setText("Selesai Hari Ini: " + totalDone);
        }

//        // Animasi bar
//        progressBar.setProgress(score);
    }

    public void showAddDialog(Habit habitToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(habitToEdit == null ? "Tambah Kebiasaan" : "Edit Kebiasaan");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_habit, null);
        builder.setView(view);

        EditText etTitle = view.findViewById(R.id.etDialogTitle);
        Spinner spCategory = view.findViewById(R.id.spDialogCategory);
        TextView tvTime = view.findViewById(R.id.tvSelectTime);
        CheckBox cbRepeat = view.findViewById(R.id.cbDialogRepeat);

        String[] categories = {"Hemat Energi", "Kurangi Sampah", "Hemat Air", "Transportasi Hijau"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapterSpinner);

        final int[] selectedTime = {8, 0}; // Hour, Minute

        // PRE-FILL DATA (If Editing)
        if (habitToEdit != null) {
            etTitle.setText(habitToEdit.getTitle());
            selectedTime[0] = habitToEdit.getHour();
            selectedTime[1] = habitToEdit.getMinute();
            tvTime.setText(String.format("%02d:%02d", selectedTime[0], selectedTime[1]));
            if (cbRepeat != null) cbRepeat.setChecked(habitToEdit.isRepeating());

            for(int i=0; i < categories.length; i++) {
                if(categories[i].equals(habitToEdit.getCategory())) spCategory.setSelection(i);
            }
        }

        tvTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view12, hourOfDay, minute) -> {
                selectedTime[0] = hourOfDay;
                selectedTime[1] = minute;
                tvTime.setText(String.format("%02d:%02d", hourOfDay, minute));
            }, selectedTime[0], selectedTime[1], true).show();
        });

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String title = etTitle.getText().toString();
            String category = spCategory.getSelectedItem().toString();

            if (title.isEmpty()) {
                Toast.makeText(this, "Nama harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (habitToEdit == null) {
                // CREATE NEW
                Habit newHabit = new Habit(title, category, selectedTime[0], selectedTime[1], true);
                if (cbRepeat != null) newHabit.setRepeating(cbRepeat.isChecked());
                globalHabitList.add(newHabit);
                setAlarm(newHabit);
            } else {
                // EDIT EXISTING
                cancelAlarm(habitToEdit); // Cancel old alarm first
                habitToEdit.setTitle(title);
                habitToEdit.setCategory(category);
                habitToEdit.setHour(selectedTime[0]);
                habitToEdit.setMinute(selectedTime[1]);
                if (cbRepeat != null) habitToEdit.setRepeating(cbRepeat.isChecked());
                // Reset completion if edited (optional choice)
                // habitToEdit.resetCompletion();

                // If switch is currently active, reset alarm
                if (habitToEdit.isActive()) {
                    setAlarm(habitToEdit);
                }
            }
            refreshData();
        });

        if (habitToEdit != null) {
            builder.setNeutralButton("Hapus", (dialog, which) -> {
                cancelAlarm(habitToEdit);
                globalHabitList.remove(habitToEdit);
                refreshData();
            });
        }

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // Method Setting Alarm
    private void setAlarm(Habit habit) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);

        // Kirim Judul & Pesan ke Receiver
        intent.putExtra("TITLE", "Waktunya EcoHabit!");
//        intent.putExtra("MESSAGE", "Jangan lupa: " + habit.getTitle());
        intent.putExtra("ID", habit.getId());
        intent.putExtra("IS_ACTION", habit.isActionRequired());

        // ID Unik agar alarm tidak saling menimpa
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                habit.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, habit.getHour());
        calendar.set(Calendar.MINUTE, habit.getMinute());
        calendar.set(Calendar.SECOND, 0);

        // If time has passed today, set for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void cancelAlarm(Habit habit) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, habit.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
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
        if (json != null) globalHabitList = gson.fromJson(json, type);
        if (globalHabitList == null) globalHabitList = new ArrayList<>();
    }

    // 1. Memunculkan Menu Titik Tiga
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // 2. Aksi Saat Menu Diklik
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            // Pindah ke Halaman About
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}