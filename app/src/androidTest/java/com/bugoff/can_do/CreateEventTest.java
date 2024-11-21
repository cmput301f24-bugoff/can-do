package com.bugoff.can_do;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.database.UserAuthenticator;
import com.bugoff.can_do.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

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

    @Mock
    private FirebaseFirestore mockDb;
    @Mock
    private CollectionReference mockUsersCollection;
    @Mock
    private DocumentReference mockDocRef;
    @Mock
    private DocumentSnapshot mockDocSnapshot;
    @Mock
    private ListenerRegistration mockListener;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        androidId = Settings.Secure.getString(
                InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        setupFirebaseMocks();
    }

    private void setupFirebaseMocks() {
        // Setup collection references
        when(mockDb.collection("users")).thenReturn(mockUsersCollection);
        when(mockUsersCollection.document(any())).thenReturn(mockDocRef);

        // Mock document operations
        Task<Void> mockVoidTask = Tasks.forResult(null);
        when(mockDocRef.get()).thenReturn(Tasks.forResult(mockDocSnapshot));
        when(mockDocRef.set(any())).thenReturn(mockVoidTask);
        when(mockDocRef.set(any(), any())).thenReturn(mockVoidTask);

        // Mock initial user state
        when(mockDocSnapshot.exists()).thenReturn(false);
        when(mockDocSnapshot.getId()).thenReturn(androidId);

        // Create a mock user that properly handles attachListener and detachListener
        User mockUser = new User(androidId) {
            @Override
            public void attachListener() {
                // Do nothing in test
            }

            @Override
            public void detachListener() {
                // Do nothing in test
            }
        };

        // Mock UserAuthenticator
        Task<User> mockUserTask = Tasks.forResult(mockUser);
        UserAuthenticator.setMockTask(mockUserTask);

        // Mock GlobalRepository
        GlobalRepository.setTestMode(true);
        GlobalRepository.setMockFirestore(mockDb);

        // Set the mock user as the logged in user
        GlobalRepository.setLoggedInUser(mockUser);
    }

    @Test
    public void testUserProfileSetup() {
        // Launch the main activity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // Enter user information
        onView(withId(R.id.nameEditText))
                .perform(typeText(TEST_NAME), closeSoftKeyboard());

        onView(withId(R.id.emailEditText))
                .perform(typeText(TEST_EMAIL), closeSoftKeyboard());

        // Small pause to ensure UI is ready
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.submitButton)).perform(click());

        // Verify we're on the home screen
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));

        scenario.close();
    }
}