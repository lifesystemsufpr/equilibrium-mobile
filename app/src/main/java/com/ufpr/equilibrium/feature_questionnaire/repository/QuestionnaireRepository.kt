package com.ufpr.equilibrium.feature_questionnaire.repository

import com.ufpr.equilibrium.feature_questionnaire.api.QuestionnaireAPI
import com.ufpr.equilibrium.feature_questionnaire.payloads.*
import com.ufpr.equilibrium.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositório para gerenciar as chamadas de API relacionadas a questionários
 */
@Singleton
class QuestionnaireRepository @Inject constructor() {
    
    private val api: QuestionnaireAPI = RetrofitClient.instanceQuestionnaireAPI
    
    /**
     * Submete as respostas de um questionário
     */
    fun submitQuestionnaireResponse(
        request: QuestionnaireResponseRequest,
        token: String,
        onSuccess: (QuestionnaireSubmitResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("QuestionnaireRepo", "=== SUBMIT REQUEST ===")
        android.util.Log.d("QuestionnaireRepo", "Token: ${token.take(50)}...")
        android.util.Log.d("QuestionnaireRepo", "ParticipantId: ${request.participantId}")
        android.util.Log.d("QuestionnaireRepo", "ProfessionalId: ${request.healthProfessionalId}")
        android.util.Log.d("QuestionnaireRepo", "QuestionnaireId: ${request.questionnaireId}")
        android.util.Log.d("QuestionnaireRepo", "Answers count: ${request.answers.size}")
        
        val call = api.submitQuestionnaireResponse(request, "Bearer $token")
        
        // Log COMPLETO da requisição
        val actualRequest = call.request()
        val url = actualRequest.url.toString()
        android.util.Log.d("QuestionnaireRepo", "=== REQUEST DETAILS ===")
        android.util.Log.d("QuestionnaireRepo", "Full URL: $url")
        android.util.Log.d("QuestionnaireRepo", "Method: ${actualRequest.method}")
        android.util.Log.d("QuestionnaireRepo", "Headers:")
        actualRequest.headers.forEach { (name, value) ->
            android.util.Log.d("QuestionnaireRepo", "  $name: ${if (name == "Authorization") value.take(50) + "..." else value}")
        }
        
        // Log do body
        try {
            val buffer = okio.Buffer()
            actualRequest.body?.writeTo(buffer)
            val bodyString = buffer.readUtf8()
            android.util.Log.d("QuestionnaireRepo", "Body (first 500 chars): ${bodyString.take(500)}")
        } catch (e: Exception) {
            android.util.Log.e("QuestionnaireRepo", "Error reading body", e)
        }
        
        call.enqueue(object : Callback<QuestionnaireSubmitResponse> {
                override fun onResponse(
                    call: Call<QuestionnaireSubmitResponse>,
                    response: Response<QuestionnaireSubmitResponse>
                ) {
                    android.util.Log.d("QuestionnaireRepo", "Response code: ${response.code()}")
                    android.util.Log.d("QuestionnaireRepo", "Response headers: ${response.headers()}")
                    
                    if (response.isSuccessful && response.body() != null) {
                        android.util.Log.d("QuestionnaireRepo", "Success! Response: ${response.body()}")
                        onSuccess(response.body()!!)
                    } else {
                        // Capturar resposta bruta para debug
                        val rawResponse = try {
                            response.errorBody()?.string() ?: response.raw().body?.string() ?: "Empty body"
                        } catch (e: Exception) {
                            "Error reading body: ${e.message}"
                        }
                        
                        android.util.Log.e("QuestionnaireRepo", "Error ${response.code()}")
                        android.util.Log.e("QuestionnaireRepo", "Raw response (first 500 chars): ${rawResponse.take(500)}")
                        android.util.Log.e("QuestionnaireRepo", "Content-Type: ${response.headers()["Content-Type"]}")
                        
                        onError("Erro ao enviar respostas: ${response.code()} - ${response.message()}\nResponse: ${rawResponse.take(200)}")
                    }
                }
                
                override fun onFailure(call: Call<QuestionnaireSubmitResponse>, t: Throwable) {
                    android.util.Log.e("QuestionnaireRepo", "Request failed", t)
                    android.util.Log.e("QuestionnaireRepo", "Error type: ${t.javaClass.simpleName}")
                    android.util.Log.e("QuestionnaireRepo", "Error message: ${t.message}")
                    
                    // Se for erro de JSON, tentar ler resposta bruta
                    if (t is com.google.gson.JsonSyntaxException || t is com.google.gson.stream.MalformedJsonException) {
                        android.util.Log.e("QuestionnaireRepo", "JSON parsing error - server returned non-JSON response")
                    }
                    
                    onError("Falha na conexão: ${t.message}")
                }
            })
    }
    
    /**
     * Busca as respostas de questionários de um participante
     */
    fun getQuestionnaireResponsesByParticipant(
        participantId: String,
        token: String,
        onSuccess: (List<QuestionnaireResponsesResponse>) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getQuestionnaireResponsesByParticipant(participantId, "Bearer $token")
            .enqueue(object : Callback<List<QuestionnaireResponsesResponse>> {
                override fun onResponse(
                    call: Call<List<QuestionnaireResponsesResponse>>,
                    response: Response<List<QuestionnaireResponsesResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        onSuccess(response.body()!!)
                    } else {
                        onError("Erro ao buscar respostas: ${response.code()} - ${response.message()}")
                    }
                }
                
                override fun onFailure(call: Call<List<QuestionnaireResponsesResponse>>, t: Throwable) {
                    onError("Falha na conexão: ${t.message}")
                }
            })
    }
    
    /**
     * Busca a estrutura do questionário IVCF-20
     */
    fun getIVCF20QuestionnaireStructure(
        token: String,
        onSuccess: (QuestionnaireStructureResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getIVCF20QuestionnaireStructure("Bearer $token")
            .enqueue(object : Callback<QuestionnaireStructureResponse> {
                override fun onResponse(
                    call: Call<QuestionnaireStructureResponse>,
                    response: Response<QuestionnaireStructureResponse>
                ) {
                    android.util.Log.d("QuestionnaireRepo", "Response code: ${response.code()}")
                    android.util.Log.d("QuestionnaireRepo", "Response successful: ${response.isSuccessful}")
                    android.util.Log.d("QuestionnaireRepo", "Response body null: ${response.body() == null}")
                    
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        android.util.Log.d("QuestionnaireRepo", "Structure ID: ${body.id}")
                        android.util.Log.d("QuestionnaireRepo", "Structure title: ${body.title}")
                        android.util.Log.d("QuestionnaireRepo", "Groups null: ${body.groups == null}")
                        android.util.Log.d("QuestionnaireRepo", "Groups size: ${body.groups?.size ?: 0}")
                        
                        onSuccess(body)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("QuestionnaireRepo", "Error response: $errorBody")
                        onError("Erro ao buscar estrutura: ${response.code()} - ${response.message()}")
                    }
                }
                
                override fun onFailure(call: Call<QuestionnaireStructureResponse>, t: Throwable) {
                    android.util.Log.e("QuestionnaireRepo", "Request failed", t)
                    onError("Falha na conexão: ${t.message}")
                }
            })
    }
    
    /**
     * Busca os detalhes de uma resposta específica
     */
    fun getQuestionnaireResponseDetails(
        responseId: String,
        token: String,
        onSuccess: (QuestionnaireDetailResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        api.getQuestionnaireResponseDetails(responseId, "Bearer $token")
            .enqueue(object : Callback<QuestionnaireDetailResponse> {
                override fun onResponse(
                    call: Call<QuestionnaireDetailResponse>,
                    response: Response<QuestionnaireDetailResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        onSuccess(response.body()!!)
                    } else {
                        onError("Erro ao buscar detalhes: ${response.code()} - ${response.message()}")
                    }
                }
                
                override fun onFailure(call: Call<QuestionnaireDetailResponse>, t: Throwable) {
                    onError("Falha na conexão: ${t.message}")
                }
            })
    }
    
    /**
     * Função auxiliar para converter respostas locais em formato de API
     */
    fun createSubmissionRequest(
        participantId: String,
        healthProfessionalId: String,
        questionnaireId: String,
        answersMap: Map<String, String>
    ): QuestionnaireResponseRequest {
        val answers = answersMap.map { (questionId, optionId) ->
            AnswerRequest(
                questionId = questionId,
                selectedOptionId = optionId
            )
        }
        
        return QuestionnaireResponseRequest(
            participantId = participantId,
            healthProfessionalId = healthProfessionalId,
            questionnaireId = questionnaireId,
            answers = answers
        )
    }
}
