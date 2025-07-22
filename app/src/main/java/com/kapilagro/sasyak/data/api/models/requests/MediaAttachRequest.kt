package com.kapilagro.sasyak.data.api.models.requests

data class MediaAttachRequest(
 val task_id: Int,
 val media : List<String>
)