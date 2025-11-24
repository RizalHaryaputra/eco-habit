package com.example.ecohabit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Inisialisasi Tombol
        Button btnStart = findViewById(R.id.btnGetStarted);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnAboutUs = findViewById(R.id.btnAboutUs);

        // 1. AKSI TOMBOL MULAI -> Ke Halaman Input (MainActivity)
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // 2. AKSI TOMBOL RIWAYAT -> Ke Halaman Riwayat (HistoryActivity)
        // (Bagian ini yang kita perbaiki agar tidak cuma muncul pesan doang)
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        btnAboutUs.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }
}