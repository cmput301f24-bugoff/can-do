package com.bugoff.can_do;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.bugoff.can_do.notification.Notification;
import com.bugoff.can_do.notification.NotificationAdapter;

import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapterTest {

    private Context context;
    private List<Notification> notifications;

    @Before
    public void setUp() {
        // Set up a context for the test
        context = ApplicationProvider.getApplicationContext();

        // Create sample notification data
        notifications = new ArrayList<>();
        notifications.add(new Notification("random-id1", "message", "this is a sample message", "from1" , "to1", "Test Notification 1"));
        notifications.add(new Notification("random-id2", "alert", "sample alert", "from1", "to2",   "Test Notification 2"));
    }


    @Test
    public void testNotificationAdapterBinding() {
        // Create the adapter with the sample data
        NotificationAdapter adapter = new NotificationAdapter(context, notifications);

        // Check the adapter's count matches the number of notifications
        assertEquals("Adapter should have the correct number of items", notifications.size(), adapter.getCount());

        // Check that the adapter correctly returns items
        for (int i = 0; i < adapter.getCount(); i++) {
            Notification notification = adapter.getItem(i);
            assertEquals("Notification content should match", notifications.get(i).getContent(), notification.getContent());
            assertEquals("Notification type should match", notifications.get(i).getType(), notification.getType());
            assertEquals("Notification ID should match", notifications.get(i).getId(), notification.getId());
        }
    }
}
