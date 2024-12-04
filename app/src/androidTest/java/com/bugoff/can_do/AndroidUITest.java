package com.bugoff.can_do;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.database.NoOpDatabaseBehavior;
import com.bugoff.can_do.database.UserAuthenticator;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.organizer.OrganizerMain;
import com.bugoff.can_do.organizer.OrganizerTransition;
import com.bugoff.can_do.user.HandleQRScan;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.TaskCompletionSource;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class AndroidUITest {
    private static final String TEST_NAME = "Test User";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FACILITY_NAME = "Test Facility";
    private static final String TEST_FACILITY_ADDRESS = "123 Test St";
    private String androidId;
    private NoOpDatabaseBehavior testBehavior;

    @Before
    public void setup() {
        // Grant permissions automatically for tests
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName() +
                            " android.permission.ACCESS_FINE_LOCATION");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                        "pm grant " + InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName() +
                                " android.permission.POST_NOTIFICATIONS");
            }
        }

        // Get Android ID
        androidId = Settings.Secure.getString(
                InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Set up test behavior
        testBehavior = new NoOpDatabaseBehavior();
        GlobalRepository.setTestMode(true);
        GlobalRepository.setBehavior(testBehavior);
        Event.setDatabaseBehavior(testBehavior);

        // Create initial test user
        User mockUser = new User(androidId);
        mockUser.setName("");
        mockUser.setEmail("");
        testBehavior.addUser(mockUser);
        GlobalRepository.setLoggedInUser(mockUser);
    }

    @After
    public void tearDown() {
        testBehavior.clearAll();
        GlobalRepository.setLoggedInUser(null);
    }

    @AfterClass
    public static void cleanUp() {
        GlobalRepository.setTestMode(false);
        UserAuthenticator.reset();
    }

    @Test
    public void testUserProfileSetup() {
        // Launch the main activity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // Verify we're on the sign in screen
        onView(withId(R.id.nameEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.emailEditText)).check(matches(isDisplayed()));

        // Enter user information
        onView(withId(R.id.nameEditText))
                .perform(typeText(TEST_NAME), closeSoftKeyboard());
        onView(withId(R.id.emailEditText))
                .perform(typeText(TEST_EMAIL), closeSoftKeyboard());

        // Click submit
        onView(withId(R.id.submitButton)).perform(click());

        // Verify we moved to the home screen
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testOrganizerTransition() {
        // Launch main activity and complete user setup
        ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class);

        mainScenario.onActivity(activity -> {
            GlobalRepository.setBehavior(testBehavior);
        });

        // Complete user profile setup
        onView(withId(R.id.nameEditText))
                .perform(typeText(TEST_NAME), closeSoftKeyboard());
        onView(withId(R.id.emailEditText))
                .perform(typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.submitButton)).perform(click());

        // Navigate to profile and verify
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.organizer_button)).check(matches(isDisplayed()));

        // Click organizer button and verify transition
        onView(withId(R.id.organizer_button)).perform(click());

        // Inject mock repository into OrganizerTransition
        ActivityScenario.launch(OrganizerTransition.class)
                .onActivity(activity -> {
                    GlobalRepository.setBehavior(testBehavior);
                });

        // Verify facility creation screen
        onView(withId(R.id.facilityNameInput)).check(matches(isDisplayed()));
        onView(withId(R.id.facilityAddressInput)).check(matches(isDisplayed()));

        // Enter facility information
        onView(withId(R.id.facilityNameInput))
                .perform(typeText(TEST_FACILITY_NAME), closeSoftKeyboard());
        onView(withId(R.id.facilityAddressInput))
                .perform(typeText(TEST_FACILITY_ADDRESS), closeSoftKeyboard());

        // Save the facility
        onView(withId(R.id.saveFacilityButton)).perform(click());

        ActivityScenario.launch(OrganizerMain.class)
                .onActivity(activity -> {
                    GlobalRepository.setBehavior(testBehavior);
                });

        // Verify we moved to the organizer main screen
        onView(withId(R.id.bottom_navigation_organizer)).check(matches(isDisplayed()));

        // Verify facility was created in mock repository
        User user = testBehavior.getUser(androidId).getResult();
        assertNotNull("User should exist", user);

        Facility facility = user.getFacility();
        assertNotNull("Facility should be created", facility);
        assertEquals("Facility name should match", TEST_FACILITY_NAME, facility.getName());
        assertEquals("Facility address should match", TEST_FACILITY_ADDRESS, facility.getAddress());
        assertEquals("Facility owner should be set", androidId, facility.getOwner().getId());

        // Verify we moved to the organizer main screen
        onView(withId(R.id.bottom_navigation_organizer)).check(matches(isDisplayed()));

        mainScenario.close();
    }

    @Test
    public void testEventCreation() {
        // First complete the necessary setup steps from previous tests
        ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class);
        mainScenario.onActivity(activity -> {
            GlobalRepository.setBehavior(testBehavior);
        });

        // Complete user profile setup
        onView(withId(R.id.nameEditText)).perform(typeText(TEST_NAME), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.submitButton)).perform(click());

        // Navigate to organizer section and set up facility
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.organizer_button)).perform(click());

        ActivityScenario.launch(OrganizerTransition.class)
                .onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        onView(withId(R.id.facilityNameInput)).perform(typeText(TEST_FACILITY_NAME), closeSoftKeyboard());
        onView(withId(R.id.facilityAddressInput)).perform(typeText(TEST_FACILITY_ADDRESS), closeSoftKeyboard());
        onView(withId(R.id.saveFacilityButton)).perform(click());

        // Launch OrganizerMain and set repository
        ActivityScenario<OrganizerMain> organizerScenario = ActivityScenario.launch(OrganizerMain.class);
        organizerScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        // Click FAB to create new event
        onView(withId(R.id.fab_add_event)).perform(click());

        // Fill in event details
        String testEventName = "Test Event";
        String testEventDescription = "This is a test event description";
        int testMaxParticipants = 50;

        onView(withId(R.id.editTextEventName))
                .perform(typeText(testEventName), closeSoftKeyboard());
        onView(withId(R.id.editTextEventDescription))
                .perform(typeText(testEventDescription), closeSoftKeyboard());
        onView(withId(R.id.editTextMaxNumParticipants))
                .perform(typeText(String.valueOf(testMaxParticipants)), closeSoftKeyboard());

        // Set all dates and times using the buttons - accepting defaults
        onView(withId(R.id.buttonRegStartDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonRegStartTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonRegEndDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonRegEndTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonEventStartDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonEventStartTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonEventEndDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonEventEndTime)).perform(click());
        onView(withText("OK")).perform(click());

        // Set geolocation requirement
        onView(withId(R.id.checkBoxGeolocation)).perform(click());

        // Create the event
        onView(withId(R.id.buttonCreateEvent)).perform(click());

        // Verify the event was created in mock repository
        User user = testBehavior.getUser(androidId).getResult();
        Facility facility = user.getFacility();
        assertNotNull("Facility should exist", facility);

        // Wait for the event to be created and added to the facility
        SystemClock.sleep(1000);

        // Verify event details
        assertEquals("Facility should have 1 event", 1, facility.getEvents().size());
        Event createdEvent = facility.getEvents().stream()
                .filter(e -> e.getName().equals(testEventName))
                .findFirst()
                .orElse(null);
        assertNotNull("Should find the created event", createdEvent);
        assertEquals("Event name should match", testEventName, createdEvent.getName());
        assertEquals("Event description should match", testEventDescription, createdEvent.getDescription());
        assertEquals("Max participants should match", testMaxParticipants, createdEvent.getMaxNumberOfParticipants().intValue());
        assertTrue("Geolocation should be required", createdEvent.getGeolocationRequired());

        // Verify we're back at the events list
        onView(withId(R.id.recycler_view_events))
                .check(matches(isDisplayed()));

        // Clean up
        mainScenario.close();
        organizerScenario.close();
        UserAuthenticator.reset();
    }

    @Test
    public void testQRCodeScanAndEventView() throws ExecutionException, InterruptedException {
        // First create an event that we can scan
        ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class);
        mainScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        // Complete user profile setup
        onView(withId(R.id.nameEditText)).perform(typeText(TEST_NAME), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.submitButton)).perform(click());

        // Navigate to organizer section and set up facility
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.organizer_button)).perform(click());

        ActivityScenario.launch(OrganizerTransition.class)
                .onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        onView(withId(R.id.facilityNameInput)).perform(typeText(TEST_FACILITY_NAME), closeSoftKeyboard());
        onView(withId(R.id.facilityAddressInput)).perform(typeText(TEST_FACILITY_ADDRESS), closeSoftKeyboard());
        onView(withId(R.id.saveFacilityButton)).perform(click());

        // Launch OrganizerMain and set repository
        ActivityScenario<OrganizerMain> organizerScenario = ActivityScenario.launch(OrganizerMain.class);
        organizerScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        // Create a test event
        String testEventName = "QR Test Event";
        String testEventDescription = "This is a test event for QR scanning";
        int testMaxParticipants = 50;

        // Click FAB to create new event
        onView(withId(R.id.fab_add_event)).perform(click());

        // Fill in event details
        onView(withId(R.id.editTextEventName))
                .perform(typeText(testEventName), closeSoftKeyboard());
        onView(withId(R.id.editTextEventDescription))
                .perform(typeText(testEventDescription), closeSoftKeyboard());
        onView(withId(R.id.editTextMaxNumParticipants))
                .perform(typeText(String.valueOf(testMaxParticipants)), closeSoftKeyboard());

        // Set all dates using the buttons - accepting defaults
        onView(withId(R.id.buttonRegStartDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonRegStartTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonRegEndDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonRegEndTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonEventStartDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonEventStartTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonEventEndDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonEventEndTime)).perform(click());
        onView(withText("OK")).perform(click());

        // Create the event
        onView(withId(R.id.buttonCreateEvent)).perform(click());

        // Wait for the event to be created
        SystemClock.sleep(1000);

        // Get the created event from the repository
        User user = testBehavior.getUser(androidId).getResult();
        Facility facility = user.getFacility();
        Event createdEvent = facility.getEvents().stream()
                .filter(e -> e.getName().equals(testEventName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Event not found"));

        // Add event to mock repository
        testBehavior.addEvent(createdEvent);

        // Set up mock document behavior for Facility
        testBehavior.addFacility(facility);

        // Close the existing scenarios before starting new one
        mainScenario.close();
        organizerScenario.close();

        // Launch a fresh MainActivity for QR scanning
        ActivityScenario<MainActivity> scanScenario = ActivityScenario.launch(MainActivity.class);
        scanScenario.onActivity(activity -> {
            GlobalRepository.setBehavior(testBehavior);
            HandleQRScan.processQRCode("cando-" + createdEvent.getId(), activity);
        });

        // Wait for navigation
        SystemClock.sleep(1000);

        // Verify event details are displayed correctly
        onView(withId(R.id.class_tile)).check(matches(withText(testEventName)));
        onView(withId(R.id.class_description)).check(matches(withText(testEventDescription)));
        onView(withId(R.id.class_location)).check(matches(withText("Address: " + TEST_FACILITY_ADDRESS)));

        // Clean up
        scanScenario.close();
    }

    @Test
    public void testWaitlistSelectionFlow() throws ExecutionException, InterruptedException {
        // First complete the necessary setup steps for organizer
        ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class);
        mainScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        // Complete organizer profile setup
        onView(withId(R.id.nameEditText)).perform(typeText(TEST_NAME), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.submitButton)).perform(click());

        // Navigate to organizer section and set up facility
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.organizer_button)).perform(click());

        ActivityScenario.launch(OrganizerTransition.class)
                .onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        onView(withId(R.id.facilityNameInput)).perform(typeText(TEST_FACILITY_NAME), closeSoftKeyboard());
        onView(withId(R.id.facilityAddressInput)).perform(typeText(TEST_FACILITY_ADDRESS), closeSoftKeyboard());
        onView(withId(R.id.saveFacilityButton)).perform(click());

        // Launch OrganizerMain and create event
        ActivityScenario<OrganizerMain> organizerScenario = ActivityScenario.launch(OrganizerMain.class);
        organizerScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        // Click FAB to create new event
        onView(withId(R.id.fab_add_event)).perform(click());

        // Fill in event details
        String testEventName = "Waitlist Test Event";
        String testEventDescription = "Test event for waitlist flow";
        int testMaxParticipants = 1; // Set to 1 to test selection process

        onView(withId(R.id.editTextEventName))
                .perform(typeText(testEventName), closeSoftKeyboard());
        onView(withId(R.id.editTextEventDescription))
                .perform(typeText(testEventDescription), closeSoftKeyboard());
        onView(withId(R.id.editTextMaxNumParticipants))
                .perform(typeText(String.valueOf(testMaxParticipants)), closeSoftKeyboard());

        // Set all dates using the buttons - accepting defaults
        onView(withId(R.id.buttonRegStartDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonRegStartTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonRegEndDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonRegEndTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonEventStartDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonEventStartTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonEventEndDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonEventEndTime)).perform(click());
        onView(withText("OK")).perform(click());

        // Create the event
        onView(withId(R.id.buttonCreateEvent)).perform(click());

        // Wait for the event to be created
        SystemClock.sleep(1000);

        // Get the created event
        User organizer = testBehavior.getUser(androidId).getResult();
        Facility facility = organizer.getFacility();
        Event createdEvent = facility.getEvents().stream()
                .filter(e -> e.getName().equals(testEventName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Event not found"));

        // Create and set up entrant user
        String entrantId = "test_entrant_" + System.currentTimeMillis();
        User entrantUser = new User(entrantId);
        entrantUser.setName("Test Entrant");
        entrantUser.setEmail("entrant@test.com");
        testBehavior.addUser(entrantUser);

        // Set up mock task for entrant authentication
        TaskCompletionSource<User> mockEntrantTask = new TaskCompletionSource<>();
        mockEntrantTask.setResult(entrantUser);
        UserAuthenticator.setMockTask(mockEntrantTask.getTask());

        // Switch to entrant view
        GlobalRepository.setLoggedInUser(entrantUser);

        // Launch MainActivity for entrant and process QR code
        ActivityScenario<MainActivity> entrantScenario = ActivityScenario.launch(MainActivity.class);
        entrantScenario.onActivity(activity -> {
            GlobalRepository.setBehavior(testBehavior);
            HandleQRScan.processQRCode("cando-" + createdEvent.getId(), activity);
        });

        SystemClock.sleep(1000);

        // Verify event details and join waitlist
        onView(withId(R.id.class_tile)).check(matches(withText(testEventName)));
        onView(withId(R.id.join_waiting_list)).perform(click());

        // Set up mock task for organizer authentication
        TaskCompletionSource<User> mockOrganizerTask = new TaskCompletionSource<>();
        mockOrganizerTask.setResult(organizer);
        UserAuthenticator.setMockTask(mockOrganizerTask.getTask());

        // Switch back to organizer view
        GlobalRepository.setLoggedInUser(organizer);

        // Re-launch OrganizerMain to ensure we have the latest state
        ActivityScenario<OrganizerMain> organizerMainScenario = ActivityScenario.launch(OrganizerMain.class);
        organizerMainScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        SystemClock.sleep(1000);

        // Click on the event to view details
        onView(withText(testEventName)).perform(click());

        // Navigate to waitlist and perform draw
        onView(withId(R.id.view_watch_list)).perform(click());

        SystemClock.sleep(1000);

        // Verify the entrant is in the waitlist
        onView(withId(R.id.text_view_user_name)).check(matches(withText("Test Entrant")));

        onView(withId(R.id.draw)).perform(click());

        // Enter number to draw in dialog
        onView(withClassName(Matchers.endsWith("EditText")))
                .perform(typeText("1"), closeSoftKeyboard());
        onView(withText("Draw")).perform(click());

        SystemClock.sleep(1000);

        // Go back to event details
        pressBack();

        // Check selected entrants list
        onView(withId(R.id.view_selected_list)).perform(click());

        SystemClock.sleep(1000);

        // Verify the entrant is in the selected list
        onView(withId(R.id.text_view_user_name)).check(matches(withText("Test Entrant")));

        // Clean up
        mainScenario.close();
        organizerScenario.close();
        entrantScenario.close();
        organizerMainScenario.close();
        UserAuthenticator.reset();
    }

    @Test
    public void testEnrollFlow() throws ExecutionException, InterruptedException {
        // First complete the necessary setup steps for organizer
        ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class);
        mainScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        // Complete organizer profile setup
        onView(withId(R.id.nameEditText)).perform(typeText(TEST_NAME), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.submitButton)).perform(click());

        // Navigate to organizer section and set up facility
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.organizer_button)).perform(click());

        ActivityScenario.launch(OrganizerTransition.class)
                .onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        onView(withId(R.id.facilityNameInput)).perform(typeText(TEST_FACILITY_NAME), closeSoftKeyboard());
        onView(withId(R.id.facilityAddressInput)).perform(typeText(TEST_FACILITY_ADDRESS), closeSoftKeyboard());
        onView(withId(R.id.saveFacilityButton)).perform(click());

        // Launch OrganizerMain and create event
        ActivityScenario<OrganizerMain> organizerScenario = ActivityScenario.launch(OrganizerMain.class);
        organizerScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        // Click FAB to create new event
        onView(withId(R.id.fab_add_event)).perform(click());

        // Fill in event details
        String testEventName = "Enrollment Test Event";
        String testEventDescription = "Test event for enrollment flow";
        int testMaxParticipants = 1;

        onView(withId(R.id.editTextEventName))
                .perform(typeText(testEventName), closeSoftKeyboard());
        onView(withId(R.id.editTextEventDescription))
                .perform(typeText(testEventDescription), closeSoftKeyboard());
        onView(withId(R.id.editTextMaxNumParticipants))
                .perform(typeText(String.valueOf(testMaxParticipants)), closeSoftKeyboard());

        // Set all dates using the buttons - accepting defaults
        onView(withId(R.id.buttonRegStartDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonRegStartTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonRegEndDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonRegEndTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonEventStartDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonEventStartTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonEventEndDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonEventEndTime)).perform(click());
        onView(withText("OK")).perform(click());

        // Create the event
        onView(withId(R.id.buttonCreateEvent)).perform(click());

        SystemClock.sleep(1000);

        // Get the created event
        User organizer = testBehavior.getUser(androidId).getResult();
        Facility facility = organizer.getFacility();
        Event createdEvent = facility.getEvents().stream()
                .filter(e -> e.getName().equals(testEventName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Event not found"));

        // Create and set up entrant user
        String entrantId = "test_entrant_" + System.currentTimeMillis();
        User entrantUser = new User(entrantId);
        entrantUser.setName("Test Entrant");
        entrantUser.setEmail("entrant@test.com");
        testBehavior.addUser(entrantUser);

        // Set up mock task for entrant authentication
        TaskCompletionSource<User> mockEntrantTask = new TaskCompletionSource<>();
        mockEntrantTask.setResult(entrantUser);
        UserAuthenticator.setMockTask(mockEntrantTask.getTask());

        // Switch to entrant view
        GlobalRepository.setLoggedInUser(entrantUser);

        // Launch MainActivity for entrant and process QR code
        ActivityScenario<MainActivity> entrantScenario = ActivityScenario.launch(MainActivity.class);
        entrantScenario.onActivity(activity -> {
            GlobalRepository.setBehavior(testBehavior);
            HandleQRScan.processQRCode("cando-" + createdEvent.getId(), activity);
        });

        SystemClock.sleep(1000);

        // Join waitlist
        onView(withId(R.id.join_waiting_list)).perform(click());

        // Set up mock task for organizer authentication
        TaskCompletionSource<User> mockOrganizerTask = new TaskCompletionSource<>();
        mockOrganizerTask.setResult(organizer);
        UserAuthenticator.setMockTask(mockOrganizerTask.getTask());

        // Switch back to organizer view
        GlobalRepository.setLoggedInUser(organizer);

        // Re-launch OrganizerMain
        ActivityScenario<OrganizerMain> organizerMainScenario = ActivityScenario.launch(OrganizerMain.class);
        organizerMainScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        SystemClock.sleep(1000);

        // Navigate to event details and perform selection
        onView(withText(testEventName)).perform(click());
        onView(withId(R.id.view_watch_list)).perform(click());
        onView(withId(R.id.draw)).perform(click());
        onView(withClassName(Matchers.endsWith("EditText")))
                .perform(typeText("1"), closeSoftKeyboard());
        onView(withText("Draw")).perform(click());

        SystemClock.sleep(1000);

        Log.d("TestEnrollFlow", "Original event: " + createdEvent.getId() + ", name: " + createdEvent.getName());

        // Verify event is in test behavior
        Event storedEvent = ((NoOpDatabaseBehavior) GlobalRepository.getBehavior()).getEvent(createdEvent.getId()).getResult();
        Log.d("TestEnrollFlow", "Stored event exists: " + (storedEvent != null));
        if (storedEvent != null) {
            Log.d("TestEnrollFlow", "Stored event name: " + storedEvent.getName());
        }

        // Switch back to entrant view
        GlobalRepository.setLoggedInUser(entrantUser);

        // Make sure the event exists in testBehavior
        testBehavior.addEvent(createdEvent);

        // Update the user's joined events
        entrantUser.addEventJoined(createdEvent.getId());
        testBehavior.addUser(entrantUser);

        // Debug current state
        Log.d("TestEnrollFlow", "Before MainActivity - event ID: " + createdEvent.getId());
        Log.d("TestEnrollFlow", "Before MainActivity - user events: " + entrantUser.getEventsJoined());

        // Launch MainActivity
        ActivityScenario<MainActivity> entrantHomeScenario = ActivityScenario.launch(MainActivity.class);

        // Use onActivity to set up the state BEFORE any fragments are created
        entrantHomeScenario.onActivity(activity -> {
            // First set the test behavior
            GlobalRepository.setBehavior(testBehavior);

            // Then create a fresh user instance with the same data
            User freshUser = new User(entrantUser.getId());
            freshUser.setName(entrantUser.getName());
            freshUser.setEmail(entrantUser.getEmail());
            freshUser.addEventJoined(createdEvent.getId());

            // Update both the repository and the test behavior
            testBehavior.addUser(freshUser);
            GlobalRepository.setLoggedInUser(freshUser);

            Log.d("TestEnrollFlow", "After state setup - Repository user events: " +
                    GlobalRepository.getLoggedInUser().getEventsJoined());

            // Force recreation of HomeActivity with the new state
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeActivity())
                    .commitNow();
        });

        // Wait for state to settle
        SystemClock.sleep(2000);

        // Debug UI state
        onView(withId(R.id.hs_events_list)).check((view, noViewFoundException) -> {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }
            RecyclerView recyclerView = (RecyclerView) view;
            Log.d("TestEnrollFlow", "Final RecyclerView visibility: " + recyclerView.getVisibility());
            Log.d("TestEnrollFlow", "Final adapter item count: " +
                    (recyclerView.getAdapter() != null ? recyclerView.getAdapter().getItemCount() : "null adapter"));

            User finalUser = GlobalRepository.getLoggedInUser();
            Log.d("TestEnrollFlow", "Final user events: " +
                    (finalUser != null ? finalUser.getEventsJoined() : "null user"));
        });

        // Now proceed with clicking if everything is set up
        onView(withId(R.id.hs_events_list))
                .check(matches(isDisplayed()));

        onView(withText(testEventName)).perform(click());

        // Accept the invitation
        onView(withId(R.id.accept_invitation)).perform(click());

        SystemClock.sleep(1000);

        // Switch back to organizer view
        GlobalRepository.setLoggedInUser(organizer);

        // Re-launch OrganizerMain
        ActivityScenario<OrganizerMain> finalOrganizerScenario = ActivityScenario.launch(OrganizerMain.class);
        finalOrganizerScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        SystemClock.sleep(1000);

        // Navigate to event details and check enrolled list
        onView(withText(testEventName)).perform(click());
        onView(withId(R.id.view_enrolled_list)).perform(click());

        SystemClock.sleep(1000);

        // Verify the entrant is in the enrolled list
        onView(withId(R.id.text_view_user_name)).check(matches(withText("Test Entrant")));

        // Clean up
        mainScenario.close();
        organizerScenario.close();
        entrantScenario.close();
        organizerMainScenario.close();
        entrantHomeScenario.close();
        finalOrganizerScenario.close();
        UserAuthenticator.reset();
    }

    @Test
    public void testCancelFlow() {
        // First complete the necessary setup steps for organizer
        ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class);
        mainScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        // Complete organizer profile setup
        onView(withId(R.id.nameEditText)).perform(typeText(TEST_NAME), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.submitButton)).perform(click());

        // Navigate to organizer section and set up facility
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.organizer_button)).perform(click());

        ActivityScenario.launch(OrganizerTransition.class)
                .onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        onView(withId(R.id.facilityNameInput)).perform(typeText(TEST_FACILITY_NAME), closeSoftKeyboard());
        onView(withId(R.id.facilityAddressInput)).perform(typeText(TEST_FACILITY_ADDRESS), closeSoftKeyboard());
        onView(withId(R.id.saveFacilityButton)).perform(click());

        // Launch OrganizerMain and create event
        ActivityScenario<OrganizerMain> organizerScenario = ActivityScenario.launch(OrganizerMain.class);
        organizerScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        // Click FAB to create new event
        onView(withId(R.id.fab_add_event)).perform(click());

        // Fill in event details
        String testEventName = "Enrollment Test Event";
        String testEventDescription = "Test event for enrollment flow";
        int testMaxParticipants = 1;

        onView(withId(R.id.editTextEventName))
                .perform(typeText(testEventName), closeSoftKeyboard());
        onView(withId(R.id.editTextEventDescription))
                .perform(typeText(testEventDescription), closeSoftKeyboard());
        onView(withId(R.id.editTextMaxNumParticipants))
                .perform(typeText(String.valueOf(testMaxParticipants)), closeSoftKeyboard());

        // Set all dates using the buttons - accepting defaults
        onView(withId(R.id.buttonRegStartDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonRegStartTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonRegEndDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonRegEndTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonEventStartDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonEventStartTime)).perform(click());
        onView(withText("OK")).perform(click());

        onView(withId(R.id.buttonEventEndDate)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.buttonEventEndTime)).perform(click());
        onView(withText("OK")).perform(click());

        // Create the event
        onView(withId(R.id.buttonCreateEvent)).perform(click());

        SystemClock.sleep(1000);

        // Get the created event
        User organizer = testBehavior.getUser(androidId).getResult();
        Facility facility = organizer.getFacility();
        Event createdEvent = facility.getEvents().stream()
                .filter(e -> e.getName().equals(testEventName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Event not found"));

        // Create and set up entrant user
        String entrantId = "test_entrant_" + System.currentTimeMillis();
        User entrantUser = new User(entrantId);
        entrantUser.setName("Test Entrant");
        entrantUser.setEmail("entrant@test.com");
        testBehavior.addUser(entrantUser);

        // Set up mock task for entrant authentication
        TaskCompletionSource<User> mockEntrantTask = new TaskCompletionSource<>();
        mockEntrantTask.setResult(entrantUser);
        UserAuthenticator.setMockTask(mockEntrantTask.getTask());

        // Switch to entrant view
        GlobalRepository.setLoggedInUser(entrantUser);

        // Launch MainActivity for entrant and process QR code
        ActivityScenario<MainActivity> entrantScenario = ActivityScenario.launch(MainActivity.class);
        entrantScenario.onActivity(activity -> {
            GlobalRepository.setBehavior(testBehavior);
            HandleQRScan.processQRCode("cando-" + createdEvent.getId(), activity);
        });

        SystemClock.sleep(1000);

        // Join waitlist
        onView(withId(R.id.join_waiting_list)).perform(click());

        // Set up mock task for organizer authentication
        TaskCompletionSource<User> mockOrganizerTask = new TaskCompletionSource<>();
        mockOrganizerTask.setResult(organizer);
        UserAuthenticator.setMockTask(mockOrganizerTask.getTask());

        // Switch back to organizer view
        GlobalRepository.setLoggedInUser(organizer);

        // Re-launch OrganizerMain
        ActivityScenario<OrganizerMain> organizerMainScenario = ActivityScenario.launch(OrganizerMain.class);
        organizerMainScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        SystemClock.sleep(1000);

        // Navigate to event details and perform selection
        onView(withText(testEventName)).perform(click());
        onView(withId(R.id.view_watch_list)).perform(click());
        onView(withId(R.id.draw)).perform(click());
        onView(withClassName(Matchers.endsWith("EditText")))
                .perform(typeText("1"), closeSoftKeyboard());
        onView(withText("Draw")).perform(click());

        SystemClock.sleep(1000);

        Log.d("TestEnrollFlow", "Original event: " + createdEvent.getId() + ", name: " + createdEvent.getName());

        // Verify event is in test behavior
        Event storedEvent = ((NoOpDatabaseBehavior) GlobalRepository.getBehavior()).getEvent(createdEvent.getId()).getResult();
        Log.d("TestEnrollFlow", "Stored event exists: " + (storedEvent != null));
        if (storedEvent != null) {
            Log.d("TestEnrollFlow", "Stored event name: " + storedEvent.getName());
        }

        // Switch back to entrant view
        GlobalRepository.setLoggedInUser(entrantUser);

        // Make sure the event exists in testBehavior
        testBehavior.addEvent(createdEvent);

        // Update the user's joined events
        entrantUser.addEventJoined(createdEvent.getId());
        testBehavior.addUser(entrantUser);

        // Debug current state
        Log.d("TestEnrollFlow", "Before MainActivity - event ID: " + createdEvent.getId());
        Log.d("TestEnrollFlow", "Before MainActivity - user events: " + entrantUser.getEventsJoined());

        // Launch MainActivity
        ActivityScenario<MainActivity> entrantHomeScenario = ActivityScenario.launch(MainActivity.class);

        // Use onActivity to set up the state BEFORE any fragments are created
        entrantHomeScenario.onActivity(activity -> {
            // First set the test behavior
            GlobalRepository.setBehavior(testBehavior);

            // Then create a fresh user instance with the same data
            User freshUser = new User(entrantUser.getId());
            freshUser.setName(entrantUser.getName());
            freshUser.setEmail(entrantUser.getEmail());
            freshUser.addEventJoined(createdEvent.getId());

            // Update both the repository and the test behavior
            testBehavior.addUser(freshUser);
            GlobalRepository.setLoggedInUser(freshUser);

            Log.d("TestEnrollFlow", "After state setup - Repository user events: " +
                    GlobalRepository.getLoggedInUser().getEventsJoined());

            // Force recreation of HomeActivity with the new state
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeActivity())
                    .commitNow();
        });

        // Wait for state to settle
        SystemClock.sleep(2000);

        // Debug UI state
        onView(withId(R.id.hs_events_list)).check((view, noViewFoundException) -> {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }
            RecyclerView recyclerView = (RecyclerView) view;
            Log.d("TestEnrollFlow", "Final RecyclerView visibility: " + recyclerView.getVisibility());
            Log.d("TestEnrollFlow", "Final adapter item count: " +
                    (recyclerView.getAdapter() != null ? recyclerView.getAdapter().getItemCount() : "null adapter"));

            User finalUser = GlobalRepository.getLoggedInUser();
            Log.d("TestEnrollFlow", "Final user events: " +
                    (finalUser != null ? finalUser.getEventsJoined() : "null user"));
        });

        // Now proceed with clicking if everything is set up
        onView(withId(R.id.hs_events_list))
                .check(matches(isDisplayed()));

        onView(withText(testEventName)).perform(click());

        // Reject the invitation
        onView(withId(R.id.reject_invitation)).perform(click());

        SystemClock.sleep(1000);

        // Switch back to organizer view
        GlobalRepository.setLoggedInUser(organizer);

        // Re-launch OrganizerMain
        ActivityScenario<OrganizerMain> finalOrganizerScenario = ActivityScenario.launch(OrganizerMain.class);
        finalOrganizerScenario.onActivity(activity -> GlobalRepository.setBehavior(testBehavior));

        SystemClock.sleep(1000);

        // Navigate to event details and check cancelled list
        onView(withText(testEventName)).perform(click());
        onView(withId(R.id.view_cancelled_list)).perform(click());

        SystemClock.sleep(1000);

        // Verify the entrant is in the cancelled list
        onView(withId(R.id.text_view_user_name)).check(matches(withText("Test Entrant")));

        // Clean up
        mainScenario.close();
        organizerScenario.close();
        entrantScenario.close();
        finalOrganizerScenario.close();
        UserAuthenticator.reset();
    }
}
