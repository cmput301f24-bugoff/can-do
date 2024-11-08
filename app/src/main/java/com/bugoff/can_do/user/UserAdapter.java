package com.bugoff.can_do.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bugoff.can_do.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;

    // Constructor
    public UserAdapter(List<User> users) {
        this.users = users;
    }

    // Update the list and notify the adapter
    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item_user layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        // Bind user data to the ViewHolder
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    // ViewHolder class
    static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewEmail;
        TextView textViewPhone;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_user_name);
            textViewEmail = itemView.findViewById(R.id.text_view_user_email);
            textViewPhone = itemView.findViewById(R.id.text_view_user_phone);
        }

        public void bind(User user) {
            textViewName.setText(user.getName());
            textViewEmail.setText(user.getEmail());
            textViewPhone.setText(user.getPhoneNumber());
            // display counts of eventsJoined and eventsEnrolled?
        }
    }
}
