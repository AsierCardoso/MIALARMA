package com.cardoso.mialarma.data

import android.content.Context
import android.content.SharedPreferences
import java.util.TimeZone

data class ZonaHoraria(
    val id: String,
    val nombre: String,
    val offsetHoras: Int
)

class ZonaHorariaManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("zonas_horarias", Context.MODE_PRIVATE)
    private val ZONAS_FAVORITAS_KEY = "zonas_favoritas"

    fun obtenerZonasFavoritas(): Set<String> {
        return prefs.getStringSet(ZONAS_FAVORITAS_KEY, setOf()) ?: setOf()
    }

    fun agregarZonaFavorita(zonaId: String) {
        val zonasActuales = obtenerZonasFavoritas().toMutableSet()
        zonasActuales.add(zonaId)
        prefs.edit().putStringSet(ZONAS_FAVORITAS_KEY, zonasActuales).apply()
    }

    fun eliminarZonaFavorita(zonaId: String) {
        val zonasActuales = obtenerZonasFavoritas().toMutableSet()
        zonasActuales.remove(zonaId)
        prefs.edit().putStringSet(ZONAS_FAVORITAS_KEY, zonasActuales).apply()
    }

    fun esZonaFavorita(zonaId: String): Boolean {
        return obtenerZonasFavoritas().contains(zonaId)
    }

    companion object {
        fun obtenerZonasHorariasDisponibles(): List<ZonaHoraria> {
            return listOf(
                ZonaHoraria("America/New_York", "Nueva York", -5),
                ZonaHoraria("Europe/London", "Londres", 0),
                ZonaHoraria("Asia/Tokyo", "Tokio", 9),
                ZonaHoraria("Australia/Sydney", "Sídney", 11),
                ZonaHoraria("Europe/Paris", "París", 1),
                ZonaHoraria("Asia/Dubai", "Dubái", 4),
                ZonaHoraria("Asia/Shanghai", "Shanghái", 8),
                ZonaHoraria("America/Los_Angeles", "Los Ángeles", -8)
            )
        }
    }
}