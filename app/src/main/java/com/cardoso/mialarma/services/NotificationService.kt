package com.cardoso.mialarma.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.cardoso.mialarma.MainActivity
import com.cardoso.mialarma.R

class NotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    
    companion object {
        const val CHANNEL_ID_ALARMA = "canal_alarmas"
        const val CHANNEL_ID_TEMPORIZADOR = "canal_temporizador"
        const val NOTIFICATION_ID_ALARMA = 1
        const val NOTIFICATION_ID_TEMPORIZADOR = 2
        const val ACTION_SILENCIAR = "com.cardoso.mialarma.SILENCIAR"
        const val ACTION_ELIMINAR = "com.cardoso.mialarma.ELIMINAR"
    }

    init {
        crearCanalesNotificacion()
        inicializarVibrador()
    }

    private fun inicializarVibrador() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun crearCanalesNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val canalAlarma = NotificationChannel(
                CHANNEL_ID_ALARMA,
                context.getString(R.string.alarms),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.alarm_notifications)
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes)
            }

            val canalTemporizador = NotificationChannel(
                CHANNEL_ID_TEMPORIZADOR,
                context.getString(R.string.timer),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.timer_notifications)
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
            }

            notificationManager.createNotificationChannel(canalAlarma)
            notificationManager.createNotificationChannel(canalTemporizador)
        }
    }

    fun mostrarNotificacionAlarma(titulo: String, mensaje: String) {
        // Iniciar sonido de alarma
        reproducirSonidoAlarma()
        
        // Iniciar vibración
        vibrar(true)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_ALARMA,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent para el botón de silenciar
        val silenciarIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SILENCIAR
            putExtra("notificationId", NOTIFICATION_ID_ALARMA)
        }
        val silenciarPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            silenciarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent para cuando se desliza la notificación
        val eliminarIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_ELIMINAR
            putExtra("notificationId", NOTIFICATION_ID_ALARMA)
        }
        val eliminarPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            eliminarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID_ALARMA)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(context.getString(R.string.alarm_notification_title))
            .setContentText(context.getString(R.string.alarm_notification_message, titulo))
            .setAutoCancel(true)
            .setOngoing(true)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(eliminarPendingIntent)
            .addAction(R.drawable.ic_silence, context.getString(R.string.silence), silenciarPendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_ALARMA, notificacion)
    }

    fun mostrarNotificacionTemporizador(titulo: String, mensaje: String) {
        // Iniciar sonido de alarma
        reproducirSonidoAlarma()
        
        // Iniciar vibración
        vibrar(false)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_TEMPORIZADOR,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent para el botón de silenciar
        val silenciarIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SILENCIAR
            putExtra("notificationId", NOTIFICATION_ID_TEMPORIZADOR)
        }
        val silenciarPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            silenciarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent para cuando se desliza la notificación
        val eliminarIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_ELIMINAR
            putExtra("notificationId", NOTIFICATION_ID_TEMPORIZADOR)
        }
        val eliminarPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            eliminarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID_TEMPORIZADOR)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setAutoCancel(true)
            .setOngoing(true)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(eliminarPendingIntent)
            .addAction(R.drawable.ic_silence, context.getString(R.string.silence), silenciarPendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_TEMPORIZADOR, notificacion)
    }

    private fun reproducirSonidoAlarma() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(
                    context,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                )
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibrar(isAlarma: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val patron = if (isAlarma) 
                longArrayOf(0, 1000, 500, 1000) 
            else 
                longArrayOf(0, 500, 250, 500)
            
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    patron,
                    0 // Repetir indefinidamente
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(
                if (isAlarma) 
                    longArrayOf(0, 1000, 500, 1000) 
                else 
                    longArrayOf(0, 500, 250, 500),
                0 // Repetir indefinidamente
            )
        }
    }

    fun cancelarNotificacion(id: Int) {
        // Detener sonido
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null

        // Detener vibración
        vibrator?.cancel()

        // Cancelar notificación
        notificationManager.cancel(id)
    }

    // Método para asegurarnos de que todos los recursos se liberan
    fun liberarRecursos() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        vibrator?.cancel()
    }
} 