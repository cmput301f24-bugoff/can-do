package com.bugoff.can_do.admin;

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
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.user.User;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Object> items; // Can contain both Event and User objects
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Object item, boolean isEvent);
    }

    public ImageAdapter(OnDeleteClickListener listener) {
        this.items = new ArrayList<>();
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_browse_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Object item = items.get(position);
        holder.bind(item, deleteClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<Object> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void addItems(List<?> newItems) {
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ImageButton deleteButton;
        private final TextView nameText;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.event_image);
            deleteButton = itemView.findViewById(R.id.button_delete_image);
            nameText = itemView.findViewById(R.id.text_event_name);
        }

        public void bind(Object item, OnDeleteClickListener listener) {
            String base64Image;
            String name;
            boolean isEvent;

            if (item instanceof Event) {
                Event event = (Event) item;
                base64Image = event.getBase64Image();
                name = event.getName();
                isEvent = true;
                name = "Event: " + name;
            } else if (item instanceof User) {
                User user = (User) item;
                base64Image = user.getBase64Image();
                name = user.getName();
                isEvent = false;
                name = "User: " + name;
            } else {
                return;
            }

            if (base64Image != null) {
                Bitmap bitmap = ImageUtils.decodeBase64Image(base64Image);
                if (bitmap != null) {
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setVisibility(View.GONE);
                }
            } else {
                imageView.setVisibility(View.GONE);
            }

            nameText.setText(name);

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(item, isEvent);
                }
            });
        }
    }
}