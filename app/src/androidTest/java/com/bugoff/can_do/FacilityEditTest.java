package com.bugoff.can_do;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.bugoff.can_do.R;
import com.bugoff.can_do.organizer.FacilityEdit;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FacilityEditTest {

    private FirebaseFirestore db;

    @Rule
    public ActivityTestRule<FacilityEdit> activityRule = new ActivityTestRule<>(FacilityEdit.class, true, false);

    @Before
    public void setUp() {
        // Initialize Firebase Firestore for testing
        db = FirebaseFirestore.getInstance();

        // Clear data if necessary for a clean test environment
        db.collection("facilities").document("test_android_id").delete();
    }

    @After
    public void tearDown() {
        // Clean up any test data
        db.collection("facilities").document("test_android_id").delete();
    }

    @Test
    public void testLoadFacilityData_NoExistingFacility() {
        // Start the Activity with a test Android ID
        Intent intent = new Intent();
        intent.putExtra("android_id", "test_android_id");
        activityRule.launchActivity(intent);

        // Verify that the facility name and address fields are empty
        Espresso.onView(ViewMatchers.withId(R.id.facilityNameInput))
                .check(ViewAssertions.matches(ViewMatchers.withText("")));
        Espresso.onView(ViewMatchers.withId(R.id.facilityAddressInput))
                .check(ViewAssertions.matches(ViewMatchers.withText("")));
    }

    @Test
    public void testAreFieldsValid_InvalidInput() {
        // Launch the Activity
        activityRule.launchActivity(new Intent());

        // Leave facility name and address fields empty
        Espresso.onView(ViewMatchers.withId(R.id.saveFacilityButton)).perform(ViewActions.click());

        // Check that the Toast message is displayed
        Espresso.onView(ViewMatchers.withText("Please fill out both fields"))
                .inRoot(new ToastMatcher()) // Custom matcher for Toasts
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testSaveFacilityData_Success() {
        // Launch the Activity
        activityRule.launchActivity(new Intent());

        // Enter valid facility data
        Espresso.onView(ViewMatchers.withId(R.id.facilityNameInput))
                .perform(ViewActions.typeText("Test Facility"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.facilityAddressInput))
                .perform(ViewActions.typeText("123 Test St"), ViewActions.closeSoftKeyboard());

        // Click save button
        Espresso.onView(ViewMatchers.withId(R.id.saveFacilityButton)).perform(ViewActions.click());

        // Check that the Toast message is displayed
        Espresso.onView(ViewMatchers.withText("Facility saved successfully"))
                .inRoot(new ToastMatcher()) // Custom matcher for Toasts
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Verify that data was saved in Firestore
        db.collection("facilities").document("test_android_id").get()
                .addOnSuccessListener(documentSnapshot -> {
                    assertNotNull(documentSnapshot);
                    assertEquals("Test Facility", documentSnapshot.getString("name"));
                    assertEquals("123 Test St", documentSnapshot.getString("address"));
                });
    }
}
