package com.cardoso.mialarma.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cardoso.mialarma.data.Alarma
import com.cardoso.mialarma.data.AlarmaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar las alarmas
 * Implementa el patrón MVVM para separar la lógica de negocio de la UI
 */
class AlarmaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AlarmaDatabase.getDatabase(application)
    private val alarmaDao = database.alarmaDao()

    /**
     * Obtiene todas las alarmas ordenadas
     * @return Flow con la lista de alarmas
     */
    val todasLasAlarmas: Flow<List<Alarma>> = alarmaDao.obtenerTodasLasAlarmas()

    /**
     * Obtiene las alarmas activas
     * @return Flow con la lista de alarmas activas
     */
    val alarmasActivas: Flow<List<Alarma>> = alarmaDao.obtenerAlarmasActivas()

    /**
     * Inserta una nueva alarma
     * @param alarma La alarma a insertar
     */
    fun insertarAlarma(alarma: Alarma) = viewModelScope.launch(Dispatchers.IO) {
        alarmaDao.insertarAlarma(alarma)
    }

    /**
     * Actualiza una alarma existente
     * @param alarma La alarma a actualizar
     */
    fun actualizarAlarma(alarma: Alarma) = viewModelScope.launch(Dispatchers.IO) {
        alarmaDao.actualizarAlarma(alarma)
    }

    /**
     * Elimina una alarma
     * @param alarma La alarma a eliminar
     */
    fun eliminarAlarma(alarma: Alarma) = viewModelScope.launch(Dispatchers.IO) {
        alarmaDao.eliminarAlarma(alarma)
    }

    /**
     * Cambia el estado de activación de una alarma
     * @param id ID de la alarma
     * @param activa Nuevo estado de activación
     */
    fun cambiarEstadoAlarma(id: Long, activa: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        alarmaDao.actualizarEstadoAlarma(id, activa)
    }

    /**
     * Obtiene las alarmas para un día específico
     * @param dia Número del día (1-7, donde 1 es domingo)
     * @return Flow con la lista de alarmas para ese día
     */
    fun obtenerAlarmasPorDia(dia: String): Flow<List<Alarma>> {
        return alarmaDao.obtenerAlarmasPorDia(dia)
    }

    /**
     * Obtiene una alarma por su ID
     * @param id ID de la alarma
     * @return La alarma encontrada o null
     */
    suspend fun obtenerAlarmaPorId(id: Long): Alarma? {
        return alarmaDao.obtenerAlarmaPorId(id)
    }
} 