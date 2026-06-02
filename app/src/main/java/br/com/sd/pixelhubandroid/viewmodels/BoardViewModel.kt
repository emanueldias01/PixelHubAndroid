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
import kotlin.math.roundToInt

class BoardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BoardUiState())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private var currentUser: String = ""

    fun connect(username: String) {
        currentUser = username
        val request = Request.Builder()
            .url("ws://10.0.2.2:8080/ws")
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
                            val newLine = Line(
                                start = message.start,
                                end = message.end,
                                color = Color(android.graphics.Color.parseColor(message.color)),
                                strokeWidth = message.lineWidth
                            )
                            _uiState.update { state ->
                                state.copy(
                                    lines = state.lines + newLine,
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
        
        val newLine = Line(startInt, endInt, color, width)
        _uiState.update { it.copy(lines = it.lines + newLine) }

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

    override fun onCleared() {
        super.onCleared()
        webSocket?.close(1000, "ViewModel cleared")
    }
}
