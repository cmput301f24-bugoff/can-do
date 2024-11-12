package com.bugoff.can_do.notification;

import static android.content.ContentValues.TAG;

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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.Event;

import java.util.List;
import java.util.UUID;

public class SendNotificationFragment extends Fragment {

    private Spinner groupSpinner;
    private EditText messageEditText;
    private String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_notification, container, false);

        if (getArguments() != null) { eventId = getArguments().getString("eventId"); }
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
                // Clear message text
                messageEditText.setText("");

            }
            else {
                // Handle notification sending logic here
                Toast.makeText(getContext(), "Notification sent to " + selectedGroup, Toast.LENGTH_SHORT).show();
                // Clear message text
                messageEditText.setText("");
            }
        });

        return view;
    }

    
    private void sendtoWaitingList(String message, String eventId) {
        // Send notification to waiting list entrants
        // Get the event
        Log.d(TAG, "sendtoWaitingList: made it here");
        GlobalRepository.getEvent(eventId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Event event = task.getResult();
                if (event != null) {
                    // Handle the event
                    Log.d("Event", "Event retrieved: " + event.getName());
                    List<String> waitingListEntrants = event.getWaitingListEntrants();
                    Log.d(TAG, "sendtoWaitingList: " + waitingListEntrants.size() + " waiting list entrants");

                    // Send notification to each waiting list entrant
                    for (String entrant : waitingListEntrants) {
                        String uniqueId = UUID.randomUUID().toString();
                        Notification notification = new Notification(
                                uniqueId,
                                "Event Update",
                                message,
                                event.getFacility().getId(),
                                entrant,
                                eventId
                        );
                        GlobalRepository.addNotification(notification);
                    }

                    Toast.makeText(getContext(), "Notification sent to Waiting List Entrants", Toast.LENGTH_SHORT).show();


                } else {
                    Log.w("Event", "Event not found");
                    // Handle the event not found case
                }
            } else {
                Log.e("Event", "Error retrieving event", task.getException());
                // Handle the failure case
            }
        });

    }



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
