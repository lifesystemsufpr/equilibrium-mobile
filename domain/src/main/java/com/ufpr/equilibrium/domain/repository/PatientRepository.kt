package com.ufpr.equilibrium.domain.repository

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.Patient

/**
 * Repository contract for Patient data operations.
 * Defines what operations are available without knowing implementation details.
 */
interface PatientRepository {
    
    /**
     * Get list of patients with optional pagination.
     */
    suspend fun getPatients(page: Int? = null, pageSize: Int? = null): Result<List<Patient>>
    
    /**
     * Get a specific patient by CPF.
     */
    suspend fun getPatientByCpf(cpf: String): Result<Patient?>
    
    /**
     * Create a new patient.
     */
    suspend fun createPatient(patient: Patient): Result<Patient>
    
    /**
     * Update existing patient data.
     */
    suspend fun updatePatient(patient: Patient): Result<Patient>
}
