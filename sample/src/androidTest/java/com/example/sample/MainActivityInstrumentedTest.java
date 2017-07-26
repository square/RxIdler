package com.example.sample;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule(MainActivity.class);

    @Test
    public void rxButtonShouldDisplayUser() {
        onView(withText("Get User Rx")).perform(click());

        onView(withId(R.id.display_users_textview))
                .check(matches(withText("rxjava: Alice Jones")));
    }

    @Test
    public void rx2ButtonShouldDisplayUser() {
        onView(withText("Get User Rx2")).perform(click());

        onView(withId(R.id.display_users_textview))
                .check(matches(withText("rxjava2: Alice Jones")));
    }
}
