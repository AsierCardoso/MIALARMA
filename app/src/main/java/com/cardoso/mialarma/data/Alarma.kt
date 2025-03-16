package com.cardoso.mialarma.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una alarma en la base de datos
 * @property id Identificador único de la alarma
 * @property hora Hora de la alarma (0-23)
 * @property minuto Minuto de la alarma (0-59)
 * @property activa Estado de la alarma (activada/desactivada)
 * @property titulo Descripción o título de la alarma
 * @property diasSemana Días en que se repite la alarma ("1,2,3,4,5,6,7" donde 1=Domingo)
 * @property vibracion Si la alarma debe vibrar
 * @property sonido URI del sonido personalizado o vacío para el predeterminado
 */
@Entity(tableName = "alarmas")
data class Alarma(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "hora")
    val hora: Int,

    @ColumnInfo(name = "minuto")
    val minuto: Int,

    @ColumnInfo(name = "activa")
    val activa: Boolean = true,

    @ColumnInfo(name = "titulo")
    val titulo: String = "",

    @ColumnInfo(name = "dias_semana")
    val diasSemana: String = "", // Formato: "1,2,3,4,5,6,7" donde 1 es domingo

    @ColumnInfo(name = "vibracion")
    val vibracion: Boolean = true,

    @ColumnInfo(name = "sonido")
    val sonido: String = "" // URI del sonido o vacío para sonido predeterminado
)