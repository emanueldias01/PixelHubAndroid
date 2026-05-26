package br.com.sd.pixelhubandroid.data.data

data class DrawMessage(
    val type: String = "draw",
    val user: String,
    val start: PointData,
    val end: PointData,
    val color: String,
    val lineWidth: Float
)
