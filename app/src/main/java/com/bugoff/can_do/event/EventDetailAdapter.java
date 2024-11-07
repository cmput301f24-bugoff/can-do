package com.bugoff.can_do.event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.R;
import com.bugoff.can_do.user.User;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventDetailAdapter extends RecyclerView.Adapter<EventDetailAdapter.EventDetailViewHolder> {

    private final Event event;
    private final Context context;
    private final List<User> entrants;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());

    public EventDetailAdapter(Context context, Event event, List<User> entrants) {
        this.context = context;
        this.event = event;
        this.entrants = entrants;
    }

    @NonNull
    @Override
    public EventDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_detail, parent, false);
        return new EventDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventDetailViewHolder holder, int position) {
        // Set event name, description, and date details
        holder.eventTitle.setText(event.getName());
        holder.eventDescription.setText(event.getDescription());

        // Format and set the event dates
        String registrationDates = String.format("Registration: %s - %s",
                dateTimeFormat.format(event.getRegistrationStartDate()),
                dateTimeFormat.format(event.getRegistrationEndDate())
        );

        String eventDates = String.format("Event: %s - %s",
                dateTimeFormat.format(event.getEventStartDate()),
                dateTimeFormat.format(event.getEventEndDate())
        );

        holder.eventDate.setText(registrationDates + " | " + eventDates);

        // Set facility location and max participants
        holder.eventLocation.setText(String.format("Location: %s", event.getFacility().getName()));

        String participantsText = event.getMaxNumberOfParticipants() + " / " + event.getMaxNumberOfParticipants() + " Participants";
        holder.eventParticipants.setText(participantsText);

        // Set up the entrants list using EntrantsListAdapter
        EntrantsListAdapter entrantsListAdapter = new EntrantsListAdapter(context, entrants);
        holder.entrantsList.setAdapter(entrantsListAdapter);
    }

    @Override
    public int getItemCount() {
        return 1; // Since this adapter is for a single event detail view
    }

    static class EventDetailViewHolder extends RecyclerView.ViewHolder {

        ImageButton backArrow;
        TextView eventTitle;
        ImageButton mapIcon;
        ImageButton shareIcon;
        TextView eventDescription;
        TextView eventDate;
        TextView eventLocation;
        TextView eventParticipants;
        Button viewWatchList;
        Button sendNotification;
        ListView entrantsList;
        ImageButton editGraph;

        public EventDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            backArrow = itemView.findViewById(R.id.back_arrow);
            eventTitle = itemView.findViewById(R.id.class_tile);
            mapIcon = itemView.findViewById(R.id.map_icon);
            shareIcon = itemView.findViewById(R.id.share_icon);
            eventDescription = itemView.findViewById(R.id.class_description);
            eventDate = itemView.findViewById(R.id.class_date);
            eventLocation = itemView.findViewById(R.id.class_location);
            eventParticipants = itemView.findViewById(R.id.text_view_num_participants); // assuming you add this TextView for participants
            viewWatchList = itemView.findViewById(R.id.view_watch_list);
            sendNotification = itemView.findViewById(R.id.send_notification);
            entrantsList = itemView.findViewById(R.id.entrants_list);
            editGraph = itemView.findViewById(R.id.edit_graph);
        }
    }
}

// Adapter for the Entrants List
class EntrantsListAdapter extends android.widget.BaseAdapter {
    private final Context context;
    private final List<User> entrants;

    public EntrantsListAdapter(Context context, List<User> entrants) {
        this.context = context;
        this.entrants = entrants;
    }

    @Override
    public int getCount() {
        return entrants.size();
    }

    @Override
    public Object getItem(int position) {
        return entrants.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.entrant_list_item, parent, false);
        }
        TextView entrantName = convertView.findViewById(R.id.entrant_name);
        User user = entrants.get(position);
        entrantName.setText(user.getName());
        return convertView;
    }
}
