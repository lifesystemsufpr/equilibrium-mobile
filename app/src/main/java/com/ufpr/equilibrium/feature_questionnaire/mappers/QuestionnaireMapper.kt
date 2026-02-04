package com.ufpr.equilibrium.feature_questionnaire.mappers

import com.ufpr.equilibrium.feature_questionnaire.Option
import com.ufpr.equilibrium.feature_questionnaire.Question
import com.ufpr.equilibrium.feature_questionnaire.payloads.GroupDto
import com.ufpr.equilibrium.feature_questionnaire.payloads.OptionDto
import com.ufpr.equilibrium.feature_questionnaire.payloads.QuestionDto
import com.ufpr.equilibrium.feature_questionnaire.payloads.QuestionnaireStructureResponse
import com.ufpr.equilibrium.feature_questionnaire.payloads.SubGroupDto

/**
 * Mappers para converter entre DTOs da API e modelos locais do questionário
 * 
 * A API retorna uma estrutura aninhada:
 * - QuestionnaireStructureResponse
 *   - groups[]
 *     - questions[]
 *     - subGroups[]
 *       - questions[]
 * 
 * Este mapper achata essa estrutura em uma lista simples de Questions,
 * preservando as informações de grupo (groupId, groupName).
 */
object QuestionnaireMapper {
    
    /**
     * Converte a resposta da API para lista de Questions locais
     * Achata a estrutura aninhada de groups/subGroups em uma lista plana
     */
    fun mapToLocalQuestions(response: QuestionnaireStructureResponse): List<Question> {
        val questions = mutableListOf<Question>()
        
        response.groups?.forEach { group ->
            // Processar questões diretas do grupo
            group.questions?.forEach { questionDto ->
                questions.add(
                    mapQuestionDtoToLocal(
                        dto = questionDto,
                        groupId = group.id,
                        groupName = group.title
                    )
                )
            }
            
            // Processar questões dos subgrupos
            group.subGroups?.forEach { subGroup ->
                subGroup.questions?.forEach { questionDto ->
                    questions.add(
                        mapQuestionDtoToLocal(
                            dto = questionDto,
                            groupId = group.id,
                            groupName = "${group.title} - ${subGroup.title}"
                        )
                    )
                }
            }
        }
        
        // Ordenar por order
        return questions.sortedBy { it.order }
    }
    
    /**
     * Converte um QuestionDto para Question local
     * Usa o UUID da API diretamente como ID
     */
    private fun mapQuestionDtoToLocal(
        dto: QuestionDto,
        groupId: String,
        groupName: String
    ): Question {
        return Question(
            id = dto.id,  // UUID da API
            text = dto.statement,  // API usa 'statement'
            options = dto.options?.sortedBy { it.order }?.map { mapOptionDtoToLocal(it) } ?: emptyList(),
            allowNote = false,
            groupId = groupId,
            groupName = groupName,
            specialScoring = isSpecialScoringGroup(groupId),
            order = dto.order
        )
    }
    
    /**
     * Converte um OptionDto para Option local
     * Inclui o UUID da API
     */
    private fun mapOptionDtoToLocal(dto: OptionDto): Option {
        return Option(
            id = dto.id,  // UUID da API
            text = dto.label,  // API usa 'label'
            score = dto.score
        )
    }
    
    /**
     * Verifica se um grupo tem pontuação especial
     * (por exemplo, AVD Instrumental tem regra especial de 4 pontos máximos para 3 questões)
     */
    private fun isSpecialScoringGroup(groupId: String?): Boolean {
        // Por enquanto, retorna false - pode ser ajustado depois se necessário
        return false
    }
}
