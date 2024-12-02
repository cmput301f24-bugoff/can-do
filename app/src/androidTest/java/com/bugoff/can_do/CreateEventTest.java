package com.bugoff.can_do;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.database.MockGlobalRepository;
import com.bugoff.can_do.database.UserAuthenticator;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.organizer.OrganizerMain;
import com.bugoff.can_do.organizer.OrganizerTransition;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class CreateEventTest {
    private static final String TEST_NAME = "Test User";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FACILITY_NAME = "Test Facility";
    private static final String TEST_FACILITY_ADDRESS = "123 Test St";
    private String androidId;
    private MockGlobalRepository mockRepository;

    @Mock
    private FirebaseFirestore mockDb;

    @Mock
    private DocumentReference mockDocRef;

    @Rule
    public GrantPermissionRule locationPermissionRule = GrantPermissionRule.grant(
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION
    );

    @Rule
    public GrantPermissionRule notificationPermissionRule =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                    GrantPermissionRule.grant(POST_NOTIFICATIONS) :
                    GrantPermissionRule.grant();

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Grant permissions programmatically for older Android versions
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Context context = instrumentation.getTargetContext();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            instrumentation.getUiAutomation().executeShellCommand(
                    "pm grant " + context.getPackageName() + " android.permission.ACCESS_FINE_LOCATION"
            );
            instrumentation.getUiAutomation().executeShellCommand(
                    "pm grant " + context.getPackageName() + " android.permission.ACCESS_COARSE_LOCATION"
            );
        }

        // Get the Android ID
        androidId = Settings.Secure.getString(
                InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Setup mock Firestore and collections
        mockDb = mock(FirebaseFirestore.class);
        DocumentReference mockEventDocRef = mock(DocumentReference.class);
        when(mockEventDocRef.getId()).thenReturn("test-event-id");
        when(mockEventDocRef.set(any(Map.class))).thenReturn(Tasks.forResult(null));

        CollectionReference mockEventsCollection = mock(CollectionReference.class);
        when(mockEventsCollection.document()).thenReturn(mockEventDocRef);
        when(mockEventsCollection.document(anyString())).thenReturn(mockEventDocRef);
        when(mockDb.collection("events")).thenReturn(mockEventsCollection);

        // Enable test mode and set mock Firestore
        GlobalRepository.setTestMode(true);
        GlobalRepository.setMockFirestore(mockDb);

        // Initialize mock repository
        mockRepository = MockGlobalRepository.getInstance();

        // Set the mock repository in UserAuthenticator
        UserAuthenticator.setRepository(mockRepository);

        // Create a mock user without name and email to simulate new user
        User mockUser = new User(androidId);
        mockUser.setName("");
        mockUser.setEmail("");

        // Set up mock user authentication to return the "new" user
        Task<User> mockUserTask = Tasks.forResult(mockUser);
        UserAuthenticator.setMockTask(mockUserTask);

        // Add the blank user to mock repository
        MockGlobalRepository.addUser(mockUser);
    }

    @After
    public void tearDown() {
        // Reset the mock repository instance
        MockGlobalRepository.resetInstance();

        // Reset UserAuthenticator
        UserAuthenticator.reset();

        // Reset test mode
        GlobalRepository.setTestMode(false);
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
            activity.setRepository(mockRepository);
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
                    activity.setRepository(mockRepository);
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
                    activity.setRepository(mockRepository);
                });

        // Verify we moved to the organizer main screen
        onView(withId(R.id.bottom_navigation_organizer)).check(matches(isDisplayed()));

        // Verify facility was created in mock repository
        User user = mockRepository.getUser(androidId).getResult();
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
        mainScenario.onActivity(activity -> activity.setRepository(mockRepository));

        // Complete user profile setup
        onView(withId(R.id.nameEditText)).perform(typeText(TEST_NAME), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.submitButton)).perform(click());

        // Navigate to organizer section and set up facility
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.organizer_button)).perform(click());

        ActivityScenario.launch(OrganizerTransition.class)
                .onActivity(activity -> activity.setRepository(mockRepository));

        onView(withId(R.id.facilityNameInput)).perform(typeText(TEST_FACILITY_NAME), closeSoftKeyboard());
        onView(withId(R.id.facilityAddressInput)).perform(typeText(TEST_FACILITY_ADDRESS), closeSoftKeyboard());
        onView(withId(R.id.saveFacilityButton)).perform(click());

        // Launch OrganizerMain and set repository
        ActivityScenario<OrganizerMain> organizerScenario = ActivityScenario.launch(OrganizerMain.class);
        organizerScenario.onActivity(activity -> activity.setRepository(mockRepository));

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
        User user = mockRepository.getUser(androidId).getResult();
        Facility facility = user.getFacility();
        assertNotNull("Facility should exist", facility);

        // Wait for the event to be created and added to the facility
        SystemClock.sleep(1000); // Give time for async operations

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
    }
}
