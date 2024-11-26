package com.bugoff.can_do;

import android.app.Activity;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.organizer.OrganizerMain;
import com.bugoff.can_do.user.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.contrib.PickerActions.*;
import static androidx.test.espresso.intent.Intents.*;
import static androidx.test.espresso.matcher.RootMatchers.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class CreateEventFragmentTest {

    private ActivityScenario<OrganizerMain> scenario;

    @Before
    public void setUp() {
        Intents.init();

        // Simulate authentication by creating a test user
        String testUserId = "testUserId";
        User testUser = new User(testUserId);

        // Create a test facility and associate it with the test user
        Facility testFacility = new Facility(testUser);
        testUser.setFacility(testFacility);

        // Set the test user as the logged-in user in GlobalRepository
        GlobalRepository.setLoggedInUser(testUser);

        // Proceed to launch the activity
        scenario = ActivityScenario.launch(OrganizerMain.class);
    }


    @After
    public void tearDown() {
        Intents.release();
        if (scenario != null) {
            scenario.close();
        }
        // Reset GlobalRepository
        GlobalRepository.setLoggedInUser(null);
    }


    @Test
    public void testEventCreation() {
        // Step 1: Click on Profile button in the bottom navigation
        onView(withId(R.id.nav_profile_organizer)).perform(click());

        // Step 2: Wait for 3 seconds
        SystemClock.sleep(3000); // Note: Using Thread.sleep is generally discouraged in Espresso tests
        onView(withId(R.id.nav_events_organizer)).perform(click());
        // Step 3: Navigate to CreateEventFragment
        onView(withId(R.id.fab_add_event)).perform(click());

        // Fill in Event Name
        onView(withId(R.id.editTextEventName))
                .perform(typeText("Test Event"), closeSoftKeyboard());

        // Fill in Event Description
        onView(withId(R.id.editTextEventDescription))
                .perform(typeText("This is a test event."), closeSoftKeyboard());

        // Set Registration Start Date
        onView(withId(R.id.buttonRegStartDate)).perform(click());
        Calendar regStartDate = Calendar.getInstance();
        regStartDate.add(Calendar.DAY_OF_MONTH, 1); // Tomorrow
        onView(isAssignableFrom(DatePicker.class)).perform(setDate(
                regStartDate.get(Calendar.YEAR),
                regStartDate.get(Calendar.MONTH) + 1, // Months are 0-based in Calendar
                regStartDate.get(Calendar.DAY_OF_MONTH)
        ));
        onView(withText("OK")).perform(click());

        // Set Registration Start Time
        onView(withId(R.id.buttonRegStartTime)).perform(click());
        onView(isAssignableFrom(TimePicker.class)).perform(setTime(10, 0));
        onView(withText("OK")).perform(click());

        // Set Registration End Date
        onView(withId(R.id.buttonRegEndDate)).perform(click());
        Calendar regEndDate = Calendar.getInstance();
        regEndDate.add(Calendar.DAY_OF_MONTH, 2); // Day after tomorrow
        onView(isAssignableFrom(DatePicker.class)).perform(setDate(
                regEndDate.get(Calendar.YEAR),
                regEndDate.get(Calendar.MONTH) + 1,
                regEndDate.get(Calendar.DAY_OF_MONTH)
        ));
        onView(withText("OK")).perform(click());

        // Set Registration End Time
        onView(withId(R.id.buttonRegEndTime)).perform(click());
        onView(isAssignableFrom(TimePicker.class)).perform(setTime(17, 0));
        onView(withText("OK")).perform(click());

        // Set Event Start Date
        onView(withId(R.id.buttonEventStartDate)).perform(click());
        Calendar eventStartDate = Calendar.getInstance();
        eventStartDate.add(Calendar.DAY_OF_MONTH, 3);
        onView(isAssignableFrom(DatePicker.class)).perform(setDate(
                eventStartDate.get(Calendar.YEAR),
                eventStartDate.get(Calendar.MONTH) + 1,
                eventStartDate.get(Calendar.DAY_OF_MONTH)
        ));
        onView(withText("OK")).perform(click());

        // Set Event Start Time
        onView(withId(R.id.buttonEventStartTime)).perform(click());
        onView(isAssignableFrom(TimePicker.class)).perform(setTime(9, 0));
        onView(withText("OK")).perform(click());

        // Set Event End Date
        onView(withId(R.id.buttonEventEndDate)).perform(click());
        Calendar eventEndDate = Calendar.getInstance();
        eventEndDate.add(Calendar.DAY_OF_MONTH, 3);
        onView(isAssignableFrom(DatePicker.class)).perform(setDate(
                eventEndDate.get(Calendar.YEAR),
                eventEndDate.get(Calendar.MONTH) + 1,
                eventEndDate.get(Calendar.DAY_OF_MONTH)
        ));
        onView(withText("OK")).perform(click());

        // Set Event End Time
        onView(withId(R.id.buttonEventEndTime)).perform(click());
        onView(isAssignableFrom(TimePicker.class)).perform(setTime(12, 0));
        onView(withText("OK")).perform(click());

        // Set Max Number of Participants
        onView(withId(R.id.editTextMaxNumParticipants))
                .perform(typeText("10"), closeSoftKeyboard());

        // Check Geolocation Required
        onView(withId(R.id.checkBoxGeolocation)).perform(click());

        // Click Create Event Button
        onView(withId(R.id.buttonCreateEvent)).perform(click());

        // Wait for the Toast to appear and verify it
        onView(withText("Event created successfully!"))
                .inRoot(withDecorView(not(is(getCurrentActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

    }

    // Helper method to get the current activity
    private Activity getCurrentActivity() {
        final Activity[] currentActivity = new Activity[1];
        onView(isRoot()).check((view, noViewFoundException) -> {
            View rootView = view.getRootView();
            currentActivity[0] = (Activity) rootView.getContext();
        });
        return currentActivity[0];
    }
}
