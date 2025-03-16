package com.cardoso.mialarma.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

class PreferenciasManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Guardar tema favorito
    fun guardarTema(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_TEMA, isDarkMode).apply()
    }

    // Obtener tema favorito
    fun obtenerTema(): Boolean {
        return sharedPreferences.getBoolean(KEY_TEMA, false)
    }

    // Aplicar tema guardado
    fun aplicarTema() {
        val isDarkMode = obtenerTema()
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    // Guardar idioma favorito
    fun guardarIdioma(codigoIdioma: String) {
        sharedPreferences.edit().putString(KEY_IDIOMA, codigoIdioma).apply()
    }

    // Obtener idioma favorito
    fun obtenerIdioma(): String {
        return sharedPreferences.getString(KEY_IDIOMA, Locale.getDefault().language) ?: "es"
    }

    // Guardar zona horaria favorita
    fun guardarZonaHoraria(nombreCiudad: String, zonaHoraria: String) {
        val zonasHorarias = obtenerZonasHorarias().toMutableMap()
        zonasHorarias[nombreCiudad] = zonaHoraria
        val zonasHorariasSet = zonasHorarias.entries.map { "${it.key}:${it.value}" }.toSet()
        sharedPreferences.edit().putStringSet(KEY_ZONAS_HORARIAS, zonasHorariasSet).apply()
    }

    // Eliminar zona horaria favorita
    fun eliminarZonaHoraria(nombreCiudad: String) {
        val zonasHorarias = obtenerZonasHorarias().toMutableMap()
        zonasHorarias.remove(nombreCiudad)
        val zonasHorariasSet = zonasHorarias.entries.map { "${it.key}:${it.value}" }.toSet()
        sharedPreferences.edit().putStringSet(KEY_ZONAS_HORARIAS, zonasHorariasSet).apply()
    }

    // Obtener zonas horarias favoritas
    fun obtenerZonasHorarias(): Map<String, String> {
        val zonasHorariasSet = sharedPreferences.getStringSet(KEY_ZONAS_HORARIAS, setOf()) ?: setOf()
        return zonasHorariasSet.map {
            val (ciudad, zona) = it.split(":")
            ciudad to zona
        }.toMap()
    }

    companion object {
        private const val PREFS_NAME = "MiAlarmaPrefs"
        private const val KEY_TEMA = "tema"
        private const val KEY_IDIOMA = "idioma"
        private const val KEY_ZONAS_HORARIAS = "zonas_horarias"
    }
} 