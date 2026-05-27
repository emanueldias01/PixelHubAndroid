package br.com.sd.pixelhubandroid.features

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import br.com.sd.pixelhubandroid.R
import br.com.sd.pixelhubandroid.data.data.PointData
import br.com.sd.pixelhubandroid.data.state.DrawingTool
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

    // Pan and Zoom state
    var scale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

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

    // Optimization to avoid ANR: Render the full board into a Bitmap only when pixels change.
    val boardBitmap = remember(boardState.boardPixels, boardState.boardWidth, boardState.boardHeight) {
        boardState.boardPixels?.let { pixels ->
            if (pixels.isEmpty()) return@let null
            try {
                val bmp = Bitmap.createBitmap(
                    boardState.boardWidth,
                    boardState.boardHeight,
                    Bitmap.Config.ARGB_8888
                )
                bmp.setPixels(
                    pixels,
                    0,
                    boardState.boardWidth,
                    0, 0,
                    boardState.boardWidth,
                    boardState.boardHeight
                )
                bmp.asImageBitmap()
            } catch (e: Exception) {
                null
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
            
            Toolbox(
                selectedTool = boardState.selectedTool,
                selectedColor = boardState.selectedColor,
                onToolSelected = { boardViewModel.selectTool(it) },
                onColorSelected = { boardViewModel.selectColor(it) }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp)
                    .background(Color.LightGray, shape = MaterialTheme.shapes.medium)
                    .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
                    .clipToBounds() // Keep drawing inside the board area when zooming/panning
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = panOffset.x,
                            translationY = panOffset.y
                        )
                        .pointerInput(boardState.selectedTool, scale, panOffset) {
                            when (boardState.selectedTool) {
                                DrawingTool.PAN -> {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(0.5f, 50f)
                                        panOffset += pan
                                    }
                                }
                                DrawingTool.PENCIL, DrawingTool.ERASER -> {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        
                                        // Transform screen coordinates to board coordinates
                                        val start = (change.position - dragAmount - panOffset) / scale
                                        val end = (change.position - panOffset) / scale
                                        
                                        val color = if (boardState.selectedTool == DrawingTool.ERASER) Color.White else boardState.selectedColor
                                        val width = if (boardState.selectedTool == DrawingTool.ERASER) 20f else 5f

                                        boardViewModel.sendDrawAction(
                                            start = PointData(start.x, start.y),
                                            end = PointData(end.x, end.y),
                                            color = color,
                                            width = width / scale // Maintain consistent visual stroke width
                                        )
                                    }
                                }
                                DrawingTool.BUCKET -> {
                                    detectTapGestures { tapOffset ->
                                        val transformedPoint = (tapOffset - panOffset) / scale
                                        boardViewModel.sendBucketAction(PointData(transformedPoint.x, transformedPoint.y))
                                    }
                                }
                            }
                        }
                ) {
                    // Draw the static board from the optimized Bitmap
                    boardBitmap?.let {
                        drawImage(it)
                    }

                    // Render temporary lines on top
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
fun Toolbox(
    selectedTool: DrawingTool,
    selectedColor: Color,
    onToolSelected: (DrawingTool) -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(Color.Black, Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan, Color.Magenta)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            IconButton(
                onClick = { onToolSelected(DrawingTool.PENCIL) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (selectedTool == DrawingTool.PENCIL) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) {
                Icon(Icons.Default.Create, contentDescription = "Pencil")
            }
            IconButton(
                onClick = { onToolSelected(DrawingTool.ERASER) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (selectedTool == DrawingTool.ERASER) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) {
                Icon(painter = painterResource(R.drawable.outline_cleaning_24), contentDescription = "Eraser")
            }
            IconButton(
                onClick = { onToolSelected(DrawingTool.BUCKET) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (selectedTool == DrawingTool.BUCKET) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) {
                Icon(painter = painterResource(R.drawable.outline_cleaning_bucket_24), contentDescription = "Bucket")
            }
        }
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(colors) { color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color, CircleShape)
                        .border(
                            width = if (selectedColor == color) 2.dp else 0.dp,
                            color = if (selectedColor == color) MaterialTheme.colorScheme.outline else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(color) }
                )
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
