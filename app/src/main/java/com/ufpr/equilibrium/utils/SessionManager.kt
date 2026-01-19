package com.ufpr.equilibrium.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.Gson
import com.ufpr.equilibrium.network.Usuario
import org.json.JSONObject

object SessionManager {
    private var prefs: SharedPreferences? = null
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs?.getString("TOKEN", null)
        set(value) = prefs?.edit()?.putString("TOKEN", value)?.apply()!!

    var user: Usuario?
        get() {
            val json = prefs?.getString("USUARIO", null)
            return json?.let { gson.fromJson(it, Usuario::class.java) }
        }
        set(value) {
            val json = gson.toJson(value)
            prefs?.edit()?.putString("USUARIO", json)?.apply()
        }

    fun clearSession() {
        prefs?.edit()?.clear()?.apply()
    }

    fun isLoggedIn(): Boolean {
        val token = this.token ?: return false
        return isTokenValid(token)
    }
    
    private fun isTokenValid(token: String): Boolean {
        return try {
            val payload = decodeJwtPayload(token) ?: return false
            val exp = payload.optLong("exp", 0)
            
            if (exp == 0L) return true // Token doesn't have exp claim
            
            val currentTimeSeconds = System.currentTimeMillis() / 1000
            exp > currentTimeSeconds // Token is valid if exp > current time
        } catch (e: Exception) {
            false // Invalid token format
        }
    }
    
    private fun decodeJwtPayload(token: String): JSONObject? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            JSONObject(payload)
        } catch (e: Exception) {
            null
        }
    }
}
