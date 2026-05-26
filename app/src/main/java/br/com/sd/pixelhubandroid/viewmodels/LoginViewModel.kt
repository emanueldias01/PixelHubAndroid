package br.com.sd.pixelhubandroid.viewmodels

import br.com.sd.pixelhubandroid.data.state.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun changeUsername(newValue: String) {
        _uiState.value = _uiState.value.copy(username = newValue)
    }


}