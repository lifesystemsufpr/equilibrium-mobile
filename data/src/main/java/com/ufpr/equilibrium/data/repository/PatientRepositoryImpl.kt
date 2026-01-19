package com.ufpr.equilibrium.data.repository

import com.ufpr.equilibrium.core.common.Result
import com.ufpr.equilibrium.data.mapper.PatientMapper
import com.ufpr.equilibrium.data.remote.PessoasService
import com.ufpr.equilibrium.domain.model.Patient
import com.ufpr.equilibrium.domain.repository.PatientRepository
import javax.inject.Inject

/**
 * Implementation of PatientRepository.
 * Handles patient data operations through the API.
 */
class PatientRepositoryImpl @Inject constructor(
    private val service: PessoasService,
    private val mapper: PatientMapper
) : PatientRepository {
    
    override suspend fun getPatients(page: Int?, pageSize: Int?): Result<List<Patient>> {
        return try {
            val response = service.getParticipants(page, pageSize)
            val patients = response.data.map { mapper.toDomain(it) }
            Result.Success(patients)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getPatientByCpf(cpf: String): Result<Patient?> {
        return try {
            val response = service.getParticipants(cpf = cpf)
            val patient = response.data.firstOrNull()?.let { mapper.toDomain(it) }
            Result.Success(patient)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun createPatient(patient: Patient): Result<Patient> {
        return try {
            val dto = mapper.toRegistrationDto(patient)
            val responseDto = service.postParticipant(dto)
            val createdPatient = mapper.fromRegistrationDto(responseDto)
            Result.Success(createdPatient)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updatePatient(patient: Patient): Result<Patient> {
        // TODO: Implement update endpoint when available in API
        return Result.Error(Exception("Update not yet implemented"))
    }
}
