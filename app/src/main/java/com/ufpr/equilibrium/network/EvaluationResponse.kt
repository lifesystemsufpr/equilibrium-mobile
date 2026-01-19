package com.ufpr.equilibrium.network

data class PatientDto(
    val id: String?,
    val birthday: String?,
    val cpf: String?,
    val fullName: String?
)

data class EvaluationResponse(
    val id: String?,
    val participantId: String?,
    val healthProfessionalId: String?,
    val healthcareUnitId: String?,
    val date: String?,
    val time_init: String?,
    val time_end: String?,
    val type: String?,
    val participant: PatientDto?
)
