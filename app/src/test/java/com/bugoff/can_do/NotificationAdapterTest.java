package com.bugoff.can_do;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.bugoff.can_do.notification.Notification;
import com.bugoff.can_do.notification.NotificationAdapter;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NotificationAdapterTest {

    private Context context;
    private List<Notification> notifications;

    @Before
    public void setUp() {
        // Set up a context for the test
        context = ApplicationProvider.getApplicationContext();

        // Create sample notification data
        notifications = new ArrayList<>();
        notifications.add(new Notification("1", "info", "Test Notification 1"));
        notifications.add(new Notification("2", "warning", "Test Notification 2"));
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
            assertEquals("Notification I should match", notifications.get(i).getId(), notification.getId());
        }
    }
}
