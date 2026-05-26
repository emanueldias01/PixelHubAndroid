package br.com.sd.pixelhubandroid.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.sd.pixelhubandroid.features.BoardScreen
import br.com.sd.pixelhubandroid.features.LoginScreen
import br.com.sd.pixelhubandroid.viewmodels.LoginViewModel

@Composable
fun Navigation() {
    val controller = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()

    NavHost(
        navController = controller,
        startDestination = "login"
    ) {

        composable(route = "login") {
            LoginScreen(controller, loginViewModel)
        }

        composable(route = "board") {
            BoardScreen(controller, loginViewModel)
        }

    }
}
