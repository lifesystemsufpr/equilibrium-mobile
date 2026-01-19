package com.ufpr.equilibrium.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

object PacienteManager {

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences("paciente_cpf", Context.MODE_PRIVATE)
    }

    var cpf: String?
        get() = prefs?.getString("CPF", null)
        set(value) = prefs?.edit()?.putString("CPF", value)?.apply()!!

    var teste: String?
        get() = prefs?.getString("CPF", null)
        set(value) = prefs?.edit()?.putString("CPF", value)?.apply()!!

    var uuid: UUID?
        get() {
            val idString = prefs?.getString("id", null)
            val uuid = idString?.let { UUID.fromString(it) }
            android.util.Log.d("PacienteManager", "Getting UUID: $uuid (string: $idString)")
            return uuid
        }
        set(value) {
            android.util.Log.d("PacienteManager", "Setting UUID: $value")
            prefs?.edit()?.putString("id", value?.toString())?.apply()
            android.util.Log.d("PacienteManager", "UUID saved to SharedPreferences")
        }

    var nome: String?
        get() = prefs?.getString("nome", null)
        set(value) = prefs?.edit()?.putString("nome", value)?.apply()!!

    fun clearPacienteCpf() {
        prefs?.edit()?.clear()?.apply()
    }

}