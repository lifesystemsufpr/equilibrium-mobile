package com.ufpr.equilibrium.data.repository

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.Answer
import com.ufpr.equilibrium.domain.model.IVCF20Questions
import com.ufpr.equilibrium.domain.model.Question
import com.ufpr.equilibrium.domain.repository.QuestionnaireRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Implementation of QuestionnaireRepository.
 * Currently uses hardcoded IVCF20 questions and in-memory storage.
 * TODO: Integrate with Room database for answer persistence.
 */
class QuestionnaireRepositoryImpl @Inject constructor(
    // TODO: Inject AnswerDao when Room is integrated
) : QuestionnaireRepository {
    
    // In-memory storage (temporary until Room is integrated)
    private val answersMap = mutableMapOf<String, MutableMap<Int, Answer>>()
    
    override fun loadQuestions(questionnaireId: String): Flow<List<Question>> = flow {
        when (questionnaireId) {
            "ivcf20" -> emit(IVCF20Questions.firstThreeGroupsQuestions)
            else -> emit(emptyList())
        }
    }
    
    override suspend fun saveAnswer(answer: Answer): Result<Unit> {
        return try {
            val questionnaireAnswers = answersMap.getOrPut("ivcf20") { mutableMapOf() }
            questionnaireAnswers[answer.questionId] = answer
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getAnswers(questionnaireId: String): Result<List<Answer>> {
        return try {
            val answers = answersMap[questionnaireId]?.values?.toList() ?: emptyList()
            Result.Success(answers)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun clearAnswers(questionnaireId: String): Result<Unit> {
        return try {
            answersMap[questionnaireId]?.clear()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
