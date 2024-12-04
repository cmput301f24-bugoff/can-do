package com.bugoff.can_do;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bugoff.can_do.testclasses.TestEvent;
import com.bugoff.can_do.testclasses.TestFacility;
import com.bugoff.can_do.testclasses.TestNoOpDatabaseBehavior;
import com.bugoff.can_do.testclasses.TestUser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NoOpDatabaseBehaviorTest {
    private TestNoOpDatabaseBehavior behavior;
    private TestUser testUser;
    private TestFacility testFacility;
    private TestEvent testEvent;

    @BeforeEach
    void setUp() {
        behavior = new TestNoOpDatabaseBehavior();

        // Create test data
        testUser = new TestUser("test-user-id");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testFacility = new TestFacility("test-facility-id", testUser);
        testFacility.setName("Test Facility");
        testFacility.setAddress("123 Test St");

        testEvent = new TestEvent("test-event-id", testFacility);
        testEvent.setName("Test Event");
        testEvent.setDescription("Test Description");
    }

    @Test
    void testMultipleUsersAndRetrieval() throws Exception {
        TestUser user1 = new TestUser("user-1");
        user1.setName("User One");
        TestUser user2 = new TestUser("user-2");
        user2.setName("User Two");

        behavior.addUser(user1);
        behavior.addUser(user2);

        // Test getting both users
        TestUser retrieved1 = behavior.getUser("user-1");
        TestUser retrieved2 = behavior.getUser("user-2");

        assertEquals("User One", retrieved1.getName());
        assertEquals("User Two", retrieved2.getName());
    }

    @Test
    void testOverwritingExistingData() throws Exception {
        // Add initial user
        behavior.addUser(testUser);

        // Create new user with same ID but different data
        TestUser updatedUser = new TestUser(testUser.getId());
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");

        // Overwrite the original user
        behavior.addUser(updatedUser);

        // Verify the data was updated
        TestUser retrieved = behavior.getUser(testUser.getId());
        assertEquals("Updated Name", retrieved.getName());
        assertEquals("updated@example.com", retrieved.getEmail());
    }

    @Test
    void testFacilityWithNullOwner() throws Exception {
        TestFacility nullOwnerFacility = new TestFacility("facility-id", null);
        nullOwnerFacility.setName("Test Facility");

        // Should be able to add and retrieve facility with null owner
        behavior.addFacility(nullOwnerFacility);
        TestFacility retrieved = behavior.getFacility("facility-id");

        Assertions.assertNotNull(retrieved);
        Assertions.assertNull(retrieved.getOwner());
    }

    @Test
    void testEventWithUpdatedFacility() throws Exception {
        // Add initial data
        behavior.addUser(testUser);
        behavior.addFacility(testFacility);
        behavior.addEvent(testEvent);

        // Create new facility for the same event
        TestFacility newFacility = new TestFacility("new-facility-id", testUser);
        newFacility.setName("New Facility");
        behavior.addFacility(newFacility);

        // Update event with new facility
        TestEvent updatedEvent = new TestEvent(testEvent.getId(), newFacility);
        updatedEvent.setName(testEvent.getName());
        updatedEvent.setDescription(testEvent.getDescription());
        behavior.saveEvent(updatedEvent);

        // Verify the event's facility was updated
        TestEvent retrieved = behavior.getEvent(testEvent.getId());
        assertEquals("new-facility-id", retrieved.getFacility().getId());
        assertEquals("New Facility", retrieved.getFacility().getName());
    }

    @Test
    void testPartialClearAndVerifyRemainingData() throws Exception {
        // Add all test data
        behavior.addUser(testUser);
        behavior.addFacility(testFacility);
        behavior.addEvent(testEvent);

        // Create and add additional test data
        TestUser additionalUser = new TestUser("additional-user");
        additionalUser.setName("Additional User");
        behavior.addUser(additionalUser);

        // Clear only events
        behavior.clearAll();
        behavior.addUser(testUser); // Re-add user
        behavior.addUser(additionalUser); // Re-add additional user

        // Verify users can still be retrieved
        Assertions.assertNotNull(behavior.getUser(testUser.getId()));
        Assertions.assertNotNull(behavior.getUser(additionalUser.getId()));

        // Verify other data is gone
        assertThrows(Exception.class, () -> behavior.getFacility(testFacility.getId()));
        assertThrows(Exception.class, () -> behavior.getEvent(testEvent.getId()));
    }

    @Test
    void testUserOperations() throws Exception {
        // Test adding user
        behavior.addUser(testUser);

        // Test getting user
        TestUser retrievedUser = behavior.getUser(testUser.getId());
        Assertions.assertNotNull(retrievedUser);
        assertEquals(testUser.getName(), retrievedUser.getName());
        assertEquals(testUser.getEmail(), retrievedUser.getEmail());

        // Test getting non-existent user
        assertThrows(Exception.class, () -> behavior.getUser("non-existent-id"));
    }

    @Test
    void testFacilityOperations() throws Exception {
        // Add prerequisite user
        behavior.addUser(testUser);

        // Test adding facility
        behavior.addFacility(testFacility);

        // Test getting facility
        TestFacility retrievedFacility = behavior.getFacility(testFacility.getId());
        Assertions.assertNotNull(retrievedFacility);
        assertEquals(testFacility.getName(), retrievedFacility.getName());
        assertEquals(testFacility.getAddress(), retrievedFacility.getAddress());
        assertEquals(testFacility.getOwner().getId(), retrievedFacility.getOwner().getId());

        // Test getting non-existent facility
        assertThrows(Exception.class, () -> behavior.getFacility("non-existent-id"));
    }

    @Test
    void testEventOperations() throws Exception {
        // Add prerequisites
        behavior.addUser(testUser);
        behavior.addFacility(testFacility);

        // Test adding event
        behavior.addEvent(testEvent);

        // Test getting event
        TestEvent retrievedEvent = behavior.getEvent(testEvent.getId());
        Assertions.assertNotNull(retrievedEvent);
        assertEquals(testEvent.getName(), retrievedEvent.getName());
        assertEquals(testEvent.getDescription(), retrievedEvent.getDescription());
        assertEquals(testEvent.getFacility().getId(), retrievedEvent.getFacility().getId());

        // Test saving event updates
        testEvent.setName("Updated Event Name");
        behavior.saveEvent(testEvent);
        retrievedEvent = behavior.getEvent(testEvent.getId());
        assertEquals("Updated Event Name", retrievedEvent.getName());

        // Test getting non-existent event
        assertThrows(Exception.class, () -> behavior.getEvent("non-existent-id"));
    }

    @Test
    void testClearAll() throws Exception {
        // Add all test data
        behavior.addUser(testUser);
        behavior.addFacility(testFacility);
        behavior.addEvent(testEvent);

        // Verify data was added
        Assertions.assertNotNull(behavior.getUser(testUser.getId()));
        Assertions.assertNotNull(behavior.getFacility(testFacility.getId()));
        Assertions.assertNotNull(behavior.getEvent(testEvent.getId()));

        // Clear all data
        behavior.clearAll();

        // Verify everything is cleared
        assertThrows(Exception.class, () -> behavior.getUser(testUser.getId()));
        assertThrows(Exception.class, () -> behavior.getFacility(testFacility.getId()));
        assertThrows(Exception.class, () -> behavior.getEvent(testEvent.getId()));
    }
}