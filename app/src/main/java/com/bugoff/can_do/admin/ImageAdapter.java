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
/**
 * Adapter for displaying images in a RecyclerView.
 * This adapter can display both Event and User objects.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Object> items; // Can contain both Event and User objects
    private OnDeleteClickListener deleteClickListener;
    /**
     * Interface to handle delete button clicks.
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(Object item, boolean isEvent);
    }
    /**
     * Creates a new ImageAdapter with the given listener.
     *
     * @param listener The listener for delete button clicks.
     */
    public ImageAdapter(OnDeleteClickListener listener) {
        this.items = new ArrayList<>();
        this.deleteClickListener = listener;
    }
    /**
     * Inflates the item layout and creates a new ViewHolder.
     *
     * @param parent The parent view.
     * @param viewType The view type.
     * @return A new ImageViewHolder.
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_browse_image, parent, false);
        return new ImageViewHolder(view);
    }
    /**
     * Binds the item at the given position to the ViewHolder.
     *
     * @param holder The ViewHolder.
     * @param position The position of the item.
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Object item = items.get(position);
        holder.bind(item, deleteClickListener);
    }
    /**
     * Returns the number of items in the adapter.
     *
     * @return The number of items.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }
    /**
     * Updates the items in the adapter with the given list.
     *
     * @param newItems The new list of items.
     */
    public void updateItems(List<Object> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
    /**
     * Adds the given list of items to the adapter.
     *
     * @param newItems The list of items to add.
     */
    public void addItems(List<?> newItems) {
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }
    /**
     * ViewHolder for displaying images in a RecyclerView.
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ImageButton deleteButton;
        private final TextView nameText;
        /**
         * Creates a new ImageViewHolder with the given view.
         *
         * @param itemView The view for the ViewHolder.
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.event_image);
            deleteButton = itemView.findViewById(R.id.button_delete_image);
            nameText = itemView.findViewById(R.id.text_event_name);
        }
        /**
         * Binds the given item to the ViewHolder.
         *
         * @param item The item to bind.
         * @param listener The listener for delete button clicks.
         */
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