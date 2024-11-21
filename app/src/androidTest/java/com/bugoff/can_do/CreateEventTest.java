package com.bugoff.can_do;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.database.MockGlobalRepository;
import com.bugoff.can_do.database.UserAuthenticator;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
    private String androidId;
    private MockGlobalRepository mockRepository;

    @Mock
    private FirebaseFirestore mockDb;

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
        when(mockDb.collection(anyString())).thenReturn(mock(com.google.firebase.firestore.CollectionReference.class));

        // Enable test mode and set mock Firestore
        GlobalRepository.setTestMode(true);
        GlobalRepository.setMockFirestore(mockDb);

        // Initialize mock repository
        mockRepository = MockGlobalRepository.getInstance();

        // Set the mock repository in UserAuthenticator
        UserAuthenticator.setRepository(mockRepository);

        // Create a mock user without name and email to simulate new user
        User mockUser = new User(androidId) {
            @Override
            public void setRemote() {
                // Override to do nothing in tests
            }
        };

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
}
