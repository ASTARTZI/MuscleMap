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

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private final List<Map<String, Object>> friends;
    private final OnFriendActionListener listener;

    public interface OnFriendActionListener {
        void onShareProgram(String friendUid);
        void onViewProgram(String friendUid);
    }

    public FriendAdapter(List<Map<String, Object>> friends, OnFriendActionListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> friend = friends.get(position);
        String email = (String) friend.get("email");
        String uid = (String) friend.get("uid");
        holder.tvEmail.setText(email);

        holder.btnShare.setOnClickListener(v -> listener.onShareProgram(uid));
        holder.btnView.setOnClickListener(v -> listener.onViewProgram(uid));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail;
        Button btnShare, btnView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tv_friend_email);
            btnShare = itemView.findViewById(R.id.btn_share_to_friend);
            btnView = itemView.findViewById(R.id.btn_view_friend_program);
        }
    }
}
