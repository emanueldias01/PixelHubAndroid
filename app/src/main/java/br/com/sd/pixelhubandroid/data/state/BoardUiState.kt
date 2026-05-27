package br.com.sd.pixelhubandroid.data.state

import androidx.compose.ui.graphics.Color
import br.com.sd.pixelhubandroid.data.data.Line

enum class DrawingTool {
    PENCIL, BUCKET, ERASER, PAN
}

data class BoardUiState(
    val lines: List<Line> = emptyList(),
    val activeUsers: Set<String> = emptySet(),
    val isConnected: Boolean = false,
    val selectedTool: DrawingTool = DrawingTool.PENCIL,
    val selectedColor: Color = Color.Black,
    val boardWidth: Int = 1000,
    val boardHeight: Int = 1000,
    val boardPixels: IntArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BoardUiState) return false

        if (lines != other.lines) return false
        if (activeUsers != other.activeUsers) return false
        if (isConnected != other.isConnected) return false
        if (selectedTool != other.selectedTool) return false
        if (selectedColor != other.selectedColor) return false
        if (boardWidth != other.boardWidth) return false
        if (boardHeight != other.boardHeight) return false
        if (boardPixels != null) {
            if (other.boardPixels == null) return false
            if (!boardPixels.contentEquals(other.boardPixels)) return false
        } else if (other.boardPixels != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lines.hashCode()
        result = 31 * result + activeUsers.hashCode()
        result = 31 * result + isConnected.hashCode()
        result = 31 * result + selectedTool.hashCode()
        result = 31 * result + selectedColor.hashCode()
        result = 31 * result + boardWidth
        result = 31 * result + boardHeight
        result = 31 * result + (boardPixels?.contentHashCode() ?: 0)
        return result
    }
}
