package com.example.ecohabit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("TITLE");
        String category = intent.getStringExtra("CATEGORY");
        int habitId = intent.getIntExtra("ID", 0);
        boolean isActionRequired = intent.getBooleanExtra("IS_ACTION", true);

        // EDUCATIONAL NOTIFICATION ENGINE
        // Instead of a static message, we fetch a random fact
        String messageBody = (category != null ? category : "Pengingat")+ " • " + getEcoFact(category);
//        String messageBody = (category != null ? category : "Pengingat") + " • Waktunya bertindak!";

        // NAVIGATION STACK LOGIC
        // 1. Create Intents for both screens
        Intent tapIntent = new Intent(context, MainActivity.class);
        Intent welcomeIntent = new Intent(context, WelcomeActivity.class);

        // 2. Use TaskStackBuilder to stack them: Welcome (Bottom) -> Main (Top)
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(welcomeIntent); // Adds Welcome to the stack first
        stackBuilder.addNextIntent(tapIntent);     // Adds Main on top of Welcome

        // 3. Generate the PendingIntent from the stack
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ecohabit_channel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // --- ACTION BUTTON LOGIC ---
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
            builder.addAction(android.R.drawable.ic_menu_save, "Selesai!", donePendingIntent);

        } else {
            // Log to history automatically if no action is required
            logToHistoryAutomatically(context, habitId, title);
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

    // --- HELPER: EDUCATIONAL FACTS GENERATOR ---
    private String getEcoFact(String category) {
        if (category == null) return "Waktunya bertindak untuk bumi! 🌱";

        String[] facts;

        switch (category) {
            case "Hemat Energi":
                facts = new String[]{
                        "💡 Tahukah kamu? Mematikan lampu saat keluar ruangan bisa mengurangi emisi karbon signifikan.",
                        "🔌 Info: Charger yang tidak dicabut tetap menyedot listrik (vampire power). Cabut yuk!",
                        "🌬️ Tips: Gunakan kipas angin daripada AC, hemat energi hingga 90%!",
                        "⚡ Fakta: Peralatan elektronik tua memakan daya 50% lebih banyak."
                };
                break;
            case "Kurangi Sampah":
                facts = new String[]{
                        "🛍️ Fakta: Kantong plastik butuh 10-1000 tahun untuk terurai. Bawa tas belanjamu sendiri!",
                        "🥤 Tips: Hindari sedotan plastik, gunakan stainless atau bambu.",
                        "📄 Info: Kertas bisa didaur ulang hingga 5-7 kali. Jangan langsung buang!",
                        "🍱 Tips: Bawa tempat makan sendiri (Tumbler/Lunchbox) kurangi sampah styrofoam."
                };
                break;
            case "Hemat Air":
                facts = new String[]{
                        "🚰 Fakta: Menutup keran saat sikat gigi bisa hemat 6 liter air per menit!",
                        "🚿 Tips: Mandi dengan shower lebih hemat air 3x lipat daripada gayung.",
                        "🌧️ Info: Air hujan bisa ditampung untuk menyiram tanaman. Gratis & ramah lingkungan!",
                        "💧 Fakta: Keran bocor yang menetes bisa membuang 20 liter air sehari."
                };
                break;
            case "Transportasi Hijau":
                facts = new String[]{
                        "🚶 Tips: Jalan kaki 30 menit sehari mengurangi risiko penyakit jantung & polusi.",
                        "🚌 Fakta: Satu bus penuh bisa menggantikan 40 mobil pribadi di jalan raya.",
                        "🚲 Tips: Bersepeda ke tempat dekat? Sehat badan, sehat bumi!",
                        "🚗 Info: Berkendara agresif (rem mendadak/gas pol) boros bensin 30%."
                };
                break;
            default:
                return "Satu langkah kecilmu berarti besar bagi masa depan bumi. 🌱";
        }

        // Pick a random fact from the category list
        return facts[new Random().nextInt(facts.length)];
    }

    private void logToHistoryAutomatically(Context context, int habitId, String title) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("EcoHabitData", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Habit>>() {}.getType();

        String jsonHabit = sharedPreferences.getString("habit_list", null);
        ArrayList<Habit> habitList = null;
        if (jsonHabit != null) habitList = gson.fromJson(jsonHabit, type);

        if (habitList != null) {
            for (Habit h : habitList) {
                if (h.getId() == habitId) {
                    Habit historyItem = new Habit(h.getTitle(), h.getCategory(), h.getHour(), h.getMinute(), false);
                    historyItem.markAsCompletedToday();

                    String jsonHistory = sharedPreferences.getString("history_list", null);
                    ArrayList<Habit> historyList = null;
                    if (jsonHistory != null) historyList = gson.fromJson(jsonHistory, type);
                    if (historyList == null) historyList = new ArrayList<>();

                    historyList.add(historyItem);

                    // Also update global list to prevent sync issues
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
}