package com.ufpr.equilibrium.domain.usecase.questionnaire

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.Answer
import com.ufpr.equilibrium.domain.model.Question
import javax.inject.Inject

/**
 * Use case to validate if all questions have been answered.
 */
class ValidateCompletionUseCase @Inject constructor() {
    
    operator fun invoke(questions: List<Question>, answers: List<Answer>): Result<Boolean> {
        return try {
            val answersMap = answers.associateBy { it.questionId }
            val allAnswered = questions.all { question ->
                answersMap[question.id]?.selectedOptionIndex?.let { it >= 0 } == true
            }
            Result.Success(allAnswered)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
