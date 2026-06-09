package br.com.sd.pixelhubandroid.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import br.com.sd.pixelhubandroid.data.data.*
import br.com.sd.pixelhubandroid.data.state.BoardUiState
import br.com.sd.pixelhubandroid.data.state.DrawingTool
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class BoardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BoardUiState())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private var currentUser: String = ""
    private var currentIp: String = ""

    fun connect(username: String, serverIp: String) {
        if (_uiState.value.isConnected && currentUser == username && currentIp == serverIp) {
            return
        }

        currentUser = username
        currentIp = serverIp

        webSocket?.close(1000, "Reconnecting")

        val request = Request.Builder()
            .url("ws://$serverIp:8080/ws")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _uiState.update { it.copy(isConnected = true) }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val jsonObject = gson.fromJson(text, JsonObject::class.java)
                    val type = jsonObject.get("type")?.asString

                    when (type) {
                        "draw" -> {
                            val message = gson.fromJson(text, DrawMessage::class.java)
                            _uiState.update { state ->
                                val newPixels = state.boardPixels?.copyOf()
                                if (newPixels != null) {
                                    applyDrawToPixels(
                                        newPixels,
                                        state.boardWidth,
                                        state.boardHeight,
                                        message
                                    )
                                }
                                state.copy(
                                    boardPixels = newPixels ?: state.boardPixels,
                                    activeUsers = state.activeUsers + message.user
                                )
                            }
                        }

                        "board" -> {
                            val message = gson.fromJson(text, BoardMessage::class.java)
                            val pixels = message.art.map {
                                try {
                                    android.graphics.Color.parseColor(it)
                                } catch (e: Exception) {
                                    android.graphics.Color.WHITE
                                }
                            }.toIntArray()
                            _uiState.update { state ->
                                state.copy(
                                    boardWidth = message.width,
                                    boardHeight = message.height,
                                    boardPixels = pixels,
                                    lines = emptyList()
                                )
                            }
                        }

                        "users" -> {
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _uiState.update { it.copy(isConnected = false) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _uiState.update { it.copy(isConnected = false) }
            }
        })
    }

    fun selectTool(tool: DrawingTool) {
        _uiState.update { it.copy(selectedTool = tool) }
    }

    fun selectColor(color: Color) {
        _uiState.update { it.copy(selectedColor = color) }
    }

    fun sendDrawAction(start: PointData, end: PointData, color: Color, width: Float) {
        val colorHex = String.format("#%06X", (0xFFFFFF and color.toArgb()))

        val startInt = PointData(start.x.roundToInt().toFloat(), start.y.roundToInt().toFloat())
        val endInt = PointData(end.x.roundToInt().toFloat(), end.y.roundToInt().toFloat())

        val message = DrawMessage(
            type = "draw",
            user = currentUser,
            start = startInt,
            end = endInt,
            color = colorHex,
            lineWidth = width
        )

        _uiState.update { state ->
            val newPixels = state.boardPixels?.copyOf()
            if (newPixels != null) {
                applyDrawToPixels(newPixels, state.boardWidth, state.boardHeight, message)
            }
            state.copy(boardPixels = newPixels ?: state.boardPixels)
        }

        webSocket?.send(gson.toJson(message))
    }

    fun sendBucketAction(point: PointData) {
        val colorHex = String.format("#%06X", (0xFFFFFF and _uiState.value.selectedColor.toArgb()))

        val pointInt = PointData(point.x.roundToInt().toFloat(), point.y.roundToInt().toFloat())

        val message = BucketMessage(
            user = currentUser,
            start = pointInt,
            color = colorHex
        )
        webSocket?.send(gson.toJson(message))
    }

    private fun applyDrawToPixels(
        pixels: IntArray,
        width: Int,
        height: Int,
        msg: DrawMessage
    ) {
        val color = try {
            android.graphics.Color.parseColor(msg.color)
        } catch (e: Exception) {
            android.graphics.Color.BLACK
        }

        val points = getLinePoints(
            msg.start.x.roundToInt(), msg.start.y.roundToInt(),
            msg.end.x.roundToInt(), msg.end.y.roundToInt()
        )

        val radius = max(0, msg.lineWidth.roundToInt() / 2)

        for ((px, py) in points) {
            for (dy in -radius..radius) {
                for (dx in -radius..radius) {
                    val nx = px + dx
                    val ny = py + dy
                    if (nx >= 0 && ny >= 0 && nx < width && ny < height) {
                        pixels[ny * width + nx] = color
                    }
                }
            }
        }
    }

    private fun getLinePoints(x0i: Int, y0i: Int, x1: Int, y1: Int): List<Pair<Int, Int>> {
        var x0 = x0i
        var y0 = y0i
        val points = mutableListOf<Pair<Int, Int>>()

        val dx = abs(x1 - x0)
        val dy = abs(y1 - y0)
        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1
        var err = dx - dy

        while (true) {
            points.add(Pair(x0, y0))
            if (x0 == x1 && y0 == y1) break
            val e2 = 2 * err
            if (e2 > -dy) { err -= dy; x0 += sx }
            if (e2 < dx)  { err += dx; y0 += sy }
        }
        return points
    }

    override fun onCleared() {
        super.onCleared()
        webSocket?.close(1000, "ViewModel cleared")
    }
}