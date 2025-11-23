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

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvHabits;
    private HabitAdapter adapter;
    private FloatingActionButton fabAdd;
    private TextView tvScore; // Score

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
        tvScore = findViewById(R.id.tvScore); // Pastikan Anda sudah menambah TextView Score di XML

        if (globalHabitList.isEmpty()) {
            globalHabitList.add(new Habit("Bawa Botol Minum", "Kurangi Sampah", "Hari ini, 08:00"));
            globalHabitList.add(new Habit("Matikan Lampu", "Hemat Energi", "Hari ini, 12:00"));
        }

        setupRecyclerView();

        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void setupRecyclerView() {
        ArrayList<Habit> activeList = new ArrayList<>();
        for (Habit h : globalHabitList) {
            if (!h.isCompleted()) {
                activeList.add(h);
            }
        }
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
        // Jika TextView Score belum ada di XML, baris ini akan error (NullPointer), hati-hati
        if (tvScore != null) {
            tvScore.setText("Total Poin: " + (totalDone * 10) + " XP");
        }
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
}