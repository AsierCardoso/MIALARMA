package com.cardoso.mialarma
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Alarma::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmaDao(): AlarmaDao
}