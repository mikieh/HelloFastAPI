package com.callarchive.app.domain.model

data class TranscriptionResult(
    val transcript: String,
    val keywords: List<String>,
    val summary: String
)
