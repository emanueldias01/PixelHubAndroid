package br.com.sd.pixelhubandroid.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.sd.pixelhubandroid.features.BoardScreen
import br.com.sd.pixelhubandroid.features.LoginScreen

@Composable
fun Navigation() {
    val controller = rememberNavController()

    NavHost(
        navController = controller,
        startDestination = "login"
    ) {

        composable(route = "login") {
            LoginScreen(controller)
        }

        composable(route = "board") {
            BoardScreen(controller)
        }

    }
}