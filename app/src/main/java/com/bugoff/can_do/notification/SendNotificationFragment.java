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
 * Fragment for sending notifications to event entrants.
 */
public class SendNotificationFragment extends Fragment {

    private Spinner groupSpinner;
    private EditText messageEditText;
    private String eventId;

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


        // Set click listener for send button
        sendButton.setOnClickListener(v -> {
            String selectedGroup = groupSpinner.getSelectedItem().toString();
            String message = messageEditText.getText().toString().trim();

            if ("Select Recipients".equals(selectedGroup)) {
                Toast.makeText(getContext(), "Please select a recipient group.", Toast.LENGTH_SHORT).show();
            } else if (message.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a message.", Toast.LENGTH_SHORT).show();
            } else if ("Waiting List Entrants".equals(selectedGroup)) {
                Log.d(TAG, "Trying to send notification to waiting list entrants");
                sendtoWaitingList(message, eventId);
                messageEditText.setText("");
            } else if ("Selected Entrants".equals(selectedGroup)) {
                Toast.makeText(getContext(), "Notification sent to Selected Entrants", Toast.LENGTH_SHORT).show();
                sendtoSelectedEntrants(message, eventId);
                messageEditText.setText("");
            } else if ("Cancelled Entrants".equals(selectedGroup)) {
                Toast.makeText(getContext(), "Notification sent to Cancelled Entrants", Toast.LENGTH_SHORT).show();
                sendtoCancelledEntrants(message, eventId);
                messageEditText.setText("");
            } else {
                // Handle notification sending logic here
                Toast.makeText(getContext(), "Notification sent to " + selectedGroup, Toast.LENGTH_SHORT).show();
                // Clear message text
                messageEditText.setText("");
            }
        });

        return view;
    }
    /**
     * Sends a notification to all users on the waiting list for the specified event.
     *
     * @param message The message to send in the notification.
     * @param eventId The ID of the event to send the notification for.
     */
    private void sendtoWaitingList(String message, String eventId) {
        Log.d(TAG, "sendtoWaitingList: starting");
        GlobalRepository.getEvent(eventId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Event event = task.getResult();
                if (event != null) {
                    List<String> waitingListEntrants = event.getWaitingListEntrants();
                    if (!waitingListEntrants.isEmpty()) {
                        // Create single notification for all recipients
                        String uniqueId = UUID.randomUUID().toString();
                        Notification notification = new Notification(
                                uniqueId,
                                "Event Update",
                                message,
                                event.getFacility().getId(),
                                new ArrayList<>(waitingListEntrants), // Create new list to avoid modifications
                                eventId
                        );
                        GlobalRepository.addNotification(notification);

                        // Send local notification
                        String eventTitle = event.getName();
                        sendLocalNotification(getContext(), eventTitle, message);

                        Toast.makeText(getContext(),
                                "Notification sent to " + waitingListEntrants.size() + " waiting list entrants",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "No users in waiting list", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "Event not found");
                    Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Error retrieving event", task.getException());
                Toast.makeText(getContext(), "Error retrieving event", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * Sends a notification to all selected entrants for the specified event.
     *
     * @param message The message to send in the notification.
     * @param eventId The ID of the event to send the notification for.
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

                        // Send local notification
                        String eventTitle = event.getName();
                        sendLocalNotification(getContext(), eventTitle, message);

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
     * Sends a notification to all cancelled entrants for the specified event.
     *
     * @param message The message to send in the notification.
     * @param eventId The ID of the event to send the notification for.
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

                        // Send local notification
                        String eventTitle = event.getName();
                        sendLocalNotification(getContext(), eventTitle, message);

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
     * Sends a local notification to the user's device.
     *
     * @param context The application context.
     * @param title   The title of the notification.
     * @param message The message to display in the notification.
     */
    private static void sendLocalNotification(Context context, String title, String message) {
        // Create NotificationManager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification Channel setup (required for Android O and above)
        String channelId = "waiting_list_channel";
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Waiting List Notifications",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Notifications for waiting list updates");
        notificationManager.createNotificationChannel(channel);

        // Build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.notifications_24px)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Show Notification
        int notificationId = new Random().nextInt(); // Unique ID for each notification
        notificationManager.notify(notificationId, builder.build());
    }


    /**
     * Returns an ArrayAdapter for the recipient group spinner.
     *
     * @return The ArrayAdapter for the recipient group spinner.
     */
    @NonNull
    private ArrayAdapter<CharSequence> getArrayAdapter() {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item, getResources().getTextArray(R.array.notification_groups)) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item ("Select Recipient")
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Set the first item in grey color
                if (position == 0) {
                    textView.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
                } else {
                    textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                }
                

                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }
}
