package com.example.muscleapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class ReceivedProgramAdapter extends RecyclerView.Adapter<ReceivedProgramAdapter.ViewHolder> {

    private final List<Map<String, Object>> programs;
    private final OnImportListener listener;

    public interface OnImportListener {
        void onImport(String json);
    }

    public ReceivedProgramAdapter(List<Map<String, Object>> programs, OnImportListener listener) {
        this.programs = programs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_received_program, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> data = programs.get(position);
        String from = (String) data.get("from_email");
        String json = (String) data.get("program_json");
        holder.tvFrom.setText("From: " + from);
        holder.btnImport.setOnClickListener(v -> listener.onImport(json));
    }

    @Override
    public int getItemCount() {
        return programs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFrom;
        Button btnImport;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFrom = itemView.findViewById(R.id.tv_received_from);
            btnImport = itemView.findViewById(R.id.btn_import_received);
        }
    }
}
