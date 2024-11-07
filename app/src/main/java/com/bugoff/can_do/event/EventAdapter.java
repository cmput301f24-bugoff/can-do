package com.bugoff.can_do.event;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnItemClickListener listener;

    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public EventAdapter(List<Event> eventList, OnItemClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    // Update the list and notify the adapter
    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
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
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    // Make EventViewHolder public and static
    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewDescription;
        TextView textViewDates;
        TextView textViewParticipants;

        private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_event_name);
            textViewDescription = itemView.findViewById(R.id.text_view_event_description);
            textViewDates = itemView.findViewById(R.id.text_view_event_dates);
            textViewParticipants = itemView.findViewById(R.id.text_view_num_participants);
        }

        public void bind(final Event event, final OnItemClickListener listener) {
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

            String participants = event.getMaxNumberOfParticipants() + " / " + event.getMaxNumberOfParticipants() + " Participants";
            textViewParticipants.setText(participants);

            // Set click listener
            itemView.setOnClickListener(v -> listener.onItemClick(event));
        }
    }
}
