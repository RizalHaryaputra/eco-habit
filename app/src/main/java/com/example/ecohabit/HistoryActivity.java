package com.example.ecohabit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HabitAdapter adapter;
    private ArrayList<Habit> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
    }

    private void ensureDataLoaded() {
        // If the global list is empty (e.g. user came straight from Welcome Screen),
        // load the data manually here.
        if (MainActivity.globalHabitList == null || MainActivity.globalHabitList.isEmpty()) {
            SharedPreferences sharedPreferences = getSharedPreferences("EcoHabitData", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString("habit_list", null);
            Type type = new TypeToken<ArrayList<Habit>>() {}.getType();

            if (json != null) {
                MainActivity.globalHabitList = gson.fromJson(json, type);
            }

            // Avoid NullPointerException if SharedPreferences was empty
            if (MainActivity.globalHabitList == null) {
                MainActivity.globalHabitList = new ArrayList<>();
            }
        }
    }

    private void loadHistoryData() {
        ensureDataLoaded();

        historyList = new ArrayList<>();

        // Ambil data yang sudah selesai
        if (MainActivity.globalHabitList != null) {
            for (Habit h : MainActivity.globalHabitList) {
                if (h.isCompletedForToday()) {
                    historyList.add(h);
                }
            }
        }

        adapter = new HabitAdapter(historyList);

        // PENTING: Aktifkan Mode History agar ikon sampah MUNCUL
        adapter.setHistoryMode(true);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);

        // Pasang aksi saat ikon SAMPAH diklik
        adapter.setOnDeleteClickListener(habit -> {
            showDeleteDialog(habit);
        });
    }

    // Validasi Hapus
    private void showDeleteDialog(Habit habitToDelete) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Riwayat")
                .setMessage("Yakin ingin menghapus permanen?")
                .setIcon(android.R.drawable.ic_dialog_alert) // Ikon peringatan
                .setPositiveButton("Hapus", (dialog, which) -> {
                    // 1. Hapus dari database GLOBAL
                    if (MainActivity.globalHabitList != null) {
                        MainActivity.globalHabitList.remove(habitToDelete);
                    }

                    // 2. Hapus dari tampilan saat ini
                    historyList.remove(habitToDelete);
                    adapter.notifyDataSetChanged();

                    saveDataLocally();

                    Toast.makeText(this, "Berhasil dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Helper to save data
    private void saveDataLocally() {
        SharedPreferences sharedPreferences = getSharedPreferences("EcoHabitData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(MainActivity.globalHabitList);
        editor.putString("habit_list", json);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryData();
    }
}