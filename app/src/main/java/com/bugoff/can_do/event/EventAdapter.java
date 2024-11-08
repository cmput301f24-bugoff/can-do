package com.bugoff.can_do.event;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.R;
import com.bugoff.can_do.organizer.EventDetailsActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private static List<Event> eventList;
    private boolean isAdmin;
    private OnDeleteClickListener deleteClickListener;

    // Interface for handling delete button clicks
    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    public EventAdapter(List<Event> eventList, boolean isAdmin, OnDeleteClickListener deleteClickListener) {
        this.eventList = eventList;
        this.isAdmin = isAdmin;
        this.deleteClickListener = deleteClickListener;
    }

    // Update the list and notify the adapter
    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
        notifyDataSetChanged();
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event, isAdmin, deleteClickListener);
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    // ViewHolder class
    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewDescription;
        TextView textViewDates;
        TextView textViewParticipants;
        ImageButton buttonDelete;

        private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_event_name);
            textViewDescription = itemView.findViewById(R.id.text_view_event_description);
            textViewDates = itemView.findViewById(R.id.text_view_event_dates);
            textViewParticipants = itemView.findViewById(R.id.text_view_num_participants);
            buttonDelete = itemView.findViewById(R.id.button_delete_event);
        }

        public void bind(final Event event, boolean isAdmin, final OnDeleteClickListener listener) {
            textViewName.setText(event.getName());
            textViewDescription.setText(event.getDescription());

            // Format dates with time
            String registrationStart = event.getRegistrationStartDate() != null ? dateTimeFormat.format(event.getRegistrationStartDate()) : "N/A";
            String registrationEnd = event.getRegistrationEndDate() != null ? dateTimeFormat.format(event.getRegistrationEndDate()) : "N/A";
            String eventStart = event.getEventStartDate() != null ? dateTimeFormat.format(event.getEventStartDate()) : "N/A";
            String eventEnd = event.getEventEndDate() != null ? dateTimeFormat.format(event.getEventEndDate()) : "N/A";

            String dates = "Registration: " + registrationStart + " - " + registrationEnd +
                    " | Event: " + eventStart + " - " + eventEnd;
            textViewDates.setText(dates);

            // Display current number of participants out of max number of participants
            String participants = event.getEnrolledEntrants().size() + " / " + event.getMaxNumberOfParticipants();
            textViewParticipants.setText(participants);

            // Set delete button visibility
            if (isAdmin) {
                buttonDelete.setVisibility(View.VISIBLE);
            } else {
                buttonDelete.setVisibility(View.GONE);
            }

            // Set click listener for delete button
            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(event);
                }
            });

            // Set click listener for the entire item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Event event2 = eventList.get(position);
                    Intent intent = new Intent(itemView.getContext(), EventDetailsActivity.class);
                    intent.putExtra("selected_event_id", event2.getId());
                    itemView.getContext().startActivity(intent);
                }
            });

        }
    }
}
