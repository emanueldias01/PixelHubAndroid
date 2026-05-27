package br.com.sd.pixelhubandroid.data.data

data class BoardMessage(
    val type: String = "board",
    val width: Int,
    val height: Int,
    val art: List<String>
)
