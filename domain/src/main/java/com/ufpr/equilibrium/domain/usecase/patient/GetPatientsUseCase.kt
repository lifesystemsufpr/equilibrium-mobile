package com.ufpr.equilibrium.domain.usecase.patient

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.domain.model.Patient
import com.ufpr.equilibrium.domain.repository.PatientRepository
import javax.inject.Inject

/**
 * Use case to get list of patients.
 */
class GetPatientsUseCase @Inject constructor(
    private val repository: PatientRepository
) {
    suspend operator fun invoke(page: Int? = null, pageSize: Int? = null): Result<List<Patient>> {
        return repository.getPatients(page, pageSize)
    }
}
