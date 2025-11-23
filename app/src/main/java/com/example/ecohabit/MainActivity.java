package com.example.ecohabit;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvHabits;
    private HabitAdapter adapter;
    private FloatingActionButton fabAdd;

    // 1. UBAH JADI PUBLIC STATIC (AGAR BISA DIAKSES DARI RIWAYAT)
    public static ArrayList<Habit> globalHabitList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvHabits = findViewById(R.id.rvHabits);
        fabAdd = findViewById(R.id.fabAdd);

        // Isi data dummy HANYA JIKA list masih kosong (biar tidak dobel saat di-restart)
        if (globalHabitList.isEmpty()) {
            globalHabitList.add(new Habit("Bawa Botol Minum", "Kurangi Sampah", "Hari ini, 08:00"));
            globalHabitList.add(new Habit("Matikan Lampu", "Hemat Energi", "Hari ini, 12:00"));
            // Tambah satu data yang sudah selesai untuk tes riwayat
            Habit hDone = new Habit("Daur Ulang Kertas", "Sampah", "Kemarin, 09:00");
            hDone.setCompleted(true);
            globalHabitList.add(hDone);
        }

        setupRecyclerView();

        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    // Method khusus untuk Refresh Tampilan
    private void setupRecyclerView() {
        // Kita hanya menampilkan yang BELUM selesai di halaman utama
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

    // 2. TAMBAHKAN onResume (Agar saat kembali dari Riwayat, list diperbarui)
    @Override
    protected void onResume() {
        super.onResume();
        setupRecyclerView();
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

        // Setup Spinner
        String[] categories = {"Hemat Energi", "Kurangi Sampah", "Hemat Air", "Transportasi Hijau"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapterSpinner);

        // Setup Date & Time Picker
        Calendar calendar = Calendar.getInstance();
        final String[] selectedDate = {"Hari ini"};
        final String[] selectedTime = {"08:00"};

        tvDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
                selectedDate[0] = dayOfMonth + "/" + (month + 1) + "/" + year;
                tvDate.setText(selectedDate[0]);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        tvTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view12, hourOfDay, minute) -> {
                String formattedTime = String.format("%02d:%02d", hourOfDay, minute);
                selectedTime[0] = formattedTime;
                tvTime.setText(formattedTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String title = etTitle.getText().toString();
            String category = spCategory.getSelectedItem().toString();
            String fullDateTime = selectedDate[0] + ", " + selectedTime[0];

            if (!title.isEmpty()) {
                globalHabitList.add(new Habit(title, category, fullDateTime));
                setupRecyclerView(); // Refresh list
                Toast.makeText(this, "Tersimpan!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Isi nama kebiasaan dulu!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}