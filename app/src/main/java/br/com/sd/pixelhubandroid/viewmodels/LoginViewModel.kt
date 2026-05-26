package br.com.sd.pixelhubandroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sd.pixelhubandroid.data.data.AuthData
import br.com.sd.pixelhubandroid.data.state.LoginUiState
import br.com.sd.pixelhubandroid.network.PixelHubApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun changeUsername(newValue: String) {
        _uiState.value = _uiState.value.copy(username = newValue, errorMessage = null)
    }

    fun login() {
        val username = _uiState.value.username
        if (username.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Username cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = PixelHubApi.retrofitService.login(AuthData(username))
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "An error occurred during login"
                )
            }
        }
    }

    fun logout() {
        val username = _uiState.value.username
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                PixelHubApi.retrofitService.logout(AuthData(username))
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "An error occurred during logout"
                )
            }
        }
    }
}
