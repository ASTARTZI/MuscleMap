package com.example.muscleapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class PlanExerciseAdapter extends RecyclerView.Adapter<PlanExerciseAdapter.ViewHolder> {

    private final Context context;
    private final List<ExerciseItem> items;
    private final OnOptionsClickListener optionsClickListener;
    private final OnItemClickListener itemClickListener;
    private boolean isViewOnly = false;
    private final ExerciseDBHandler dbHandler;
    private final String currentLang;

    public interface OnOptionsClickListener {
        void onOptionsClick(int position);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public PlanExerciseAdapter(Context context, List<ExerciseItem> items, OnOptionsClickListener optionsClickListener, OnItemClickListener itemClickListener) {
        this.context = context;
        this.items = items;
        this.optionsClickListener = optionsClickListener;
        this.itemClickListener = itemClickListener;
        this.dbHandler = new ExerciseDBHandler(context);
        this.currentLang = getCurrentLang();
    }

    private String getCurrentLang() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        String lang;
        if (!locales.isEmpty()) {
            Locale locale = locales.get(0);
            lang = (locale != null) ? locale.getLanguage() : "en";
        } else {
            lang = Locale.getDefault().getLanguage();
        }
        return "el".equals(lang) ? "el" : "en";
    }

    public void setViewOnly(boolean viewOnly) {
        this.isViewOnly = viewOnly;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_plan_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseItem item = items.get(position);
        
        // Dynamic localized title lookup
        Exercise localized = dbHandler.getExerciseByImageName(item.getImageName(), currentLang);
        if (localized != null) {
            holder.titleTV.setText(localized.getTitle());
        } else {
            holder.titleTV.setText(item.getTitle()); // Fallback to stored title
        }

        if (item.getSets() > 0 || item.getReps() > 0 || item.getWeight() > 0) {
            holder.statsTV.setVisibility(View.VISIBLE);
            holder.statsTV.setText(context.getString(R.string.stats_format, 
                    item.getSets(), item.getReps(), item.getWeight(), item.getWeightUnit()));
        } else {
            holder.statsTV.setVisibility(View.GONE);
        }

        String img = item.getImageName();
        if (img != null && (img.startsWith("content://") || img.startsWith("file://"))) {
            Glide.with(context)
                    .load(img)
                    .placeholder(R.drawable.ic_placeholder)
                    .fitCenter()
                    .into(holder.imageView);
        } else {
            int resId = context.getResources().getIdentifier(
                    img, "drawable", context.getPackageName());

            if (resId != 0) {
                Glide.with(context)
                        .load(resId)
                        .placeholder(R.drawable.ic_placeholder)
                        .fitCenter()
                        .into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.drawable.ic_placeholder);
            }
        }

        if (isViewOnly) {
            holder.optionsBtn.setVisibility(View.GONE);
        } else {
            holder.optionsBtn.setVisibility(View.VISIBLE);
            holder.optionsBtn.setOnClickListener(v -> {
                if (optionsClickListener != null) {
                    optionsClickListener.onOptionsClick(holder.getAbsoluteAdapterPosition());
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(holder.getAbsoluteAdapterPosition());
            }
        });
    }

    public void addItem(ExerciseItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTV, statsTV;
        ImageButton optionsBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.plan_exercise_image);
            titleTV = itemView.findViewById(R.id.plan_exercise_title);
            statsTV = itemView.findViewById(R.id.plan_exercise_stats);
            optionsBtn = itemView.findViewById(R.id.plan_exercise_options_btn);
        }
    }
}
