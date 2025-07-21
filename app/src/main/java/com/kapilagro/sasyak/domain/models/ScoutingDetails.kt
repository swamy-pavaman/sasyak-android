package com.kapilagro.sasyak.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ScoutingDetails(
    val scoutingDate: String,
    val cropName: String,
    val row: String,
    val treeNo: String,
    val noOfFruitSeen: String?,
    val noOfFlowersSeen: String?,
    val noOfFruitsDropped: String?,
    val targetPest: String?,
    val valveName: String,
    val dueDate:String?,
    //val uploadedFiles: List<String>? = null
)