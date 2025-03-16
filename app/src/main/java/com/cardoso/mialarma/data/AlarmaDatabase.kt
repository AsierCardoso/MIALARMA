package com.cardoso.mialarma.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos principal de la aplicación
 * Utiliza Room como capa de abstracción sobre SQLite
 */
@Database(
    entities = [Alarma::class],
    version = 1,
    exportSchema = false
)
abstract class AlarmaDatabase : RoomDatabase() {
    /**
     * Proporciona acceso al DAO de alarmas
     */
    abstract fun alarmaDao(): AlarmaDao

    companion object {
        @Volatile
        private var INSTANCE: AlarmaDatabase? = null

        /**
         * Obtiene una instancia de la base de datos
         * Utiliza el patrón Singleton para asegurar una única instancia
         * @param context Contexto de la aplicación
         * @return Instancia de la base de datos
         */
        fun getDatabase(context: Context): AlarmaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlarmaDatabase::class.java,
                    "alarma_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Inicializar la base de datos con datos de ejemplo si es necesario
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                inicializarBaseDeDatos(database.alarmaDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Inicializa la base de datos con datos de ejemplo
         * @param alarmaDao DAO para acceder a la base de datos
         */
        private suspend fun inicializarBaseDeDatos(alarmaDao: AlarmaDao) {
            // Aquí puedes añadir alarmas de ejemplo si lo deseas
            // Por ejemplo, una alarma para despertar a las 7:00
            val alarmaEjemplo = Alarma(
                hora = 7,
                minuto = 0,
                activa = true,
                titulo = "Despertar",
                diasSemana = "2,3,4,5,6", // Lunes a Viernes
                vibracion = true
            )
            alarmaDao.insertarAlarma(alarmaEjemplo)
        }
    }
}