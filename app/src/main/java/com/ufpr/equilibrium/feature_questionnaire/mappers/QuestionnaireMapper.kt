package com.ufpr.equilibrium.feature_questionnaire.mappers

import com.ufpr.equilibrium.feature_questionnaire.Option
import com.ufpr.equilibrium.feature_questionnaire.Question
import com.ufpr.equilibrium.feature_questionnaire.payloads.OptionDto
import com.ufpr.equilibrium.feature_questionnaire.payloads.QuestionDto
import com.ufpr.equilibrium.feature_questionnaire.payloads.QuestionnaireStructureResponse

/**
 * Mappers para converter entre DTOs da API e modelos locais do questionário
 */
object QuestionnaireMapper {
    
    /**
     * Converte a resposta da API para lista de Questions locais
     */
    fun mapToLocalQuestions(response: QuestionnaireStructureResponse): List<Question> {
        return response.questions
            ?.sortedBy { it.order }
            ?.map { questionDto ->
                mapQuestionDtoToLocal(questionDto)
            } ?: emptyList()
    }
    
    /**
     * Converte um QuestionDto para Question local
     * Usa o UUID da API diretamente como ID
     */
    fun mapQuestionDtoToLocal(dto: QuestionDto): Question {
        return Question(
            id = dto.id,  // UUID da API
            text = dto.text,
            options = dto.options?.map { mapOptionDtoToLocal(it) } ?: emptyList(),
            allowNote = false,
            groupId = dto.groupId,
            groupName = dto.groupName,
            specialScoring = isSpecialScoringGroup(dto.groupId)
        )
    }
    
    /**
     * Converte um OptionDto para Option local
     * Inclui o UUID da API
     */
    fun mapOptionDtoToLocal(dto: OptionDto): Option {
        return Option(
            id = dto.id,  // UUID da API
            text = dto.text,
            score = dto.score
        )
    }
    
    /**
     * Verifica se um grupo tem pontuação especial
     * (por exemplo, AVD Instrumental tem regra especial de 4 pontos máximos para 3 questões)
     */
    private fun isSpecialScoringGroup(groupId: String?): Boolean {
        return groupId == "avd_instrumental"
    }
    
    /**
     * Cria um mapa de IDs da API para IDs locais
     * Útil para mapear respostas
     */
    fun createIdMapping(response: QuestionnaireStructureResponse): Map<Int, String> {
        return response.questions
            ?.sortedBy { it.order }
            ?.mapIndexed { index, questionDto -> 
                index to questionDto.id 
            }
            ?.toMap() ?: emptyMap()
    }
    
    /**
     * Cria um mapa reverso: IDs da API para IDs locais
     */
    fun createReverseIdMapping(response: QuestionnaireStructureResponse): Map<String, Int> {
        return response.questions
            ?.sortedBy { it.order }
            ?.mapIndexed { index, questionDto -> 
                questionDto.id to index 
            }
            ?.toMap() ?: emptyMap()
    }
    
    /**
     * Mapeia opções da API para IDs locais (índices)
     */
    fun createOptionMapping(dto: QuestionDto): Map<String, Int> {
        return dto.options?.mapIndexed { index, optionDto ->
            optionDto.id to index
        }?.toMap() ?: emptyMap()
    }
}
