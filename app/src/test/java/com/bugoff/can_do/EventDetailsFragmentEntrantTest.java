package com.bugoff.can_do;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.test.core.app.ApplicationProvider;

import com.bugoff.can_do.database.MockGlobalRepository;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.event.EventViewModel;
import com.bugoff.can_do.event.EventViewModelFactory;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.EventDetailsFragmentEntrant;
import com.bugoff.can_do.user.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class EventDetailsFragmentEntrantTest {

    private MockGlobalRepository mockRepository;
    private EventDetailsFragmentEntrant fragment;

    private User testUser;
    private Facility testFacility;
    private Event testEvent;

    @Mock
    private Context mockContext;

    @Mock
    private Toast mockToast;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize MockGlobalRepository and set test data
        mockRepository = MockGlobalRepository.getInstance();

        // Create test user, facility, and event
        testUser = new User("test-user-id", "Test User", "testuser@example.com", "1234567890", false, null);
        MockGlobalRepository.addUser(testUser);

        testFacility = new Facility(testUser);
        testFacility.setName("Test Facility");
        MockGlobalRepository.addFacility(testFacility);

        testEvent = new Event(testFacility);
        testEvent.setName("Test Event");
        MockGlobalRepository.addEvent(testEvent);

        // Set up fragment with arguments
        fragment = EventDetailsFragmentEntrant.newInstance(testEvent.getId());
        fragment.setArguments(createBundleWithEventId(testEvent.getId()));
        fragment.onCreateView(LayoutInflater.from(ApplicationProvider.getApplicationContext()), null, null);

        // Mock Toast.makeText() behavior
        when(Toast.makeText(any(Context.class), anyString(), anyInt())).thenReturn(mockToast);
    }

    private Bundle createBundleWithEventId(String eventId) {
        Bundle bundle = new Bundle();
        bundle.putString("selected_event_id", eventId);
        return bundle;
    }

    @After
    public void tearDown() {
        MockGlobalRepository.resetInstance();
    }

    @Test
    public void testJoinWaitingList_Success() {
        EventViewModel viewModel = new ViewModelProvider(fragment, new EventViewModelFactory(testEvent.getId())).get(EventViewModel.class);

        // Call joinWaitingList and check if user is added to the waiting list
        fragment.joinWaitingList(viewModel);

        // Verify that the user's event list was updated
        assertTrue(testUser.getEventsJoined().contains(testEvent.getId()));

        // Verify Toast was shown
        verify(mockToast, times(1)).show();
    }

    @Test
    public void testLeaveWaitingList_Success() {
        EventViewModel viewModel = new ViewModelProvider(fragment, new EventViewModelFactory(testEvent.getId())).get(EventViewModel.class);

        // Add the user to the waiting list first
        testUser.addEventJoined(testEvent.getId());
        MockGlobalRepository.addUser(testUser);

        // Call leaveWaitingList and check if user is removed from the waiting list
        fragment.leaveWaitingList(viewModel);

        // Verify that the user's event list no longer includes the event
        assertFalse(testUser.getEventsJoined().contains(testEvent.getId()));

        // Verify Toast was shown
        verify(mockToast, times(1)).show();
    }
}
