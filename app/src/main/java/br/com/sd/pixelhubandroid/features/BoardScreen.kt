package br.com.sd.pixelhubandroid.features

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import br.com.sd.pixelhubandroid.data.data.PointData
import br.com.sd.pixelhubandroid.viewmodels.BoardViewModel
import br.com.sd.pixelhubandroid.viewmodels.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    navController: NavController,
    loginViewModel: LoginViewModel,
    boardViewModel: BoardViewModel = viewModel()
) {

    val loginState by loginViewModel.uiState.collectAsState()
    val boardState by boardViewModel.uiState.collectAsState()

    LaunchedEffect(loginState.username) {
        if (loginState.username.isNotEmpty()) {
            boardViewModel.connect(loginState.username)
        }
    }

    LaunchedEffect(loginState.isLoggedIn) {
        if (!loginState.isLoggedIn) {
            navController.navigate("login") {
                popUpTo("board") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PixelHub Board", fontSize = 18.sp)
                        Text(
                            text = if (boardState.isConnected) "Conectado" else "Desconectado",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (boardState.isConnected) Color.Green else Color.Red
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { loginViewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ActiveUsersList(boardState.activeUsers)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp)
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val start = change.position - dragAmount
                                val end = change.position
                                
                                boardViewModel.sendDrawAction(
                                    start = PointData(start.x, start.y),
                                    end = PointData(end.x, end.y),
                                    color = Color.Black,
                                    width = 5f
                                )
                            }
                        }
                ) {
                    boardState.lines.forEach { line ->
                        drawLine(
                            color = line.color,
                            start = androidx.compose.ui.geometry.Offset(line.start.x, line.start.y),
                            end = androidx.compose.ui.geometry.Offset(line.end.x, line.end.y),
                            strokeWidth = line.strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveUsersList(users: Set<String>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Na sessão:", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(users.toList()) { user ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = user,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardScreenPreview() {
    BoardScreen(rememberNavController(), LoginViewModel(), BoardViewModel())
}
