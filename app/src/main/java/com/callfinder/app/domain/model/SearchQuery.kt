package com.callfinder.app.domain.model

data class SearchQuery(
    val phoneNumber: String? = null,
    val contactName: String? = null,
    val dateFrom: Long? = null,
    val dateTo: Long? = null,
    val minDurationSeconds: Int? = null,
    val callType: CallType? = null,
    val keywords: List<String> = emptyList(),
    val textQuery: String? = null
)
