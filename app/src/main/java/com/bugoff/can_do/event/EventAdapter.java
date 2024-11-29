package com.bugoff.can_do.event;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying a list of events.
 * Manages the display of each event item in the RecyclerView and
 * binds event data to the respective views.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private static List<Event> eventList;
    private boolean isAdmin;
    private final boolean isFromAdmin;
    private final boolean isFromEntrant;
    private OnDeleteClickListener deleteClickListener;
    private OnItemClickListener itemClickListener;

    // Interface for handling delete button clicks
    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    /**
     * Interface for handling click events on individual event items.
     */
    public interface OnItemClickListener {
        /**
         * Called when an event item is clicked.
         *
         * @param event The event that was clicked.
         */
        void onItemClick(Event event);
    }

    /**
     * Constructs a new EventAdapter with a list of events and a click listener.
     *
     * @param eventList The list of events to display.
     * @param isAdmin Whether the current user is an admin
     * @param isFromAdmin Whether we're viewing from the admin interface
     * @param deleteClickListener Listener for delete button clicks
     * @param itemClickListener Listener for item clicks
     */
    public EventAdapter(List<Event> eventList, boolean isAdmin, boolean isFromAdmin,
                        OnDeleteClickListener deleteClickListener, OnItemClickListener itemClickListener, boolean isFromEntrant) {
        this.eventList = eventList;
        this.isAdmin = isAdmin;
        this.isFromAdmin = isFromAdmin;
        this.isFromEntrant = isFromEntrant;
        this.deleteClickListener = deleteClickListener;
        this.itemClickListener = itemClickListener;
    }


    /**
     * Updates the event list and refreshes the RecyclerView.
     *
     * @param eventList The updated list of events.
     */
    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder for an event item view.
     *
     * @param parent   The parent view group.
     * @param viewType The view type.
     * @return A new instance of EventViewHolder.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(itemView);
    }

    /**
     * Binds the event data to the ViewHolder at the specified position.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the dataset.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        // Pass both isAdmin and isFromAdmin to determine delete button visibility
        holder.bind(event, isAdmin, isFromAdmin, deleteClickListener, itemClickListener, isFromEntrant);
    }

    /**
     * Returns the total number of items in the dataset.
     *
     * @return The size of the event list.
     */
    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    /**
     * ViewHolder class for holding and binding data to each event item view in the RecyclerView.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewDescription;
        TextView textViewDates;
        TextView textViewParticipants;
        ImageButton buttonDelete;
        TextView textViewWaitingList;
        TextView textViewStatus;

        private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());

        /**
         * Initializes the ViewHolder and finds the views within the event item layout.
         *
         * @param itemView The item view associated with the ViewHolder.
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_event_name);
            textViewDescription = itemView.findViewById(R.id.text_view_event_description);
            textViewDates = itemView.findViewById(R.id.text_view_event_dates);
            textViewParticipants = itemView.findViewById(R.id.text_view_num_participants);
            buttonDelete = itemView.findViewById(R.id.button_delete_event);
            textViewWaitingList = itemView.findViewById(R.id.text_view_waitlist);
            textViewStatus = itemView.findViewById(R.id.text_view_status);
        }

        /**
         * Binds event data to the views, including formatting dates and displaying participant counts.
         *
         * @param event The event data to display.
         * @param isAdmin Whether the current user is an admin
         * @param isFromAdmin Whether we're viewing from the admin interface
         * @param deleteListener The listener for handling delete button clicks
         * @param itemClickListener The listener for handling item clicks
         */
        public void bind(final Event event, boolean isAdmin, boolean isFromAdmin,
                         final OnDeleteClickListener deleteListener, final OnItemClickListener itemClickListener, boolean isFromEntrant) {
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

            // Display the current number of participants out of the max allowed
            String participants = "Registered Participants: " + event.getEnrolledEntrants().size() + " / " + event.getMaxNumberOfParticipants();
            textViewParticipants.setText(participants);

            // Display the number of entrants on the waiting list
            String waitingList = "Waiting List: " + event.getWaitingListEntrants().size();
            textViewWaitingList.setText(waitingList);

            // Set delete button visibility - only show if both isAdmin and isFromAdmin are true
            buttonDelete.setVisibility(isAdmin && isFromAdmin ? View.VISIBLE : View.GONE);

            // Display the event status
            if (isFromEntrant) {
                List<String> waitlist_entrants = event.getWaitingListEntrants();
                if (waitlist_entrants.contains(GlobalRepository.getLoggedInUser().getId())) {
                    textViewStatus.setText("In Waitlist");
                    textViewStatus.setTextColor(Color.parseColor("#FF964F"));
                    textViewStatus.setVisibility(View.VISIBLE);
                } else if (event.getSelectedEntrants().contains(GlobalRepository.getLoggedInUser().getId())) {
                    textViewStatus.setText("You have been selected, please accept or decline");
                    textViewStatus.setTextColor(Color.parseColor("#FF0000"));
                    textViewStatus.setVisibility(View.VISIBLE);
                } else if (event.getEnrolledEntrants().contains(GlobalRepository.getLoggedInUser().getId())) {
                    textViewStatus.setText("Enrolled");
                    textViewStatus.setTextColor(Color.parseColor("#008000"));
                    textViewStatus.setVisibility(View.VISIBLE);
                } else {
                    textViewStatus.setVisibility(View.GONE);
                }
            } else {
                // Hide the status TextView when not in entrant section
                textViewStatus.setVisibility(View.GONE);
            }


            // Set click listener for delete button
            buttonDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(event);
                }
            });

            // Update click listener for the entire item
            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(event);
                }
            });
        }
    }
}
