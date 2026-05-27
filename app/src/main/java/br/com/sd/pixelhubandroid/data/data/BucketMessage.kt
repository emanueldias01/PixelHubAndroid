package br.com.sd.pixelhubandroid.data.data

data class BucketMessage(
    val type: String = "bucket",
    val user: String,
    val start: PointData,
    val color: String
)
