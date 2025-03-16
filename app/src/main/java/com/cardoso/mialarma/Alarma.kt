package com.cardoso.mialarma

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarmas")
data class Alarma(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hora: String,
    val activa: Boolean
)