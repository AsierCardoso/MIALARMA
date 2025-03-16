package com.cardoso.mialarma

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlarmaDao {
    @Query("SELECT * FROM alarmas")
    fun getAll(): LiveData<List<Alarma>>

    @Insert
    suspend fun insert(alarma: Alarma)
}