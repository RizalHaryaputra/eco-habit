package com.example.ecohabit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

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

        // --- LOGIC: ACTION VS GENERAL ---
        if (isActionRequired) {
            // IF ACTION: Show "Done" Button
            Intent doneIntent = new Intent(context, HabitActionReceiver.class);
            doneIntent.setAction("ACTION_DONE");
            doneIntent.putExtra("HABIT_ID", habitId);

            PendingIntent donePendingIntent = PendingIntent.getBroadcast(
                    context,
                    habitId,
                    doneIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.addAction(android.R.drawable.ic_menu_save, "Selesai!", donePendingIntent);

        } else {
            // IF GENERAL: Log to History Automatically!
            logToHistoryAutomatically(context, habitId, title);
        }

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

    private void logToHistoryAutomatically(Context context, int habitId, String title) {
        // We need to load data safely, find the habit, clone it to history, and save.
        SharedPreferences sharedPreferences = context.getSharedPreferences("EcoHabitData", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Habit>>() {}.getType();

        // 1. Load Active List to find details
        String jsonHabit = sharedPreferences.getString("habit_list", null);
        ArrayList<Habit> habitList = null;
        if (jsonHabit != null) habitList = gson.fromJson(jsonHabit, type);

        if (habitList != null) {
            for (Habit h : habitList) {
                if (h.getId() == habitId) {

                    // 2. Clone to History
                    Habit historyItem = new Habit(h.getTitle(), h.getCategory(), h.getHour(), h.getMinute(), false);
                    historyItem.markAsCompletedToday();

                    // 3. Load History List
                    String jsonHistory = sharedPreferences.getString("history_list", null);
                    ArrayList<Habit> historyList = null;
                    if (jsonHistory != null) historyList = gson.fromJson(jsonHistory, type);
                    if (historyList == null) historyList = new ArrayList<>();

                    // 4. Add and Save
                    historyList.add(historyItem);

                    // Update global list if app is running
                    if (MainActivity.globalHistoryList != null) {
                        MainActivity.globalHistoryList.add(historyItem);
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("history_list", gson.toJson(historyList));
                    editor.apply();
                    break;
                }
            }
        }
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