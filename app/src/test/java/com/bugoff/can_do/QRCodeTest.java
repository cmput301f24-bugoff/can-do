package com.bugoff.can_do;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;

import com.bugoff.can_do.database.MockGlobalRepository;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.HandleQRScan;
import com.bugoff.can_do.user.User;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

public class QRCodeTest {

    private MockGlobalRepository mockRepository;

    @Mock
    private Context mockContext;

    private Event testEvent;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize MockGlobalRepository and create test data
        mockRepository = MockGlobalRepository.getInstance();

        // Create a User, Facility, and Event for testing
        User testUser = new User("test-user-id", "Test User", "testuser@example.com", "1234567890", false, null);
        MockGlobalRepository.addUser(testUser);

        Facility testFacility = new Facility(testUser);
        testFacility.setName("Test Facility");
        MockGlobalRepository.addFacility(testFacility);

        testEvent = new Event(testFacility);
        testEvent.setName("Test Event");
        testEvent.setEventStartDate(new Date());
        MockGlobalRepository.addEvent(testEvent);
    }

    @After
    public void tearDown() {
        MockGlobalRepository.resetInstance();
    }

    @Test
    public void testProcessQRCode_ValidQRCode_Success() {
        // Set up valid QR code string
        String validQRCode = "cando-" + testEvent.getId();

        // Mock Toast for testing
        doNothing().when(mockContext).startActivity(any());
        doNothing().when(mockContext).getString(anyInt());

        // Invoke the processQRCode method with a valid QR code
        HandleQRScan.processQRCode(validQRCode, mockContext);

        // Verify that an action is triggered in the context (or mock fragment transaction)
        verify(mockContext, times(1)).startActivity(any());
    }

    @Test
    public void testProcessQRCode_InvalidQRCode_Failure() {
        // Set up invalid QR code string
        String invalidQRCode = "invalid-qr";

        // Invoke the processQRCode method with invalid QR code
        HandleQRScan.processQRCode(invalidQRCode, mockContext);

        // Verify that a Toast is shown for the invalid QR code format
//        verify(mockContext, times(1)).showToast(anyString(), Toast.LENGTH_SHORT);
    }
}
