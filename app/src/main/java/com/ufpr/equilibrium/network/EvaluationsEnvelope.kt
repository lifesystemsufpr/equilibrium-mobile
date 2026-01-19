package com.ufpr.equilibrium.network

data class Meta(
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 0,
    val lastPage: Int = 1
)

data class EvaluationsEnvelope(
    val data: List<EvaluationResponse> = emptyList(),
    val meta: Meta? = null
)
