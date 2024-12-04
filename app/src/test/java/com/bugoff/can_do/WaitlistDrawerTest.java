package com.bugoff.can_do;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bugoff.can_do.testclasses.TestUser;
import com.bugoff.can_do.testclasses.WaitlistDrawer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class WaitlistDrawerTest {
    private WaitlistDrawer drawer;
    private TestUser user1;
    private TestUser user2;
    private TestUser user3;

    @BeforeEach
    void setUp() {
        drawer = new WaitlistDrawer();

        // Create test users
        user1 = new TestUser("1");
        user1.setName("User 1");
        user1.setEmail("user1@test.com");

        user2 = new TestUser("2");
        user2.setName("User 2");
        user2.setEmail("user2@test.com");

        user3 = new TestUser("3");
        user3.setName("User 3");
        user3.setEmail("user3@test.com");

        // Add to waitlist
        drawer.addToWaitlist(user1);
        drawer.addToWaitlist(user2);
        drawer.addToWaitlist(user3);
    }

    @Test
    void testDrawSingleUser() {
        List<TestUser> selected = drawer.performDrawing(1);

        assertEquals(1, selected.size(), "Should select exactly one user");
        assertEquals(2, drawer.getWaitlist().size(), "Should have two users remaining in waitlist");
        assertEquals(1, drawer.getSelectedUsers().size(), "Should have one selected user");

        // Verify selected user was removed from waitlist
        assertFalse(drawer.getWaitlist().contains(selected.get(0)),
                "Selected user should not be in waitlist anymore");
    }

    @Test
    void testDrawAllUsers() {
        List<TestUser> selected = drawer.performDrawing(3);

        assertEquals(3, selected.size(), "Should select all three users");
        assertTrue(drawer.getWaitlist().isEmpty(), "Waitlist should be empty");
        assertEquals(3, drawer.getSelectedUsers().size(), "Should have all users selected");
    }

    @Test
    void testInvalidDrawNumber() {
        // Test drawing zero users
        assertThrows(IllegalArgumentException.class, () -> drawer.performDrawing(0),
                "Should throw exception when drawing zero users");

        // Test drawing more users than available
        assertThrows(IllegalArgumentException.class, () -> drawer.performDrawing(4),
                "Should throw exception when drawing more users than available");

        // Test drawing negative number
        assertThrows(IllegalArgumentException.class, () -> drawer.performDrawing(-1),
                "Should throw exception when drawing negative number of users");
    }

    @Test
    void testDrawMultipleTimes() {
        // First draw
        List<TestUser> firstDraw = drawer.performDrawing(1);
        assertEquals(1, firstDraw.size(), "First draw should select one user");
        assertEquals(2, drawer.getWaitlist().size(), "Should have two users remaining");

        // Second draw
        List<TestUser> secondDraw = drawer.performDrawing(1);
        assertEquals(1, secondDraw.size(), "Second draw should select one user");
        assertEquals(1, drawer.getWaitlist().size(), "Should have one user remaining");

        // Verify different users were selected
        assertNotEquals(firstDraw.getFirst().getId(), secondDraw.getFirst().getId(),
                "Should select different users in each draw");
    }

    @Test
    void testSelectedUsersAccumulate() {
        drawer.performDrawing(1);
        assertEquals(1, drawer.getSelectedUsers().size(), "Should have one selected user");

        drawer.performDrawing(1);
        assertEquals(2, drawer.getSelectedUsers().size(), "Should have two selected users");

        drawer.performDrawing(1);
        assertEquals(3, drawer.getSelectedUsers().size(), "Should have all three selected users");
    }
}
