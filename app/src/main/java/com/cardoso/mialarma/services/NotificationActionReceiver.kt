package com.cardoso.mialarma.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cardoso.mialarma.data.AlarmaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notificationId", -1)
        val alarmaId = intent.getLongExtra("alarmaId", -1L)

        if (notificationId != -1) {
            val notificationService = NotificationService(context)
            
            when (intent.action) {
                NotificationService.ACTION_SILENCIAR,
                NotificationService.ACTION_ELIMINAR -> {
                    when (notificationId) {
                        NotificationService.NOTIFICATION_ID_ALARMA -> {
                            // Para alarmas: cancelar notificación y eliminar la alarma
                            notificationService.cancelarNotificacion(notificationId)
                            if (alarmaId != -1L) {
                                scope.launch {
                                    val database = AlarmaDatabase.getDatabase(context)
                                    database.alarmaDao().eliminarAlarmaPorId(alarmaId)
                                }
                            }
                        }
                        NotificationService.NOTIFICATION_ID_TEMPORIZADOR -> {
                            // Para temporizador: solo cancelar notificación y enviar broadcast para resetear
                            notificationService.cancelarNotificacion(notificationId)
                            context.sendBroadcast(Intent("com.cardoso.mialarma.RESET_TIMER"))
                        }
                    }
                }
            }
        }
    }
} 