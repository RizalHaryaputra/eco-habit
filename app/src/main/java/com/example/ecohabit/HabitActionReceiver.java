package com.example.ecohabit;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.content.SharedPreferences;
import com.google.gson.Gson;

// Handles the "Done" button on the notification
public class HabitActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("ACTION_DONE".equals(intent.getAction())) {
            int habitId = intent.getIntExtra("HABIT_ID", -1);

            // 1. Dismiss Notification
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(habitId);

            // 2. Update Data
            if (MainActivity.globalHabitList != null) {
                for (Habit h : MainActivity.globalHabitList) {
                    if (h.getId() == habitId) {
                        h.markAsCompletedToday();

                        // If NOT repeating, deactivate it so it hides from main list
                        if (!h.isRepeating()) {
                            h.setActive(false);
                        }

                        Toast.makeText(context, "Hebat! " + h.getTitle() + " tercatat selesai.", Toast.LENGTH_SHORT).show();

                        // 3. Force Save
                        saveDataLocally(context);
                        break;
                    }
                }
            }
        }
    }

    private void saveDataLocally(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("EcoHabitData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(MainActivity.globalHabitList);
        editor.putString("habit_list", json);
        editor.apply();
    }
}