package br.com.sd.pixelhubandroid.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sd.pixelhubandroid.data.data.DrawMessage
import br.com.sd.pixelhubandroid.data.data.Line
import br.com.sd.pixelhubandroid.data.data.PointData
import br.com.sd.pixelhubandroid.data.state.BoardUiState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString

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
            .url("ws://10.0.2.2:8000/ws")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _uiState.value = _uiState.value.copy(isConnected = true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = gson.fromJson(text, DrawMessage::class.java)
                    if (message.type == "draw") {
                        val newLine = Line(
                            start = message.start,
                            end = message.end,
                            color = Color(android.graphics.Color.parseColor(message.color)),
                            strokeWidth = message.lineWidth
                        )
                        viewModelScope.launch {
                            _uiState.value = _uiState.value.copy(
                                lines = _uiState.value.lines + newLine,
                                activeUsers = _uiState.value.activeUsers + message.user
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _uiState.value = _uiState.value.copy(isConnected = false)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _uiState.value = _uiState.value.copy(isConnected = false)
            }
        })
    }

    fun sendDrawAction(start: PointData, end: PointData, color: Color, width: Float) {
        val colorHex = String.format("#%06X", (0xFFFFFF and color.toArgb()))
        val message = DrawMessage(
            type = "draw",
            user = currentUser,
            start = start,
            end = end,
            color = colorHex,
            lineWidth = width
        )
        
        val newLine = Line(start, end, color, width)
        _uiState.value = _uiState.value.copy(
            lines = _uiState.value.lines + newLine
        )

        webSocket?.send(gson.toJson(message))
    }

    override fun onCleared() {
        super.onCleared()
        webSocket?.close(1000, "ViewModel cleared")
    }
}
