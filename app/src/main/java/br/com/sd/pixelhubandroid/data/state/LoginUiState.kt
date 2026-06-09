package br.com.sd.pixelhubandroid.data.state

data class LoginUiState(
    val username: String = "",
    val serverIp: String = "10.0.2.2",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)