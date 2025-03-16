package com.cardoso.mialarma.repository

import com.cardoso.mialarma.data.Alarma
import com.cardoso.mialarma.data.AlarmaDao
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Repositorio para manejar las operaciones de negocio relacionadas con las alarmas
 * Implementa el patrón Repository para abstraer la fuente de datos
 */
class AlarmaRepository(private val alarmaDao: AlarmaDao) {

    /**
     * Obtiene todas las alarmas ordenadas
     * @return Flow con la lista de alarmas
     */
    fun obtenerTodasLasAlarmas(): Flow<List<Alarma>> = alarmaDao.obtenerTodasLasAlarmas()

    /**
     * Obtiene las alarmas activas
     * @return Flow con la lista de alarmas activas
     */
    fun obtenerAlarmasActivas(): Flow<List<Alarma>> = alarmaDao.obtenerAlarmasActivas()

    /**
     * Obtiene las alarmas para el día actual
     * @return Flow con la lista de alarmas para hoy
     */
    fun obtenerAlarmasParaHoy(): Flow<List<Alarma>> {
        val calendar = Calendar.getInstance()
        // En Calendar, domingo es 1 y sábado es 7, coincide con nuestro formato
        val diaActual = calendar.get(Calendar.DAY_OF_WEEK).toString()
        return alarmaDao.obtenerAlarmasPorDia(diaActual)
    }

    /**
     * Inserta una nueva alarma
     * @param alarma La alarma a insertar
     * @return ID de la alarma insertada
     */
    suspend fun insertarAlarma(alarma: Alarma): Long = alarmaDao.insertarAlarma(alarma)

    /**
     * Actualiza una alarma existente
     * @param alarma La alarma a actualizar
     */
    suspend fun actualizarAlarma(alarma: Alarma) = alarmaDao.actualizarAlarma(alarma)

    /**
     * Elimina una alarma
     * @param alarma La alarma a eliminar
     */
    suspend fun eliminarAlarma(alarma: Alarma) = alarmaDao.eliminarAlarma(alarma)

    /**
     * Cambia el estado de activación de una alarma
     * @param id ID de la alarma
     * @param activa Nuevo estado de activación
     */
    suspend fun cambiarEstadoAlarma(id: Long, activa: Boolean) = 
        alarmaDao.actualizarEstadoAlarma(id, activa)

    /**
     * Obtiene una alarma por su ID
     * @param id ID de la alarma
     * @return La alarma encontrada o null
     */
    suspend fun obtenerAlarmaPorId(id: Long): Alarma? = alarmaDao.obtenerAlarmaPorId(id)

    /**
     * Verifica si una alarma debe sonar en este momento
     * @param alarma La alarma a verificar
     * @return true si la alarma debe sonar
     */
    fun debeActivarseAhora(alarma: Alarma): Boolean {
        if (!alarma.activa) return false

        val calendar = Calendar.getInstance()
        val horaActual = calendar.get(Calendar.HOUR_OF_DAY)
        val minutoActual = calendar.get(Calendar.MINUTE)
        val diaActual = calendar.get(Calendar.DAY_OF_WEEK).toString()

        return alarma.hora == horaActual &&
               alarma.minuto == minutoActual &&
               (alarma.diasSemana.isEmpty() || alarma.diasSemana.contains(diaActual))
    }
} 