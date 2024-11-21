package com.bugoff.can_do;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.database.MockGlobalRepository;
import com.bugoff.can_do.database.UserAuthenticator;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.organizer.OrganizerTransition;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Get the Android ID
        androidId = Settings.Secure.getString(
                InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Setup mock Firestore
        mockDb = mock(FirebaseFirestore.class);
        mockDocRef = mock(DocumentReference.class);
        when(mockDb.collection(anyString())).thenReturn(mock(com.google.firebase.firestore.CollectionReference.class));
        when(mockDb.collection(anyString()).document(anyString())).thenReturn(mockDocRef);

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
        // First complete user setup
        ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class);

        // Complete user profile setup
        onView(withId(R.id.nameEditText))
                .perform(typeText(TEST_NAME), closeSoftKeyboard());
        onView(withId(R.id.emailEditText))
                .perform(typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.submitButton)).perform(click());

        // Navigate to profile
        onView(withId(R.id.nav_profile)).perform(click());

        // Launch organizer transition activity
        mainScenario.onActivity(activity -> {
            Intent intent = new Intent(activity, OrganizerTransition.class);
            activity.startActivity(intent);
        });

        ActivityScenario<OrganizerTransition> orgScenario =
                ActivityScenario.launch(OrganizerTransition.class);

        // Enter facility information
        onView(withId(R.id.facilityNameInput))
                .perform(typeText(TEST_FACILITY_NAME), closeSoftKeyboard());
        onView(withId(R.id.facilityAddressInput))
                .perform(typeText(TEST_FACILITY_ADDRESS), closeSoftKeyboard());

        // Save the facility
        onView(withId(R.id.saveFacilityButton)).perform(click());

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

        orgScenario.close();
        mainScenario.close();
    }
}
