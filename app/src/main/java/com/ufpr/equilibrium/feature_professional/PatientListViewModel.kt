package com.ufpr.equilibrium.feature_professional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.Patient
import com.ufpr.equilibrium.domain.usecase.patient.GetPatientByCpfUseCase
import com.ufpr.equilibrium.domain.usecase.patient.GetPatientsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Patient List screen.
 * Refactored to use Clean Architecture with Use Cases.
 */
@HiltViewModel
class PatientListViewModel @Inject constructor(
    private val getPatientsUseCase: GetPatientsUseCase,
    private val getPatientByCpfUseCase: GetPatientByCpfUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientListUiState())
    val uiState: StateFlow<PatientListUiState> = _uiState.asStateFlow()

    init {
        loadPatients()
    }

    /**
     * Load all patients.
     */
    fun loadPatients() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            when (val result = getPatientsUseCase()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            patients = result.value.sortedBy { patient -> patient.fullName },
                            filteredPatients = result.value.sortedBy { patient -> patient.fullName },
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.cause.message ?: "Erro ao carregar pacientes"
                        )
                    }
                }
            }
        }
    }

    /**
     * Filter patients by CPF.
     */
    fun filterByCpf(cpf: String) {
        val allPatients = _uiState.value.patients
        
        if (cpf.isBlank()) {
            _uiState.update { it.copy(filteredPatients = allPatients) }
            return
        }
        
        val cleanCpf = cpf.filter { it.isDigit() }
        val filtered = allPatients.filter { patient ->
            patient.cpf.filter { it.isDigit() }.contains(cleanCpf)
        }
        
        _uiState.update { it.copy(filteredPatients = filtered) }
    }

    /**
     * Search patient by exact CPF match.
     */
    fun searchByCpf(cpf: String) {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = getPatientByCpfUseCase(cpf)) {
                is Result.Success -> {
                    val patient = result.value
                    _uiState.update {
                        it.copy(
                            filteredPatients = if (patient != null) listOf(patient) else emptyList(),
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao buscar paciente"
                        )
                    }
                }
            }
        }
    }
}

/**
 * UI State for Patient List screen.
 */
data class PatientListUiState(
    val patients: List<Patient> = emptyList(),
    val filteredPatients: List<Patient> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
