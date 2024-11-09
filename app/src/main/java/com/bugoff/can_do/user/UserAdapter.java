package com.bugoff.can_do.user;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.R;

import java.util.List;

/**
 * Adapter for the RecyclerView in BrowseProfilesFragment.
 * This adapter binds user data to the ViewHolder.
 */
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

    /** Called by RecyclerView to display the data at the specified position. */
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

    /** ViewHolder for the RecyclerView */
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

        /** Bind user data to the ViewHolder */
        public void bind(User user) {
            Log.d("UserAdapter", "Binding user: " + user.getId() + ", name: " + user.getName()
                    + ", email: " + user.getEmail() + ", phone: " + user.getPhoneNumber());
            textViewName.setText(
                    !TextUtils.isEmpty(user.getName()) ? user.getName() : "No Name"
            );
            textViewEmail.setText(
                    !TextUtils.isEmpty(user.getEmail()) ? user.getEmail() : "No Email"
            );
            textViewPhone.setText(
                    !TextUtils.isEmpty(user.getPhoneNumber()) ? user.getPhoneNumber() : "No Phone"
            );
        }
    }
}
