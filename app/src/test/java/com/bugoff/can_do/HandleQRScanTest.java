package com.bugoff.can_do;

import static com.google.common.base.CharMatcher.any;
import static com.google.common.base.Verify.verify;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.content.Context;
import android.widget.Toast;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.user.EventDetailsFragmentEntrant;
import com.bugoff.can_do.user.HandleQRScan;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HandleQRScanTest {

    @Mock
    private Context mockContext;

    @Mock
    private GlobalRepository mockRepository;

    @Mock
    private AppCompatActivity mockActivity;

    @Mock
    private FragmentTransaction mockTransaction;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessQRCode_validQRCode() {
        String validQRCode = "cando-123456";
        String expectedEventId = "123456";

        // Mock the GlobalRepository.getEvent method to simulate fetching an event
        Task<Event> mockSuccessTask = Tasks.forResult(new Event("Sample Event", "2023-11-11"));
        when(mockRepository.getEvent(expectedEventId)).thenReturn(mockSuccessTask);

        // Call the method
        HandleQRScan.processQRCode(validQRCode, mockContext);

        // Verify that fetchEvent was called with the correct event ID
        verify(mockRepository).getEvent(expectedEventId);
    }

    @Test
    public void testProcessQRCode_invalidQRCode() {
        String invalidQRCode = "invalid-123456";

        HandleQRScan.processQRCode(invalidQRCode, mockContext);

        // Capture and assert that the log was printed for an invalid QR code
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        verify(Log).e(eq("HandleQRScan"), logCaptor.capture());
        assertTrue(logCaptor.getValue().contains("Invalid QR Code format"));
    }

    @Test
    public void testFetchEvent_nullOrEmptyEventId() {
        String nullEventId = null;
        String emptyEventId = "";

        // Simulate calling fetchEvent with a null eventId
        HandleQRScan.fetchEvent(nullEventId, mockContext);

        // Verify that a Toast is shown for null eventId
        verify(mockContext).getString(eq("Invalid Event ID"));

        // Simulate calling fetchEvent with an empty eventId
        HandleQRScan.fetchEvent(emptyEventId, mockContext);

        // Verify that a Toast is shown for empty eventId
        verify(mockContext).getString(eq("Invalid Event ID"));
    }

    @Test
    public void testFetchEvent_validEvent() {
        String validEventId = "123456";

        // Mock a successful Task
        Task<Event> successTask = Tasks.forResult(new Event("Sample Event", "2023-11-11"));
        when(GlobalRepository.getEvent(validEventId)).thenReturn(successTask);

        // Set up FragmentTransaction mock
        when(mockActivity.getSupportFragmentManager().beginTransaction()).thenReturn(mockTransaction);

        // Call fetchEvent with valid eventId
        HandleQRScan.fetchEvent(validEventId, mockActivity);

        // Verify that fragment transaction replace and commit were called
        verify(mockTransaction).replace(eq(R.id.fragment_container), any(EventDetailsFragmentEntrant.class));
        verify(mockTransaction).addToBackStack(null);
        verify(mockTransaction).commit();
    }

    @Test
    public void testFetchEvent_incompleteEvent() {
        String validEventId = "123456";

        // Mock Task with incomplete Event data
        Task<Event> incompleteEventTask = Tasks.forResult(new Event(null, null));
        when(GlobalRepository.getEvent(validEventId)).thenReturn(incompleteEventTask);

        // Call fetchEvent
        HandleQRScan.fetchEvent(validEventId, mockContext);

        // Verify that Toast was shown for incomplete event data
        verify(mockContext).getString(eq("Event details are incomplete"));
    }

    @Test
    public void testFetchEvent_eventNotFound() {
        String validEventId = "123456";

        // Mock a failed Task
        Task<Event> failureTask = Tasks.forException(new Exception("Event not found"));
        when(GlobalRepository.getEvent(validEventId)).thenReturn(failureTask);

        // Call fetchEvent
        HandleQRScan.fetchEvent(validEventId, mockContext);

        // Verify that Toast was shown for event not found
        verify(mockContext).getString(eq("Event not found"));
    }
}
