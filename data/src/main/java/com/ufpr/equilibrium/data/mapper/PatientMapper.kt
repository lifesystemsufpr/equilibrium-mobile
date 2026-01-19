package com.ufpr.equilibrium.data.mapper

import com.ufpr.equilibrium.data.remote.dto.PatientDto
import com.ufpr.equilibrium.data.remote.dto.PatientRegistrationDto
import com.ufpr.equilibrium.data.remote.dto.UserDto
import com.ufpr.equilibrium.domain.model.Address
import com.ufpr.equilibrium.domain.model.Patient
import java.util.UUID
import javax.inject.Inject

/**
 * Mapper to convert between Patient DTOs and Domain Models.
 */
class PatientMapper @Inject constructor() {
    
    /**
     * Convert PatientDto (from API) to Patient domain model.
     */
    fun toDomain(dto: PatientDto): Patient {
        return Patient(
            id = UUID.fromString(dto.id),
            cpf = dto.cpf,
            fullName = dto.fullName,
            phone = "", // Not available in this DTO
            birthDate = "", // Not available in this DTO
            gender = "", // Not available in this DTO
            age = dto.age,
            education = null,
            socioeconomicLevel = null,
            weight = dto.weight?.toInt(),
            height = dto.height,
            hasFallHistory = dto.downFall,
            address = null
        )
    }
    
    /**
     * Convert Patient domain model to PatientDto (for API).
     */
    fun toDto(patient: Patient): PatientDto {
        return PatientDto(
            id = patient.id.toString(),
            fullName = patient.fullName,
            cpf = patient.cpf,
            age = patient.age,
            weight = patient.weight?.toFloat(),
            height = patient.height,
            downFall = patient.hasFallHistory
        )
    }
    
    /**
     * Convert Patient domain model to PatientRegistrationDto (for registration API).
     */
    fun toRegistrationDto(patient: Patient): PatientRegistrationDto {
        return PatientRegistrationDto(
            user = UserDto(
                id = null,
                cpf = patient.cpf,
                fullName = patient.fullName,
                password = null,
                phone = patient.phone,
                gender = patient.gender,
                role = "PATIENT"
            ),
            birthday = patient.birthDate,
            weight = patient.weight ?: 0,
            height = patient.height,
            zipCode = patient.address?.zipCode ?: "",
            street = patient.address?.street ?: "",
            number = patient.address?.number?.toString() ?: "",
            complement = patient.address?.complement ?: "",
            neighborhood = patient.address?.neighborhood ?: "",
            scholarship = patient.education ?: "",
            socioEconomicLevel = patient.socioeconomicLevel ?: "",
            city = patient.address?.city ?: "",
            state = patient.address?.state ?: ""
        )
    }
    
    /**
     * Convert PatientRegistrationDto to Patient domain model.
     * Note: API response may not include user object, so we handle that case.
     */
    fun fromRegistrationDto(dto: PatientRegistrationDto): Patient {
        // Handle case where API doesn't return user object in response
        val userDto = dto.user ?: UserDto(
            id = null,
            cpf = "",
            fullName = "",
            password = null,
            phone = "",
            gender = "",
            role = "PATIENT"
        )
        
        return Patient(
            id = UUID.randomUUID(), // Will be assigned by backend
            cpf = userDto.cpf,
            fullName = userDto.fullName,
            phone = userDto.phone,
            birthDate = dto.birthday,
            gender = userDto.gender,
            age = calculateAge(dto.birthday),
            education = dto.scholarship,
            socioeconomicLevel = dto.socioEconomicLevel,
            weight = dto.weight,
            height = dto.height,
            hasFallHistory = false, // Default
            address = Address(
                zipCode = dto.zipCode,
                street = dto.street,
                number = dto.number.toIntOrNull() ?: 0,
                complement = dto.complement,
                neighborhood = dto.neighborhood,
                city = dto.city,
                state = dto.state,
                stateCode = dto.state // Assuming this is already the code
            )
        )
    }
    
    private fun calculateAge(birthDate: String): Int {
        // TODO: Implement proper age calculation from date
        return 0
    }
}
