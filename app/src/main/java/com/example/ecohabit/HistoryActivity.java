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
import java.util.Collections;

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
            Type type = new TypeToken<ArrayList<Habit>>() {}.getType();

            // Load active habits list
            String jsonHabit = sharedPreferences.getString("habit_list", null);
            if (jsonHabit != null) MainActivity.globalHabitList = gson.fromJson(jsonHabit, type);
            // Avoid NullPointerException if SharedPreferences was empty
            if (MainActivity.globalHabitList == null) MainActivity.globalHabitList = new ArrayList<>();

            // Load history list
            String jsonHistory = sharedPreferences.getString("history_list", null);
            if (jsonHistory != null) MainActivity.globalHistoryList = gson.fromJson(jsonHistory, type);
            if (MainActivity.globalHistoryList == null) MainActivity.globalHistoryList = new ArrayList<>();
        }
    }

    private void loadHistoryData() {
        ensureDataLoaded();

        historyList = new ArrayList<>();

        // Ambil data yang sudah selesai
        if (MainActivity.globalHistoryList != null) {
            historyList.addAll(MainActivity.globalHistoryList);
        }

        Collections.reverse(historyList);

        adapter = new HabitAdapter(historyList);
        // Aktifkan Mode History agar ikon sampah MUNCUL
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
                    // 1. Hapus dari database history global
                    if (MainActivity.globalHistoryList != null) {
                        MainActivity.globalHistoryList.remove(habitToDelete);
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
        // Save both lists to be safe, though we only modified history
        String jsonHabit = gson.toJson(MainActivity.globalHabitList);
        editor.putString("habit_list", jsonHabit);

        String jsonHistory = gson.toJson(MainActivity.globalHistoryList);
        editor.putString("history_list", jsonHistory);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryData();
    }
}