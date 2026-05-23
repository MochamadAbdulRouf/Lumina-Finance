package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.FinanceApp
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Lumina Finance", appName)
  }

  @Test
  fun `instantiate finance viewmodel`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = FinanceViewModel(app)
    assertNotNull(viewModel)
  }

  @Test
  fun `verify auth screen inputs exist`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = FinanceViewModel(app)
    composeTestRule.setContent {
      MyApplicationTheme {
        AuthScreen(viewModel = viewModel)
      }
    }

    // Verify critical authentication controls exist and are displayed
    composeTestRule.onNodeWithTag("email_input").assertExists()
    composeTestRule.onNodeWithTag("password_input").assertExists()
  }

  @Test
  fun `verify spending analytics screen loads without crashing`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = FinanceViewModel(app)
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.screens.SpendingAnalyticsScreen(viewModel = viewModel)
      }
    }
    // Simple assert to make sure rendering proceeded and finished without throwing Exception
    assertNotNull(viewModel)
  }
}
