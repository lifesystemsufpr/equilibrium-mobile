package com.ufpr.equilibrium.domain.usecase.patient

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.Patient
import com.ufpr.equilibrium.domain.repository.PatientRepository
import javax.inject.Inject

/**
 * Use case to create a new patient.
 */
class CreatePatientUseCase @Inject constructor(
    private val repository: PatientRepository
) {
    suspend operator fun invoke(patient: Patient): Result<Patient> {
        return repository.createPatient(patient)
    }
}
