package com.bugoff.can_do.notification;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bugoff.can_do.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications); // Ensure this is the correct layout file name

        // Find the back button and set its click listener to finish the activity
        ImageButton backButton = findViewById(R.id.notif_back_button);
        backButton.setOnClickListener(v -> finish()); // Closes the activity

        // Find the ListView in the layout
        ListView notifListView = findViewById(R.id.notif_list);

        // Create a list of sample notification data !!!


        // Need to change later
        List<String> notificationList = new ArrayList<>();
        notificationList.add("Notification 1");
        notificationList.add("Notification 2");
        notificationList.add("Notification 3");
        // Add more notifications as needed

        // Create and set the adapter
        NotificationAdapter adapter = new NotificationAdapter(this, notificationList);
        notifListView.setAdapter(adapter);
    }
}
