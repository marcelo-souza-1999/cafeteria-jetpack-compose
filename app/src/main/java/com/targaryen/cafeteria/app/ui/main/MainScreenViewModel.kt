package com.targaryen.cafeteria.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.targaryen.cafeteria.app.data.DataRepository
import com.targaryen.cafeteria.app.ui.main.MainScreenUiState.Success
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainScreenViewModel(dataRepository: DataRepository) : ViewModel() {
    val uiState: StateFlow<MainScreenUiState> =
        dataRepository.data
            .map<List<String>, MainScreenUiState>(::Success)
            .catch { e -> emit(MainScreenUiState.Error(e)) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                MainScreenUiState.Loading
            )

    companion object {
        private const val TIMEOUT_MILLIS = 5000L
    }
}

sealed interface MainScreenUiState {
    object Loading : MainScreenUiState

    data class Error(val throwable: Throwable) : MainScreenUiState

    data class Success(val data: List<String>) : MainScreenUiState
}
