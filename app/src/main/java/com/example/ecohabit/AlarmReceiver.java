package com.example.ecohabit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Ambil data judul dan pesan yang dikirim dari MainActivity
        String title = intent.getStringExtra("TITLE");
//        String message = intent.getStringExtra("MESSAGE");
        int habitId = intent.getIntExtra("ID", 0);
        boolean isActionRequired = intent.getBooleanExtra("IS_ACTION", true);

        String message = "Waktunya untuk: " + title;

//        // Tampilkan Notifikasi
//        showNotification(context, title, message);

        Intent tapIntent = new Intent(context, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ecohabit_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // "DONE" BUTTON LOGIC
        if (isActionRequired) {
            Intent doneIntent = new Intent(context, HabitActionReceiver.class);
            doneIntent.setAction("ACTION_DONE");
            doneIntent.putExtra("HABIT_ID", habitId);

            PendingIntent donePendingIntent = PendingIntent.getBroadcast(
                    context,
                    habitId,
                    doneIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Add Button: "Selesai"
            builder.addAction(android.R.drawable.ic_menu_save, "Selesai!", donePendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "ecohabit_channel", "Notifikasi EcoHabit", NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(habitId, builder.build());
    }

//    private void showNotification(Context context, String title, String message) {
//        String CHANNEL_ID = "ecohabit_channel";
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // 1. Buat Channel (Wajib untuk Android O ke atas)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    CHANNEL_ID,
//                    "Notifikasi EcoHabit",
//                    NotificationManager.IMPORTANCE_HIGH
//            );
//            notificationManager.createNotificationChannel(channel);
//        }
//
//        // 2. Buat Intent agar kalau notifikasi diklik, buka aplikasi
//        Intent tapIntent = new Intent(context, MainActivity.class);
//        tapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                context, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE
//        );
//
//        // 3. Bangun Tampilan Notifikasi
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                .setSmallIcon(android.R.drawable.ic_dialog_info) // Ikon kecil
//                .setContentTitle(title)
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);
//
//        // 4. Munculkan!
//        // Gunakan waktu sekarang sebagai ID unik agar notifikasi bisa menumpuk
//        int notificationId = (int) System.currentTimeMillis();
//        notificationManager.notify(notificationId, builder.build());
//    }
}