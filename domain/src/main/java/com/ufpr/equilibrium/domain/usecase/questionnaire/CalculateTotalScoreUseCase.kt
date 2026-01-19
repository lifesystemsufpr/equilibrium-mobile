package com.ufpr.equilibrium.domain.usecase.questionnaire

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.Answer
import com.ufpr.equilibrium.domain.model.IVCF20Questions
import javax.inject.Inject

/**
 * Use case to calculate the total score of a questionnaire.
 * 
 * CRITICAL: This implements the special AVD Instrumental rule where 3 questions
 * share a maximum of 4 points (not 4+4+4 = 12).
 */
class CalculateTotalScoreUseCase @Inject constructor() {
    
    operator fun invoke(answers: List<Answer>): Result<Int> {
        return try {
            val totalScore = calculateTotalScoreWithSpecialRules(answers)
            Result.Success(totalScore)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    private fun calculateTotalScoreWithSpecialRules(answers: List<Answer>): Int {
        val answersMap = answers.associateBy { it.questionId }
        var total = 0
        
        // Calculate score for normal questions
        val normalQuestions = answersMap.filterKeys { questionId ->
            !IVCF20Questions.avdInstrumentalQuestionIds.contains(questionId)
        }
        total += normalQuestions.values.sumOf { it.score }
        
        // Calculate special score for AVD Instrumental (maximum 4 points)
        // If any of the 3 Instrumental AVD questions was answered with "Yes" (score = 4),
        // the score is 4. Does NOT sum 4+4+4.
        val avdInstrumentalAnswers = answersMap.filterKeys { questionId ->
            IVCF20Questions.avdInstrumentalQuestionIds.contains(questionId)
        }
        
        val avdInstrumentalScore = if (avdInstrumentalAnswers.values.any { it.score > 0 }) {
            4
        } else {
            0
        }
        total += avdInstrumentalScore
        
        return total
    }
}
