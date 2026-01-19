package com.ufpr.equilibrium.utils

/**
 * Enum representing all Brazilian states with their official abbreviations (UF)
 */
enum class BrazilianState(val uf: String, val nome: String) {
    AC("AC", "Acre"),
    AL("AL", "Alagoas"),
    AP("AP", "Amapá"),
    AM("AM", "Amazonas"),
    BA("BA", "Bahia"),
    CE("CE", "Ceará"),
    DF("DF", "Distrito Federal"),
    ES("ES", "Espírito Santo"),
    GO("GO", "Goiás"),
    MA("MA", "Maranhão"),
    MT("MT", "Mato Grosso"),
    MS("MS", "Mato Grosso do Sul"),
    MG("MG", "Minas Gerais"),
    PA("PA", "Pará"),
    PB("PB", "Paraíba"),
    PR("PR", "Paraná"),
    PE("PE", "Pernambuco"),
    PI("PI", "Piauí"),
    RJ("RJ", "Rio de Janeiro"),
    RN("RN", "Rio Grande do Norte"),
    RS("RS", "Rio Grande do Sul"),
    RO("RO", "Rondônia"),
    RR("RR", "Roraima"),
    SC("SC", "Santa Catarina"),
    SP("SP", "São Paulo"),
    SE("SE", "Sergipe"),
    TO("TO", "Tocantins");

    companion object {
        /**
         * Returns all UF abbreviations as a list
         */
        fun getAllUFs(): List<String> = values().map { it.uf }
        
        /**
         * Returns state name from UF abbreviation
         */
        fun getStateName(uf: String): String? = values().find { it.uf == uf.uppercase() }?.nome
        
        /**
         * Checks if a UF is valid
         */
        fun isValidUF(uf: String): Boolean = values().any { it.uf == uf.uppercase() }
    }
}
