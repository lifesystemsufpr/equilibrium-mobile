package com.ufpr.equilibrium.domain.usecase.questionnaire

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.Answer
import com.ufpr.equilibrium.domain.model.IVCF20Questions
import com.ufpr.equilibrium.domain.model.Question
import javax.inject.Inject

/**
 * Use case to calculate the score for a specific question group.
 */
class CalculateGroupScoreUseCase @Inject constructor() {
    
    operator fun invoke(
        groupId: String,
        questions: List<Question>,
        answers: List<Answer>
    ): Result<Int> {
        return try {
            val score = calculateGroupScore(groupId, questions, answers)
            Result.Success(score)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    private fun calculateGroupScore(
        groupId: String,
        questions: List<Question>,
        answers: List<Answer>
    ): Int {
        val answersMap = answers.associateBy { it.questionId }
        val groupQuestions = questions.filter { it.groupId == groupId }
        
        // Special rule for AVD group
        if (groupId == IVCF20Questions.GROUP_AVD) {
            var groupTotal = 0
            
            // AVD Instrumental (special - shared maximum 4 points)
            val avdInstAnswers = answersMap.filterKeys { qId ->
                IVCF20Questions.avdInstrumentalQuestionIds.contains(qId)
            }
            groupTotal += if (avdInstAnswers.values.any { it.score > 0 }) 4 else 0
            
            // AVD Basic (banho - up to 6 points)
            val banhoAnswer = answersMap[IVCF20Questions.Q_AVD_BANHO]
            groupTotal += banhoAnswer?.score ?: 0
            
            return groupTotal
        }
        
        // For other groups, sum normally
        return groupQuestions.sumOf { q ->
            answersMap[q.id]?.score ?: 0
        }
    }
}
