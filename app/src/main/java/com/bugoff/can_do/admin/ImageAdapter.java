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

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<Event> eventsWithImages;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    public ImageAdapter(List<Event> eventsWithImages, OnDeleteClickListener listener) {
        this.eventsWithImages = eventsWithImages;
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
        Event event = eventsWithImages.get(position);
        holder.bind(event, deleteClickListener);
    }

    @Override
    public int getItemCount() {
        return eventsWithImages != null ? eventsWithImages.size() : 0;
    }

    public void updateEvents(List<Event> events) {
        this.eventsWithImages = events;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView eventImage;
        private final ImageButton deleteButton;
        private final TextView eventName;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.event_image);
            deleteButton = itemView.findViewById(R.id.button_delete_image);
            eventName = itemView.findViewById(R.id.text_event_name);
        }

        public void bind(Event event, OnDeleteClickListener listener) {
            if (event.getBase64Image() != null) {
                Bitmap bitmap = ImageUtils.decodeBase64Image(event.getBase64Image());
                if (bitmap != null) {
                    eventImage.setVisibility(View.VISIBLE);
                    eventImage.setImageBitmap(bitmap);
                } else {
                    eventImage.setVisibility(View.GONE);
                }
            } else {
                eventImage.setVisibility(View.GONE);
            }

            eventName.setText(event.getName());

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(event);
                }
            });
        }
    }
}