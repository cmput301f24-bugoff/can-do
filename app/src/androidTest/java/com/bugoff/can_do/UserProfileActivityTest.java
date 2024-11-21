package com.bugoff.can_do;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserProfileActivityTest {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setScenario() {
        Intents.init();
        scenario = ActivityScenario.launch(MainActivity.class);
    }

    @After
    public void tearDownScenario() {
        Intents.release();
        if (scenario != null) { scenario.close(); }
    }

    @Test
    public void testEditName() {
        // Launch the Profile Screen
        onView(withId(R.id.nav_profile)).perform(click());
        // Click the "Edit Name" button
        onView(withId(R.id.name_button)).perform(click());
        // Type "Paris" in the first name editText
        onView(withId(R.id.input_first_name)).perform(ViewActions.typeText("Paris"));
        onView(withId(R.id.input_first_name)).perform(ViewActions.closeSoftKeyboard());
        // Type "Hilton" in the last name editText
        onView(withId(R.id.input_last_name)).perform(ViewActions.typeText("Hilton"));
        onView(withId(R.id.input_last_name)).perform(ViewActions.closeSoftKeyboard());
        // Click Confirm
        onView(withText("CONFIRM")).perform(click());
        // Check if "Paris Hilton" is displayed on the profile screen
        onView(withText("Paris")).check(matches(isDisplayed()));
        onView(withText("Hilton")).check(matches(isDisplayed()));
    }
}
