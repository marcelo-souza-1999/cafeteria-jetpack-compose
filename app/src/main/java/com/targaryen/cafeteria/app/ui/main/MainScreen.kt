package com.targaryen.cafeteria.app.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import com.targaryen.cafeteria.app.ui.main.MainScreenUiState.Success
import com.targaryen.cafeteria.app.ui.main.MainScreenUiState.Error
import com.targaryen.cafeteria.app.ui.main.MainScreenUiState.Loading
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme

@Composable
fun MainScreen(
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = koinViewModel(),
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  when (val current = state) {
    Loading -> {
      // Blank
    }
    is Success -> {
      MainScreen(data = current.data, modifier = modifier)
    }
    is Error -> {
      Text("Error loading data: ${current.throwable.message}")
    }
  }
}

@Composable
internal fun MainScreen(data: List<String>, modifier: Modifier = Modifier) {
  Column(modifier) { data.forEach { name -> Greeting(name) } }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
  TargaryenTheme { MainScreen(listOf("Android")) }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
fun MainScreenPortraitPreview() {
  TargaryenTheme { MainScreen(listOf("Android")) }
}
