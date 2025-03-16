package com.cardoso.mialarma.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.cardoso.mialarma.R
import com.cardoso.mialarma.data.AlarmaDatabase
import kotlinx.coroutines.*
import java.util.*
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class AlarmaService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var verificacionJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var database: AlarmaDatabase
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val CHANNEL_ID = "AlarmaChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_SILENCIAR = "com.cardoso.mialarma.SILENCIAR"
    }

    override fun onCreate() {
        super.onCreate()
        database = AlarmaDatabase.getDatabase(this)
        createNotificationChannel()
        iniciarVerificacionAlarmas()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SILENCIAR -> detenerAlarma()
            else -> {
                if (verificacionJob == null) {
                    iniciarVerificacionAlarmas()
                }
            }
        }
        return START_STICKY
    }

    private fun iniciarVerificacionAlarmas() {
        verificacionJob = serviceScope.launch {
            while (isActive) {
                verificarAlarmas()
                // Esperar hasta el próximo minuto
                delay(calculaTiempoHastaProximoMinuto())
            }
        }
    }

    private fun calculaTiempoHastaProximoMinuto(): Long {
        val ahora = LocalDateTime.now()
        val siguienteMinuto = ahora.plusMinutes(1).withSecond(0).withNano(0)
        return java.time.Duration.between(ahora, siguienteMinuto).toMillis().coerceAtLeast(100)
    }

    private suspend fun verificarAlarmas() {
        try {
            val alarmasActivas = database.alarmaDao().obtenerAlarmasActivas().firstOrNull() ?: emptyList()
            val ahora = LocalTime.now()
            
            for (alarma in alarmasActivas) {
                if (alarma.hora == ahora.hour && alarma.minuto == ahora.minute) {
                    val diasSemana = alarma.diasSemana.split(",").filterNot { it.isEmpty() }
                    val diaActual = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                    
                    if (diasSemana.isEmpty() || diasSemana.contains(diaActual.toString())) {
                        activarAlarma(alarma.titulo)
                        
                        // Si es una alarma de una sola vez, desactivarla
                        if (diasSemana.isEmpty()) {
                            database.alarmaDao().actualizarAlarma(alarma.copy(activa = false))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun activarAlarma(titulo: String) {
        // Adquirir WakeLock
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmaService::WakeLock").apply {
                acquire(10*60*1000L) // 10 minutos máximo
            }
        }

        // Iniciar sonido
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                val sonidoAlarma = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                setDataSource(applicationContext, sonidoAlarma)
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                setLooping(true)
                prepare()
                start()
            }
        }

        // Crear intent para silenciar
        val silenciarIntent = Intent(this, AlarmaService::class.java).apply {
            action = ACTION_SILENCIAR
        }
        val silenciarPendingIntent = PendingIntent.getService(
            this, 0, silenciarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mostrar notificación
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.alarm_notifications))
            .setContentText(titulo)
            .setSmallIcon(R.mipmap.logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, 
                      getString(R.string.silence), 
                      silenciarPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun detenerAlarma() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        
        wakeLock?.release()
        wakeLock = null
        
        stopForeground(true)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.alarm_notifications),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.alarm_notifications)
                setSound(null, null) // Sin sonido en el canal, usamos MediaPlayer
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        verificacionJob?.cancel()
        detenerAlarma()
        serviceScope.cancel()
    }
}