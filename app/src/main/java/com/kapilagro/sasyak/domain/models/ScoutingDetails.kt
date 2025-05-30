package com.kapilagro.sasyak.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ScoutingDetails(
    val scoutingDate: String,
    val cropName: String,
    val row: Int,
    val treeNo: Int,
    val noOfFruitSeen: String?,
    val noOfFlowersSeen: String?,
    val noOfFruitsDropped: String?,
    val nameOfDisease: String?,
    //val uploadedFiles: List<String>? = null
)