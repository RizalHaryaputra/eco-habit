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
import android.widget.ProgressBar;
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
import java.util.Random;

import android.widget.LinearLayout;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvHabits;
    private HabitAdapter adapter;
    private FloatingActionButton fabAdd;
    private TextView tvScore;
    private TextView tvLevel;
    private TextView tvDailyStats;
    private ProgressBar progressBarLevel;

    // List 1: Active Schedules
    public static ArrayList<Habit> globalHabitList = new ArrayList<>();
    // List 2: Past Logs (History)
    public static ArrayList<Habit> globalHistoryList = new ArrayList<>();

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
        tvLevel = findViewById(R.id.tvLevel);
        tvDailyStats = findViewById(R.id.tvDailyStats);
        progressBarLevel = findViewById(R.id.progressBarLevel);
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
        loadData();
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
        int totalHistoryCount = 0;
        if (globalHistoryList != null) {
            totalHistoryCount = globalHistoryList.size();
        }

        // --- 1. CALCULATE LEVEL & XP ---
        // Logic: 10 Habits = 1 Level (100 XP)
        // Each habit worth 10 XP
        int totalXP = totalHistoryCount * 10;
        int currentLevel = (totalXP / 100) + 1; // Start at Level 1
        int currentProgress = totalXP % 100;    // 0 to 99

        if (tvLevel != null) tvLevel.setText("Level " + currentLevel);
        if (tvScore != null) tvScore.setText(currentProgress + "/100 XP");
        if (progressBarLevel != null) progressBarLevel.setProgress(currentProgress);

        // --- 2. CALCULATE DAILY STATS ---
        int dailyCount = 0;
        String today = getCurrentDateString();

        if (globalHistoryList != null) {
            for (Habit h : globalHistoryList) {
                // Check if the history item has a date AND if it matches today
                if (h.getLastCompletedDate() != null && h.getLastCompletedDate().equals(today)) {
                    dailyCount++;
                }
            }
        }

        if (tvDailyStats != null) {
            tvDailyStats.setText("Hari ini: " + dailyCount + "x Menyadari Lingkungan");
        }
    }

    // Helper to get today's date for comparison
    private String getCurrentDateString() {
        Calendar calendar = Calendar.getInstance();
        return String.format(java.util.Locale.US, "%d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                (calendar.get(Calendar.MONTH) + 1),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void createDummyData() {
        // 1. Routine Habit (Action Required, Repeating) - Morning
        Habit h1 = new Habit("Bawa Botol Minum", "Kurangi Sampah", 7, 0, true);
        h1.setRepeating(true);
        h1.setRandom(false);
        globalHabitList.add(h1);
        setAlarm(h1);

        // 2. Awareness Reminder (No Action, Random Time) - Energy
        Habit h2 = new Habit("Matikan lampu", "Hemat Energi", 10, 0, false);
        h2.setRepeating(true);
        h2.setRandom(true); // Will trigger randomly between 08:00-20:00
        globalHabitList.add(h2);
        setAlarm(h2);

        // 3. One-Time Task (Action Required, No Repeat) - Water
        Habit h3 = new Habit("Cek Keran Air", "Hemat Air", 7, 30, true);
        h3.setRepeating(false); // Disappears after doing it once
        h3.setRandom(false);
        globalHabitList.add(h3);
        setAlarm(h3);

        // 4. Transport Habit (Action Required, Repeating) - Afternoon
        Habit h4 = new Habit("Jalan Kaki ke Kelas Jika Sempat", "Transportasi Hijau", 9, 0, true);
        h4.setRepeating(true);
        h4.setRandom(false);
        globalHabitList.add(h4);
        setAlarm(h4);

        // Save immediately
        saveData();
    }

    public void showAddDialog(Habit habitToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(habitToEdit == null ? "Tambah Kebiasaan" : "Edit Kebiasaan");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_habit, null);
        builder.setView(view);

        EditText etTitle = view.findViewById(R.id.etDialogTitle);
        Spinner spCategory = view.findViewById(R.id.spDialogCategory);
        TextView tvTime = view.findViewById(R.id.tvSelectTime);
        LinearLayout layoutTimePicker = view.findViewById(R.id.layoutTimePicker);

        CheckBox cbRepeat = view.findViewById(R.id.cbDialogRepeat);
        CheckBox cbAction = view.findViewById(R.id.cbDialogAction);
        CheckBox cbRandom = view.findViewById(R.id.cbDialogRandom);

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
            if (cbAction != null) cbAction.setChecked(habitToEdit.isActionRequired());
            if (cbRandom != null) {
                cbRandom.setChecked(habitToEdit.isRandom());
                if(habitToEdit.isRandom()) layoutTimePicker.setVisibility(View.GONE);
            }

            for(int i=0; i < categories.length; i++) {
                if(categories[i].equals(habitToEdit.getCategory())) spCategory.setSelection(i);
            }
        }

        // LOGIC: Random Checkbox hides Time Picker
        if (cbRandom != null) {
            cbRandom.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    layoutTimePicker.setVisibility(View.GONE);
                } else {
                    layoutTimePicker.setVisibility(View.VISIBLE);
                }
            });
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

            // --- LOGIC: Random Time Generation ---
            int finalHour = selectedTime[0];
            int finalMinute = selectedTime[1];
            boolean isRandom = (cbRandom != null && cbRandom.isChecked());

            if (isRandom) {
                // Generate random time between 08:00 and 20:00
                Random r = new Random();
                finalHour = r.nextInt(12) + 8; // 8 to 19
                finalMinute = r.nextInt(60);
            }

            boolean isAction = (cbAction != null && cbAction.isChecked());
            boolean isRepeat = (cbRepeat != null && cbRepeat.isChecked());

            if (habitToEdit == null) {
                // CREATE NEW
                Habit newHabit = new Habit(title, category, finalHour, finalMinute, isAction);
                newHabit.setRepeating(isRepeat);
                newHabit.setRandom(isRandom);
                globalHabitList.add(newHabit);
                setAlarm(newHabit);
            } else {
                // EDIT EXISTING
                cancelAlarm(habitToEdit); // Cancel old alarm first
                habitToEdit.setTitle(title);
                habitToEdit.setCategory(category);
                habitToEdit.setHour(finalHour);
                habitToEdit.setMinute(finalMinute);
                habitToEdit.setActionRequired(isAction);
                habitToEdit.setRepeating(isRepeat);
                habitToEdit.setRandom(isRandom);
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
        intent.putExtra("TITLE", habit.getTitle());
        intent.putExtra("CATEGORY", habit.getCategory());
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

        // Save Active List
        String jsonHabit = gson.toJson(globalHabitList);
        editor.putString("habit_list", jsonHabit);

        // Save History List
        String jsonHistory = gson.toJson(globalHistoryList);
        editor.putString("history_list", jsonHistory);

        editor.apply();
    }

    // 2. Method Muat Data
    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("EcoHabitData", MODE_PRIVATE);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Habit>>() {}.getType();

        // Load Active
        String jsonHabit = sharedPreferences.getString("habit_list", null);
        if (jsonHabit != null) globalHabitList = gson.fromJson(jsonHabit, type);
        if (globalHabitList == null || globalHabitList.isEmpty()) {
            globalHabitList = new ArrayList<>();
            createDummyData();
        }

        // Load History
        String jsonHistory = sharedPreferences.getString("history_list", null);
        if (jsonHistory != null) globalHistoryList = gson.fromJson(jsonHistory, type);
        if (globalHistoryList == null) globalHistoryList = new ArrayList<>();
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