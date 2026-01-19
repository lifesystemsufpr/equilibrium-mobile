package com.ufpr.equilibrium.feature_questionnaire.examples

import com.ufpr.equilibrium.feature_questionnaire.mappers.QuestionnaireMapper
import com.ufpr.equilibrium.feature_questionnaire.payloads.AnswerRequest
import com.ufpr.equilibrium.feature_questionnaire.payloads.QuestionnaireResponseRequest
import com.ufpr.equilibrium.feature_questionnaire.repository.QuestionnaireRepository

/**
 * Exemplos de uso dos payloads de questionário
 * 
 * IMPORTANTE: Esta classe é apenas para referência e documentação.
 * Não deve ser usada diretamente em produção.
 */
object QuestionnairePayloadExamples {
    
    /**
     * Exemplo 1: Carregar estrutura do questionário IVCF-20 da API
     */
    fun exampleLoadQuestionnaireStructure(
        repository: QuestionnaireRepository,
        token: String
    ) {
        repository.getIVCF20QuestionnaireStructure(
            token = token,
            onSuccess = { structure ->
                // Converter para modelos locais
                val localQuestions = QuestionnaireMapper.mapToLocalQuestions(structure)
                
                // Criar mapeamento IDs locais -> IDs da API
                val idMapping = QuestionnaireMapper.createIdMapping(structure)
                
                println("✅ Questionário carregado: ${structure.name}")
                println("   Total de questões: ${localQuestions.size}")
                println("   ID do questionário: ${structure.id}")
                
                // Armazenar idMapping para uso posterior ao enviar respostas
            },
            onError = { error ->
                println("❌ Erro ao carregar questionário: $error")
            }
        )
    }
    
    /**
     * Exemplo 2: Submeter respostas do questionário
     */
    fun exampleSubmitQuestionnaireResponses(
        repository: QuestionnaireRepository,
        token: String,
        participantId: String,
        healthProfessionalId: String,
        questionnaireId: String,
        answersMap: Map<String, String> // questionId da API -> optionId da API
    ) {
        val request = QuestionnaireResponseRequest(
            participantId = participantId,
            healthProfessionalId = healthProfessionalId,
            questionnaireId = questionnaireId,
            answers = answersMap.map { (qId, oId) ->
                AnswerRequest(
                    questionId = qId,
                    selectedOptionId = oId
                )
            }
        )
        
        repository.submitQuestionnaireResponse(
            request = request,
            token = token,
            onSuccess = { response ->
                println("✅ Respostas enviadas com sucesso!")
                println("   ID da submissão: ${response.id}")
                println("   Pontuação total: ${response.totalScore ?: "Não calculada"}")
                println("   Data: ${response.createdAt}")
                
                response.message?.let { msg ->
                    println("   Mensagem: $msg")
                }
            },
            onError = { error ->
                println("❌ Erro ao enviar respostas: $error")
            }
        )
    }
    
    /**
     * Exemplo 3: Buscar histórico de respostas de um participante
     */
    fun exampleGetParticipantHistory(
        repository: QuestionnaireRepository,
        token: String,
        participantId: String
    ) {
        repository.getQuestionnaireResponsesByParticipant(
            participantId = participantId,
            token = token,
            onSuccess = { responses ->
                println("✅ Histórico carregado: ${responses.size} respostas encontradas")
                
                responses.forEachIndexed { index, response ->
                    println("\n📋 Resposta ${index + 1}:")
                    println("   ID: ${response.id}")
                    println("   Questionário: ${response.questionnaireName ?: "N/A"}")
                    println("   Data: ${response.createdAt}")
                    println("   Pontuação: ${response.totalScore ?: "N/A"}")
                    println("   Total de respostas: ${response.answers.size}")
                }
            },
            onError = { error ->
                println("❌ Erro ao buscar histórico: $error")
            }
        )
    }
    
    /**
     * Exemplo 4: Buscar detalhes de uma resposta específica
     */
    fun exampleGetResponseDetails(
        repository: QuestionnaireRepository,
        token: String,
        responseId: String
    ) {
        repository.getQuestionnaireResponseDetails(
            responseId = responseId,
            token = token,
            onSuccess = { details ->
                println("✅ Detalhes da resposta carregados")
                println("\n📊 Informações Gerais:")
                println("   ID: ${details.id}")
                println("   Data: ${details.createdAt}")
                println("   Pontuação total: ${details.totalScore ?: "N/A"}")
                
                details.participant?.let { participant ->
                    println("\n👤 Participante:")
                    println("   Nome: ${participant.fullName}")
                    println("   CPF: ${participant.cpf}")
                }
                
                details.healthProfessional?.let { professional ->
                    println("\n👨‍⚕️ Profissional:")
                    println("   Nome: ${professional.fullName}")
                    println("   Especialidade: ${professional.speciality}")
                }
                
                details.questionnaire?.let { questionnaire ->
                    println("\n📝 Questionário:")
                    println("   Nome: ${questionnaire.name}")
                    println("   Descrição: ${questionnaire.description ?: "N/A"}")
                }
                
                println("\n✏️ Respostas (${details.answers.size}):")
                details.answers.forEach { answer ->
                    println("   • ${answer.questionText}")
                    println("     Resposta: ${answer.selectedOptionText}")
                    println("     Pontuação: ${answer.score ?: "N/A"}")
                }
            },
            onError = { error ->
                println("❌ Erro ao buscar detalhes: $error")
            }
        )
    }
    
    /**
     * Exemplo 5: Fluxo completo - Carregar, Responder e Enviar
     */
    fun exampleCompleteFlow(
        repository: QuestionnaireRepository,
        token: String,
        participantId: String,
        healthProfessionalId: String
    ) {
        println("🚀 Iniciando fluxo completo do questionário...\n")
        
        // Passo 1: Carregar estrutura do questionário
        repository.getIVCF20QuestionnaireStructure(
            token = token,
            onSuccess = { structure ->
                println("✅ Passo 1: Estrutura carregada")
                val questionnaireId = structure.id
                
                // Criar mapeamento de IDs
                val idMapping = QuestionnaireMapper.createIdMapping(structure)
                
                // Passo 2: Simular respostas do usuário
                // (em produção, isso viria da UI)
                val localAnswers = mapOf(
                    0 to "09086cb8-0f47-4a15-9f2f-0f953dd6d1e2",  // Questão 0 -> Opção X
                    1 to "4fc15a8a-1c4c-45f9-9b9a-b360cd69d93c"   // Questão 1 -> Opção Y
                )
                
                // Passo 3: Converter IDs locais para IDs da API
                val apiAnswers = localAnswers.mapKeys { (localId, _) ->
                    idMapping[localId] ?: throw IllegalStateException("ID local não encontrado")
                }
                
                println("✅ Passo 2: Respostas coletadas (${apiAnswers.size} respostas)")
                
                // Passo 4: Enviar para o servidor
                val request = repository.createSubmissionRequest(
                    participantId = participantId,
                    healthProfessionalId = healthProfessionalId,
                    questionnaireId = questionnaireId,
                    answersMap = apiAnswers
                )
                
                repository.submitQuestionnaireResponse(
                    request = request,
                    token = token,
                    onSuccess = { response ->
                        println("✅ Passo 3: Respostas enviadas!")
                        println("   ID da submissão: ${response.id}")
                        println("   Pontuação: ${response.totalScore}")
                        println("\n🎉 Fluxo completo finalizado com sucesso!")
                    },
                    onError = { error ->
                        println("❌ Passo 3 falhou: $error")
                    }
                )
            },
            onError = { error ->
                println("❌ Passo 1 falhou: $error")
            }
        )
    }
    
    /**
     * Exemplo 6: Criar request manualmente
     */
    fun exampleCreateManualRequest(): QuestionnaireResponseRequest {
        return QuestionnaireResponseRequest(
            participantId = "0a775d40-65c3-4514-ad1e-d31f023a2191",
            healthProfessionalId = "18b0b378-1060-42d0-8d82-4a11ba7d2cee",
            questionnaireId = "9825800d-6ec8-4220-ad50-eeb10a84c337",
            answers = listOf(
                AnswerRequest(
                    questionId = "512c1ba0-b3d3-434b-afe5-e3d9f8b344b8",
                    selectedOptionId = "09086cb8-0f47-4a15-9f2f-0f953dd6d1e2"
                ),
                AnswerRequest(
                    questionId = "1a294ad8-0b70-4669-97e2-f8366a60341d",
                    selectedOptionId = "4fc15a8a-1c4c-45f9-9b9a-b360cd69d93c"
                )
            )
        )
    }
}
