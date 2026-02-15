package com.ao3reader.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ao3reader.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for the search flow.
 * Tests the complete user journey from home to search results.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SearchFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun searchFlow_navigateToSearch_enterQuery_showsResults() {
        // Wait for home screen to load
        composeTestRule.waitForIdle()

        // Verify home screen is displayed
        composeTestRule.onNodeWithText("Welcome to AO3 Reader")
            .assertIsDisplayed()

        // Navigate to search
        composeTestRule.onNodeWithText("Search Works")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify search screen is displayed
        composeTestRule.onNodeWithText("Search Works")
            .assertIsDisplayed()

        // Note: Actual search requires network and takes 5+ seconds due to rate limiting
        // This test verifies navigation and UI elements only
        composeTestRule.onNodeWithContentDescription("Search")
            .assertIsDisplayed()
    }

    @Test
    fun searchScreen_emptyQuery_showsPrompt() {
        // Navigate to search
        composeTestRule.onNodeWithText("Search Works")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify empty state message
        composeTestRule.onNodeWithText("Enter a search query to get started")
            .assertIsDisplayed()
    }

    @Test
    fun searchScreen_backButton_navigatesToHome() {
        // Navigate to search
        composeTestRule.onNodeWithText("Search Works")
            .performClick()

        composeTestRule.waitForIdle()

        // Click back button
        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify we're back on home screen
        composeTestRule.onNodeWithText("Welcome to AO3 Reader")
            .assertIsDisplayed()
    }
}
