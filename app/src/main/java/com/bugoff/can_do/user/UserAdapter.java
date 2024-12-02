package com.bugoff.can_do.user;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.ImageUtils;
import com.bugoff.can_do.R;

import java.util.List;
/**
 * Adapter for displaying a list of users in a RecyclerView.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users;
    private final OnDeleteClickListener deleteClickListener;
    private final boolean isAdminDeletion;
    private final boolean showDeleteButton;

    public interface OnDeleteClickListener {
        void onDeleteClick(User user);
    }

    /**
     * Constructor for admin user deletion functionality
     * @param users List of users to display
     * @param listener Listener for delete actions
     */
    public UserAdapter(List<User> users, OnDeleteClickListener listener) {
        this(users, listener, false, false); // Default to no deletion functionality
    }

    /**
     * Constructor for organizer list removal functionality
     * @param users List of users to display
     * @param listener Listener for delete actions
     * @param isAdminDeletion Whether this adapter is being used for admin deletion
     * @param showDeleteButton Whether to show the delete button
     */
    public UserAdapter(List<User> users, OnDeleteClickListener listener, boolean isAdminDeletion, boolean showDeleteButton) {
        this.users = users;
        this.deleteClickListener = listener;
        this.isAdminDeletion = isAdminDeletion;
        this.showDeleteButton = showDeleteButton;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, deleteClickListener, showDeleteButton);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView userAvatar;
        private final TextView userName;
        private final TextView userEmail;
        private final TextView userPhone;
        private final ImageButton deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.image_view_user_avatar);
            userName = itemView.findViewById(R.id.text_view_user_name);
            userEmail = itemView.findViewById(R.id.text_view_user_email);
            userPhone = itemView.findViewById(R.id.text_view_user_phone);
            deleteButton = itemView.findViewById(R.id.button_delete_user);
        }

        public void bind(User user, OnDeleteClickListener listener, boolean showDeleteButton) {
            userName.setText(user.getName());
            userEmail.setText(user.getEmail());
            userPhone.setText(user.getPhoneNumber());

            // Set delete button visibility
            deleteButton.setVisibility(showDeleteButton ? View.VISIBLE : View.GONE);

            // Set up delete button
            if (showDeleteButton && listener != null) {
                deleteButton.setOnClickListener(v -> listener.onDeleteClick(user));
            } else {
                deleteButton.setOnClickListener(null);
            }

            // Load and set user avatar
            String base64Image = user.getBase64Image();
            if (base64Image != null && !base64Image.isEmpty()) {
                Bitmap bitmap = ImageUtils.decodeBase64Image(base64Image);
                if (bitmap != null) {
                    userAvatar.setImageBitmap(bitmap);
                } else {
                    setDefaultAvatar(user);
                }
            } else {
                setDefaultAvatar(user);
            }
        }

        private void setDefaultAvatar(User user) {
            String name = user.getName();
            String firstLetter = name != null && !name.isEmpty()
                    ? name.substring(0, 1).toUpperCase()
                    : "?";

            Bitmap defaultAvatar = ImageUtils.generateDefaultAvatar(firstLetter);
            userAvatar.setImageBitmap(defaultAvatar);
        }
    }
}
