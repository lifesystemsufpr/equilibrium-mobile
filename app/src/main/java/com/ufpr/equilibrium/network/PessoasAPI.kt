package com.ufpr.equilibrium.network

import com.ufpr.equilibrium.feature_healthUnit.HealthUnit
import com.ufpr.equilibrium.feature_healthUnit.HealthUnitEnvelope
import com.ufpr.equilibrium.feature_professional.PacienteModel
import com.ufpr.equilibrium.feature_professional.PacientesEnvelope
import com.ufpr.equilibrium.feature_professional.ProfessionalModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

import retrofit2.http.*

/**
 * API interface for Pessoas endpoints.
 * Note: Method names use "Participant" but model classes remain unchanged for compatibility.
 */
interface PessoasAPI {

    // Lookup person by CPF
    @GET("lookup")
    fun getPessoaByCpf(
        @Query("cpf") cpf: String
    ): Call<Usuario>

    // legacy login removed; use data module AuthRepositoryImpl

    // GET /participant -> envelope with data + meta
    @GET("participant")
    fun getParticipants(
        // optional: backend pagination support
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        // optional: search by CPF
        @Query("cpf") cpf: String? = null
    ): Call<PacientesEnvelope>

    // POST /participant - accepts plain JSON (without "user" wrapper)
    @POST("participant")
    fun postParticipant(
        @Body request: PacienteModel
    ): Call<PacienteModel>

    // POST /evaluation - submit test/evaluation data
    @POST("evaluation")
    fun postTestes(
        @Body request: Teste,
        @Header("Authorization") token: String
    ): Call<Teste>

    // GET /evaluation - get evaluations for a participant
    @GET("evaluation")
    fun getEvaluations(
        @Query("participantId") participantId: String
    ): Call<EvaluationsEnvelope>

    // POST /healthProfessional - register health professional
    @POST("healthProfessional")
    fun postProfessional(
        @Body request: ProfessionalModel,
    ): Call<ProfessionalModel>

    // GET /health-unit - get list of health units
    @GET("health-unit")
    fun getHealthUnit(): Call<HealthUnitEnvelope>
}
