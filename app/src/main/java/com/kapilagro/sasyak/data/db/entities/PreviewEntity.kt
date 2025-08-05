package com.kapilagro.sasyak.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "preview")
data class PreviewEntity(
    @PrimaryKey
    val taskType: String,
    val valueName: String,
    val cropName: String,
    val row: String,
    val treeNo: String?=null,
)