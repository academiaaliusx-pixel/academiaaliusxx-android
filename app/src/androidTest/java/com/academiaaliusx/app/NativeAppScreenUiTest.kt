package com.academiaaliusx.app

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeAppScreenUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearLocalState() {
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.getSharedPreferences("aax-native-local", 0).edit().clear().commit()
        }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
    }

    @Test
    fun bottomTabsSwitchBetweenNativeScreens() {
        composeRule.onNodeWithTag("nav-community").performClick()
        composeRule.onNodeWithText("Funciones pendientes de integración segura").assertExists()

        composeRule.onNodeWithTag("nav-profile").performClick()
        composeRule.onNodeWithText("Tus datos están guardados en este dispositivo.").assertExists()

        composeRule.onNodeWithTag("nav-home").performClick()
        composeRule.onNodeWithText("Tu actividad").assertExists()
    }

    @Test
    fun tournamentDetailShowsIdTogglesLocalRegistrationAndReturns() {
        composeRule.onNodeWithTag("nav-tournaments").performClick()
        composeRule.onNodeWithTag("tournament-details-bx-local").performClick()
        composeRule.onNodeWithTag("tournament-detail-screen").assertExists()
        composeRule.onNodeWithText("ID local: bx-local").assertExists()

        composeRule.onNodeWithTag("tournament-registration-toggle").performClick()
        composeRule.onNodeWithText("Retirar inscripción local").assertExists()
        composeRule.onNodeWithTag("tournament-detail-back").performClick()
        composeRule.onNodeWithText("Beyblade X — torneo de muestra").assertExists()
    }

    @Test
    fun localTournamentCreationAndDeletionRequireConfirmation() {
        composeRule.onNodeWithTag("nav-tournaments").performClick()
        composeRule.onNodeWithTag("tournament-create").performClick()
        composeRule.onNodeWithTag("title-dialog-input").performTextInput("Torneo UI")
        composeRule.onNodeWithTag("title-dialog-save").performClick()
        composeRule.onNodeWithText("Torneo UI").assertExists()

        composeRule.onAllNodesWithText("Detalles")[3].performClick()
        composeRule.onNodeWithTag("tournament-detail-screen").assertExists()
        composeRule.onNodeWithTag("tournament-detail-delete").performClick()
        composeRule.onNodeWithTag("tournament-delete-confirm").performClick()
        composeRule.onNodeWithText("Torneo UI").assertDoesNotExist()
    }

    @Test
    fun profileNameCanBeEditedAndSaved() {
        composeRule.onNodeWithTag("nav-profile").performClick()
        composeRule.onNodeWithTag("profile-edit").performClick()
        val nameField = composeRule.onNodeWithTag("profile-name-input")
        nameField.performTextClearance()
        nameField.performTextInput("Usuario UI")
        composeRule.onNodeWithTag("profile-save").performClick()
        composeRule.onNodeWithText("Usuario UI").assertExists()
    }
}
