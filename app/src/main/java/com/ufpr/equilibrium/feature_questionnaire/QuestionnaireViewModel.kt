package com.ufpr.equilibrium.feature_questionnaire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Questionnaire screen.
 * Simplified version - logic implemented directly
 */
@HiltViewModel
class QuestionnaireViewModel @Inject constructor(
    private val questionnaireRepository: com.ufpr.equilibrium.feature_questionnaire.repository.QuestionnaireRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionnaireUiState())
    val uiState: StateFlow<QuestionnaireUiState> = _uiState.asStateFlow()
    
    // In-memory collection of answers (key = questionId UUID)
    private val answersMap = mutableMapOf<String, Answer>()

    /**
     * Load questions for a specific questionnaire.
     * Fetches from API using authenticated token.
     */
    fun loadQuestions(questionnaireId: String = "ivcf20") {
        viewModelScope.launch {
            // Set loading state
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Get authentication token from SessionManager
            val token = com.ufpr.equilibrium.utils.SessionManager.token
            
            if (token == null) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Erro: Usuário não autenticado. Faça login novamente."
                    ) 
                }
                return@launch
            }
            
            // Fetch questionnaire structure from API
            questionnaireRepository.getIVCF20QuestionnaireStructure(
                token = token,
                onSuccess = { response ->
                    // Convert API DTOs to local Question models using mapper
                    val questions = com.ufpr.equilibrium.feature_questionnaire.mappers.QuestionnaireMapper
                        .mapToLocalQuestions(response)
                    
                    // Capture the real questionnaire ID from API response
                    val questionnaireId = response.id
                    
                    android.util.Log.d("QuestionnaireVM", "Successfully loaded ${questions.size} questions from API")
                    android.util.Log.d("QuestionnaireVM", "Questionnaire ID: $questionnaireId")
                    
                    _uiState.update { 
                        it.copy(
                            questions = questions,
                            questionnaireId = questionnaireId,  // Store the real ID from API
                            isLoading = false,
                            error = null
                        ) 
                    }
                },
                onError = { error ->
                    android.util.Log.e("QuestionnaireVM", "Failed to load questions: $error")
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Erro ao carregar questionário: $error"
                        ) 
                    }
                }
            )
        }
    }

    /**
     * Handle answer change from the UI.
     */
    fun onAnswerChanged(questionId: String, selectedIndex: Int, score: Int, note: String? = null) {
        val answer = Answer(
            selectedOptionIndex = selectedIndex,
            score = score,
            note = note
        )
        answersMap[questionId] = answer
        
        viewModelScope.launch {
            // Update scores
            updateScores()
        }
    }

    /**
     * Calculate and update scores.
     */
    private fun updateScores() {
        val answers = answersMap.values.toList()
        
        // Calculate total score
        val totalScore = calculateTotalScore(answers)
        _uiState.update { it.copy(totalScore = totalScore) }
        
        // Interpret score
        val interpretation = interpretScore(totalScore)
        _uiState.update { it.copy(interpretation = interpretation) }
    }
    
    private fun calculateTotalScore(answers: List<Answer>): Int {
        return answers.sumOf { it.score }
    }
    
    private fun interpretScore(score: Int): String {
        return when {
            score <= 6 -> "Baixo risco"
            score <= 13 -> "Risco moderado"
            else -> "Alto risco"
        }
    }

    /**
     * Calculate group score.
     */
    fun getGroupScore(groupId: String): Int {
        val questions = _uiState.value.questions.filter { it.groupId == groupId }
        return questions.sumOf { question ->
            answersMap[question.id]?.score ?: 0
        }
    }

    /**
     * Get total score.
     */
    fun getTotalScore(): Int {
        return _uiState.value.totalScore
    }

    /**
     * Get interpretation.
     */
    fun getInterpretation(): String {
        return _uiState.value.interpretation
    }

    /**
     * Get maximum possible score based on loaded questions.
     */
    fun getMaxScore(): Int {
        val questions = _uiState.value.questions
        return questions.sumOf { question ->
            question.options.maxOfOrNull { it.score } ?: 0
        }
    }

    /**
     * Validate if all questions are answered.
     */
    fun isComplete(): Boolean {
        val questions = _uiState.value.questions
        return questions.all { question ->
            answersMap.containsKey(question.id) && answersMap[question.id]?.selectedOptionIndex != -1
        }
    }

    /**
     * Get completion percentage.
     */
    fun getCompletionPercentage(): Int {
        val questions = _uiState.value.questions
        if (questions.isEmpty()) return 0
        
        val answeredCount = questions.count { question ->
            answersMap.containsKey(question.id) && answersMap[question.id]?.selectedOptionIndex != -1
        }
        
        return (answeredCount * 100) / questions.size
    }
    
    /**
     * Submit questionnaire answers to the backend API
     * Now using direct UUIDs from questions and options
     */
    fun submitAnswers(
        participantId: String,
        healthProfessionalId: String,
        token: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Use the questionnaire ID from state (captured during loading)
            val questionnaireId = _uiState.value.questionnaireId
            
            if (questionnaireId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Erro: ID do questionário não encontrado") }
                onError("Erro: ID do questionário não encontrado. Recarregue o questionário.")
                return@launch
            }
            
            // Get loaded questions for UUID lookup
            val questions = _uiState.value.questions
            
            // Convert local answers to API format using UUIDs from Question and Option objects
            val apiAnswers = answersMap.mapNotNull { (questionId, answer) ->
                // Find question by ID (now UUID)
                val question = questions.find { it.id == questionId }
                
                if (question != null && answer.selectedOptionIndex >= 0 && answer.selectedOptionIndex < question.options.size) {
                    val selectedOption = question.options[answer.selectedOptionIndex]
                    
                    // Both question ID and option ID are now UUIDs
                    if (selectedOption.id != null) {
                        com.ufpr.equilibrium.feature_questionnaire.payloads.AnswerRequest(
                            questionId = questionId,  // Already a UUID
                            selectedOptionId = selectedOption.id!!  // UUID from option
                        )
                    } else {
                        android.util.Log.w("QuestionnaireVM", "Option missing UUID for question $questionId option ${answer.selectedOptionIndex}")
                        null
                    }
                } else {
                    android.util.Log.w("QuestionnaireVM", "Question not found or invalid option for $questionId")
                    null
                }
            }
            
            android.util.Log.d("QuestionnaireVM", "Submitting ${apiAnswers.size} answers out of ${answersMap.size} total")
            
            val request = com.ufpr.equilibrium.feature_questionnaire.payloads.QuestionnaireResponseRequest(
                participantId = participantId,
                healthProfessionalId = healthProfessionalId,
                questionnaireId = questionnaireId,
                answers = apiAnswers
            )
            
            questionnaireRepository.submitQuestionnaireResponse(
                request = request,
                token = token,
                onSuccess = { response ->
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess("Questionário enviado com sucesso! ID: ${response.id}")
                },
                onError = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error) }
                    onError(error)
                }
            )
        }
    }
}
