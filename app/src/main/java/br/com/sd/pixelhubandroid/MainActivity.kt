package br.com.sd.pixelhubandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import br.com.sd.pixelhubandroid.navigation.Navigation
import br.com.sd.pixelhubandroid.ui.theme.PixelHubAndroidTheme
import br.com.sd.pixelhubandroid.viewmodels.BoardViewModel
import br.com.sd.pixelhubandroid.viewmodels.LoginViewModel

class MainActivity : ComponentActivity() {
    
    private val loginViewModel: LoginViewModel by viewModels()
    private val boardViewModel: BoardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PixelHubAndroidTheme {
                Navigation(loginViewModel, boardViewModel)
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing && loginViewModel.uiState.value.isLoggedIn) {
            loginViewModel.logout()
        }
        super.onDestroy()
    }
}
