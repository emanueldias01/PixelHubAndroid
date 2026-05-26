package br.com.sd.pixelhubandroid.data.state

import br.com.sd.pixelhubandroid.data.data.Line

data class BoardUiState(
    val lines: List<Line> = emptyList(),
    val activeUsers: Set<String> = emptySet(),
    val isConnected: Boolean = false
)