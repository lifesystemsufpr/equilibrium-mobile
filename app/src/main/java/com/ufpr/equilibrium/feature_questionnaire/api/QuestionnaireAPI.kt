package com.ufpr.equilibrium.feature_questionnaire.api

import com.ufpr.equilibrium.feature_questionnaire.payloads.*
import retrofit2.Call
import retrofit2.http.*

/**
 * Interface Retrofit para endpoints de Questionário
 * Base URL: /questionnaires
 */
interface QuestionnaireAPI {
    
    /**
     * Submete as respostas de um questionário
     * POST /questionnaires/response
     */
    @POST("questionnaires/response")
    fun submitQuestionnaireResponse(
        @Body request: QuestionnaireResponseRequest,
        @Header("Authorization") token: String
    ): Call<QuestionnaireSubmitResponse>
    
    /**
     * Busca as respostas de questionários de um participante
     * GET /questionnaires/participant/{participantId}
     */
    @GET("questionnaires/participant/{participantId}")
    fun getQuestionnaireResponsesByParticipant(
        @Path("participantId") participantId: String,
        @Header("Authorization") token: String
    ): Call<List<QuestionnaireResponsesResponse>>
    
    /**
     * Busca a estrutura de um questionário específico (IVCF-20)
     * GET /questionnaires/ivcf-20
     */
    @GET("questionnaires/ivcf-20")
    fun getIVCF20QuestionnaireStructure(
        @Header("Authorization") token: String
    ): Call<QuestionnaireStructureResponse>
    
    /**
     * Busca os detalhes de uma resposta específica
     * GET /questionnaires/response/{responseId}
     */
    @GET("questionnaires/response/{responseId}")
    fun getQuestionnaireResponseDetails(
        @Path("responseId") responseId: String,
        @Header("Authorization") token: String
    ): Call<QuestionnaireDetailResponse>
}
