package com.bugoff.can_do.notification;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Fragment for sending notifications to specific groups of event participants.
 * Provides functionality to compose and send notifications to different recipient categories,
 * including waiting list entrants, selected entrants, and cancelled entrants.
 */
public class SendNotificationFragment extends Fragment {

    /**
     * Spinner for selecting the recipient group.
     */
    private Spinner groupSpinner;

    /**
     * EditText for entering the notification message.
     */
    private EditText messageEditText;

    /**
     * The ID of the event for which notifications are being sent.
     */
    private String eventId;

    /**
     * Called to create the view hierarchy associated with the fragment.
     *
     * @param inflater  The LayoutInflater object used to inflate views.
     * @param container The parent container in which the fragment's UI will be displayed.
     * @param savedInstanceState Saved state for restoring the fragment (if applicable).
     * @return The root view of the fragment's layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_notification, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        Log.d(TAG, "eventId: " + eventId);

        groupSpinner = view.findViewById(R.id.group_spinner);
        messageEditText = view.findViewById(R.id.message_edit_text);
        Button sendButton = view.findViewById(R.id.send_button);

        ArrayAdapter<CharSequence> adapter = getArrayAdapter();
        groupSpinner.setAdapter(adapter);

        sendButton.setOnClickListener(v -> {
            String selectedGroup = groupSpinner.getSelectedItem().toString();
            String message = messageEditText.getText().toString().trim();

            if ("Select Recipients".equals(selectedGroup)) {
                Toast.makeText(getContext(), "Please select a recipient group.", Toast.LENGTH_SHORT).show();
            } else if (message.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a message.", Toast.LENGTH_SHORT).show();
            } else if ("Waiting List Entrants".equals(selectedGroup)) {
                sendtoWaitingList(message, eventId);
                messageEditText.setText("");
            } else if ("Selected Entrants".equals(selectedGroup)) {
                sendtoSelectedEntrants(message, eventId);
                messageEditText.setText("");
            } else if ("Cancelled Entrants".equals(selectedGroup)) {
                sendtoCancelledEntrants(message, eventId);
                messageEditText.setText("");
            } else {
                Toast.makeText(getContext(), "Notification sent to " + selectedGroup, Toast.LENGTH_SHORT).show();
                messageEditText.setText("");
            }
        });

        return view;
    }

    /**
     * Sends a notification to waiting list entrants for the specified event.
     *
     * @param message The notification message.
     * @param eventId The ID of the event.
     */
    private void sendtoWaitingList(String message, String eventId) {
        GlobalRepository.getEvent(eventId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Event event = task.getResult();
                if (event != null) {
                    List<String> waitingListEntrants = event.getWaitingListEntrants();
                    if (!waitingListEntrants.isEmpty()) {
                        String uniqueId = UUID.randomUUID().toString();
                        Notification notification = new Notification(
                                uniqueId,
                                "Event Update",
                                message,
                                event.getFacility().getId(),
                                new ArrayList<>(waitingListEntrants),
                                eventId
                        );
                        GlobalRepository.addNotification(notification);
                        sendLocalNotification(getContext(), event.getName(), message);
                        Toast.makeText(getContext(),
                                "Notification sent to " + waitingListEntrants.size() + " waiting list entrants",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "No users in waiting list", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error retrieving event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends a notification to selected entrants for the specified event.
     *
     * @param message The notification message.
     * @param eventId The ID of the event.
     */
    private void sendtoSelectedEntrants(String message, String eventId) {
        GlobalRepository.getEvent(eventId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Event event = task.getResult();
                if (event != null) {
                    List<String> selectedEntrants = event.getSelectedEntrants();
                    if (!selectedEntrants.isEmpty()) {
                        String uniqueId = UUID.randomUUID().toString();
                        Notification notification = new Notification(
                                uniqueId,
                                "Event Update",
                                message,
                                event.getFacility().getId(),
                                new ArrayList<>(selectedEntrants),
                                eventId
                        );
                        GlobalRepository.addNotification(notification);
                        sendLocalNotification(getContext(), event.getName(), message);
                        Toast.makeText(getContext(),
                                "Notification sent to " + selectedEntrants.size() + " selected entrants",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "No selected entrants", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error retrieving event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends a notification to cancelled entrants for the specified event.
     *
     * @param message The notification message.
     * @param eventId The ID of the event.
     */
    private void sendtoCancelledEntrants(String message, String eventId) {
        GlobalRepository.getEvent(eventId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Event event = task.getResult();
                if (event != null) {
                    List<String> cancelledEntrants = event.getCancelledEntrants();
                    if (!cancelledEntrants.isEmpty()) {
                        String uniqueId = UUID.randomUUID().toString();
                        Notification notification = new Notification(
                                uniqueId,
                                "Event Update",
                                message,
                                event.getFacility().getId(),
                                new ArrayList<>(cancelledEntrants),
                                eventId
                        );
                        GlobalRepository.addNotification(notification);
                        sendLocalNotification(getContext(), event.getName(), message);
                        Toast.makeText(getContext(),
                                "Notification sent to " + cancelledEntrants.size() + " cancelled entrants",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "No cancelled entrants", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error retrieving event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends a local notification to the device.
     *
     * @param context The application context.
     * @param title   The title of the notification.
     * @param message The content of the notification.
     */
    private static void sendLocalNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "waiting_list_channel";
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Waiting List Notifications",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Notifications for waiting list updates");
        notificationManager.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.notifications_24px)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        int notificationId = new Random().nextInt();
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Creates an ArrayAdapter for the group selection spinner.
     *
     * @return An ArrayAdapter populated with recipient group options.
     */
    @NonNull
    private ArrayAdapter<CharSequence> getArrayAdapter() {
        return new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item, getResources().getTextArray(R.array.notification_groups)) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // Disable the first item ("Select Recipient")
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0) {
                    textView.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
                } else {
                    textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                }
                return view;
            }
        };
    }
}
