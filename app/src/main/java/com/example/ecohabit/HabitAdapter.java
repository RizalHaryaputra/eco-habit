package com.example.ecohabit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView; // Import ImageView
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

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
        holder.tvDate.setText(habit.getDateTime());

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

        // Logic Checkbox
        holder.cbDone.setOnCheckedChangeListener(null);
        holder.cbDone.setChecked(habit.isCompleted());
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            habit.setCompleted(isChecked);
        });
    }

    @Override
    public int getItemCount() { return habitList.size(); }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvDate;
        CheckBox cbDone;
        ImageView btnDelete; // Variabel baru untuk ikon sampah

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHabitTitle);
            tvCategory = itemView.findViewById(R.id.tvHabitCategory);
            tvDate = itemView.findViewById(R.id.tvHabitDate);
            cbDone = itemView.findViewById(R.id.cbHabitDone);
            btnDelete = itemView.findViewById(R.id.btnDelete); // Hubungkan dengan XML
        }
    }
}