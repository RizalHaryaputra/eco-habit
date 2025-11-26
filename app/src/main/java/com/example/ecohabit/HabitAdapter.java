package com.example.ecohabit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView; // Import ImageView
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private ArrayList<Habit> habitList;
    private OnDeleteClickListener deleteListener; // Listener baru khusus tombol sampah
    private boolean isHistoryPage = false; // Penanda apakah ini halaman history

    // Interface
    public interface OnDeleteClickListener {
        void onDeleteClick(Habit habit);
    }

    public HabitAdapter(ArrayList<Habit> habitList) {
        this.habitList = habitList;
    }

    // Method untuk mengaktifkan mode History (memunculkan tombol sampah)
    public void setHistoryMode(boolean isHistory) {
        this.isHistoryPage = isHistory;
    }

    // Method untuk memasang listener tombol sampah
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habitList.get(position);
        
        holder.tvTitle.setText(habit.getTitle());
        holder.tvCategory.setText(habit.getCategory());

        // BADGE LOGIC
        if (isHistoryPage) {
            // Hide badges in history to keep it clean, or keep them if you prefer
            holder.tvBadgeRepeat.setVisibility(View.GONE);
            holder.tvBadgeType.setVisibility(View.GONE);
        } else {
            // 1. Repeat Badge
            if (habit.isRepeating()) {
                holder.tvBadgeRepeat.setVisibility(View.VISIBLE);
                holder.tvBadgeRepeat.setText("↻ Rutin");
            } else {
                holder.tvBadgeRepeat.setVisibility(View.VISIBLE);
                holder.tvBadgeRepeat.setText("1x Sekali");
            }

            // 2. Type Badge
            holder.tvBadgeType.setVisibility(View.VISIBLE);
            if (habit.isActionRequired()) {
                holder.tvBadgeType.setText("⚡ Aksi");
                holder.tvBadgeType.setTextColor(android.graphics.Color.parseColor("#EF6C00")); // Orange
            } else {
                holder.tvBadgeType.setText("ℹ️ Info");
                holder.tvBadgeType.setTextColor(android.graphics.Color.parseColor("#1976D2")); // Blue
            }
        }

        // DATE DISPLAY LOGIC
        if (isHistoryPage) {
            // Display Format: "Selesai: 2023-11-26, 08:00"
            String completedDate = habit.getLastCompletedDate();
            if (completedDate == null) completedDate = "";
            holder.tvDate.setText("Selesai: " + completedDate + ", " + habit.getFormattedTime());
        } else {
            // Show "Acak" if it's a random time
            if (habit.isRandom()) {
                holder.tvDate.setText("Waktu Acak (" + habit.getFormattedTime() + ")");
            } else {
                holder.tvDate.setText(habit.getFormattedTime());
            }
        }

        // SWITCH LOGIC (Alarm Activation)
        // Detach listener first to avoid loops
        holder.swAlarm.setOnCheckedChangeListener(null);
        holder.swAlarm.setChecked(habit.isActive());

        if (isHistoryPage) {
            holder.swAlarm.setVisibility(View.GONE); // Hide switch in history
        } else {
            holder.swAlarm.setVisibility(View.VISIBLE);
            holder.swAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
                habit.setActive(isChecked);

                // Call MainActivity to update Alarm Manager
                if (holder.itemView.getContext() instanceof MainActivity) {
                    ((MainActivity) holder.itemView.getContext()).updateAlarmState(habit);
                }

                // Refresh to show/hide "Done" button based on new active state
                notifyItemChanged(position);
            });
        }

        // LOGIKA TOMBOL SAMPAH
        if (isHistoryPage) {
            holder.btnDelete.setVisibility(View.VISIBLE); // Munculkan jika di history
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(habit);
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE); // Sembunyikan jika di halaman utama
        }

        // DONE BUTTON LOGIC (Visibility)
        if (isHistoryPage) {
            holder.btnDone.setVisibility(View.GONE);
        } else {
            // Check if we should show the button
            if (shouldShowDoneButton(habit)) {
                holder.btnDone.setVisibility(View.VISIBLE);
                holder.btnDone.setOnClickListener(v -> {
                    // Mark as Done
                    habit.markAsCompletedToday();

                    // Logic: If one-time use, turn off switch
                    if (!habit.isRepeating()) {
                        habit.setActive(false);
                        holder.swAlarm.setChecked(false);
                    }

                    // Create history snapshot
                    // We duplicate the habit to the History List
                    Habit historyItem = new Habit(habit.getTitle(), habit.getCategory(), habit.getHour(), habit.getMinute(), habit.isActionRequired());
                    historyItem.markAsCompletedToday(); // Sets the date on the snapshot
                    MainActivity.globalHistoryList.add(historyItem); // Add to history log

                    Toast.makeText(holder.itemView.getContext(), "Hebat! Tercatat di Riwayat.", Toast.LENGTH_SHORT).show();

                    // Refresh UI
                    if (holder.itemView.getContext() instanceof MainActivity) {
                        ((MainActivity) holder.itemView.getContext()).refreshData();
                    }
                });
            } else {
                holder.btnDone.setVisibility(View.GONE);
            }
        }

        // EDIT LOGIC (Click Body)
        if (!isHistoryPage) {
            holder.itemView.setOnClickListener(v -> {
                if (holder.itemView.getContext() instanceof MainActivity) {
                    ((MainActivity) holder.itemView.getContext()).showAddDialog(habit);
                }
            });
        }

        // VISIBILITY & STYLING
        if (isHistoryPage) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDeleteClick(habit);
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }

        // Logika Ganti Warna Icon/Text Berdasarkan Kategori
        if (habit.getCategory().contains("Air")) {
            holder.tvCategory.setTextColor(android.graphics.Color.BLUE);
        } else if (habit.getCategory().contains("Sampah")) {
            holder.tvCategory.setTextColor(android.graphics.Color.parseColor("#795548")); // Coklat
        } else if (habit.getCategory().contains("Energi")) {
            holder.tvCategory.setTextColor(android.graphics.Color.parseColor("#FF9800")); // Oranye
        } else {
            holder.tvCategory.setTextColor(android.graphics.Color.GREEN); // Default Hijau
        }
    }

    // Helper: Determine if "Done" button should appear
    private boolean shouldShowDoneButton(Habit habit) {
        // If Action is NOT required, NEVER show button
        if (!habit.isActionRequired()) return false;

        // 1. Must be Active
        if (!habit.isActive()) return false;

        // 2. Must NOT be done today already
        if (habit.isCompletedForToday()) return false;

        // 3. Must be PAST the alarm time
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        // Compare Hours
        if (currentHour > habit.getHour()) return true;

        // Compare Minutes if Hour is same
        if (currentHour == habit.getHour() && currentMinute >= habit.getMinute()) return true;

        return false;
    }

    @Override
    public int getItemCount() { return habitList.size(); }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvDate;
        TextView tvBadgeRepeat, tvBadgeType;
        SwitchCompat swAlarm; // Changed from CheckBox
        Button btnDone;       // New Button
        ImageView btnDelete; // Variabel baru untuk ikon sampah

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHabitTitle);
            tvCategory = itemView.findViewById(R.id.tvHabitCategory);
            tvDate = itemView.findViewById(R.id.tvHabitDate);
            tvBadgeRepeat = itemView.findViewById(R.id.tvBadgeRepeat);
            tvBadgeType = itemView.findViewById(R.id.tvBadgeType);
            swAlarm = itemView.findViewById(R.id.swAlarm);
            btnDone = itemView.findViewById(R.id.btnDoneAction);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}