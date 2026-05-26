package br.com.sd.pixelhubandroid.data.state

data class LoginUiState(
    val username: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)