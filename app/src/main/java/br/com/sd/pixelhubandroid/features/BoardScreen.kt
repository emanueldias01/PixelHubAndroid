package br.com.sd.pixelhubandroid.features

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import br.com.sd.pixelhubandroid.ui.theme.PixelHubAndroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(navController: NavController) {
    Scaffold(
        modifier = Modifier,
        topBar = { TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                Text(
                    text = "PixelHub",
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        ) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) { }
    }
}

@Preview
@Composable
private fun BoardScreenPreview() {
    PixelHubAndroidTheme {
        BoardScreen(rememberNavController())
    }
}
