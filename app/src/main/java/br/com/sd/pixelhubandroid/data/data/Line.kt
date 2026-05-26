package br.com.sd.pixelhubandroid.data.data

import androidx.compose.ui.graphics.Color

data class Line(
    val start: PointData,
    val end: PointData,
    val color: Color,
    val strokeWidth: Float
)