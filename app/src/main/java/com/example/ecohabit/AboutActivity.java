package com.example.ecohabit;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Mengubah judul di action bar atas (opsional)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tentang Kami");
        }
    }
}