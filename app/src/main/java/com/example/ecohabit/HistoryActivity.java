package com.example.ecohabit;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
        loadHistoryData();
    }

    private void loadHistoryData() {
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
                    MainActivity.globalHabitList.remove(habitToDelete);

                    // 2. Hapus dari tampilan saat ini
                    historyList.remove(habitToDelete);
                    adapter.notifyDataSetChanged();

                    Toast.makeText(this, "Berhasil dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryData();
    }
}