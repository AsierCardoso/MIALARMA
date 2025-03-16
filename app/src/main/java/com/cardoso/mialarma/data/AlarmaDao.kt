package com.cardoso.mialarma.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la entidad Alarma
 * Proporciona métodos para acceder y modificar alarmas en la base de datos
 */
@Dao
interface AlarmaDao {
    /**
     * Obtiene todas las alarmas ordenadas por hora y minuto
     * @return Flow con la lista de alarmas
     */
    @Query("SELECT * FROM alarmas ORDER BY hora ASC, minuto ASC")
    fun obtenerTodasLasAlarmas(): Flow<List<Alarma>>

    /**
     * Obtiene una alarma específica por su ID
     * @param id ID de la alarma
     * @return La alarma encontrada o null si no existe
     */
    @Query("SELECT * FROM alarmas WHERE id = :id")
    suspend fun obtenerAlarmaPorId(id: Long): Alarma?

    /**
     * Inserta una nueva alarma en la base de datos
     * @param alarma La alarma a insertar
     * @return El ID de la alarma insertada
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAlarma(alarma: Alarma): Long

    /**
     * Actualiza una alarma existente
     * @param alarma La alarma a actualizar
     */
    @Update
    suspend fun actualizarAlarma(alarma: Alarma)

    /**
     * Elimina una alarma de la base de datos
     * @param alarma La alarma a eliminar
     */
    @Delete
    suspend fun eliminarAlarma(alarma: Alarma)

    /**
     * Elimina una alarma por su ID
     * @param id ID de la alarma a eliminar
     */
    @Query("DELETE FROM alarmas WHERE id = :id")
    suspend fun eliminarAlarmaPorId(id: Long)

    /**
     * Actualiza el estado de activación de una alarma
     * @param id ID de la alarma
     * @param activa Nuevo estado de activación
     */
    @Query("UPDATE alarmas SET activa = :activa WHERE id = :id")
    suspend fun actualizarEstadoAlarma(id: Long, activa: Boolean)

    /**
     * Obtiene todas las alarmas activas
     * @return Flow con la lista de alarmas activas
     */
    @Query("SELECT * FROM alarmas WHERE activa = 1 ORDER BY hora ASC, minuto ASC")
    fun obtenerAlarmasActivas(): Flow<List<Alarma>>

    /**
     * Obtiene las alarmas programadas para ciertos días de la semana
     * @param diasSemana Lista de días (1-7, donde 1 es domingo)
     * @return Flow con la lista de alarmas para esos días
     */
    @Query("SELECT * FROM alarmas WHERE activa = 1 AND dias_semana LIKE '%' || :dia || '%' ORDER BY hora ASC, minuto ASC")
    fun obtenerAlarmasPorDia(dia: String): Flow<List<Alarma>>
}