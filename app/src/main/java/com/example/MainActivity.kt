package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.screens.FinanceApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Global Uncaught Exception Handler to capture and print any startup or runtime failures
    val sysHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      android.util.Log.e("CRASH_DUMP", "CRITICAL UNCAUGHT EXCEPTION on thread ${thread.name}: ${throwable.message}", throwable)
      throwable.printStackTrace()
      sysHandler?.uncaughtException(thread, throwable)
    }

    try {
      enableEdgeToEdge()
    } catch (e: Exception) {
      android.util.Log.e("CRASH_DUMP", "enableEdgeToEdge failed gracefully: ${e.message}", e)
    }
    setContent {
      MyApplicationTheme {
        FinanceApp()
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}
