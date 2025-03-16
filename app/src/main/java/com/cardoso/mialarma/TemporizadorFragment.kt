package com.cardoso.mialarma

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cardoso.mialarma.services.NotificationService
import java.util.concurrent.TimeUnit

class TemporizadorFragment : Fragment() {
    private lateinit var npHoras: NumberPicker
    private lateinit var npMinutos: NumberPicker
    private lateinit var npSegundos: NumberPicker
    private lateinit var tvTiempoRestante: TextView
    private lateinit var btnIniciar: Button
    private lateinit var btnCancelar: Button
    private lateinit var notificationService: NotificationService
    
    private var countDownTimer: CountDownTimer? = null
    private var tiempoTotalMillis: Long = 0
    private var temporizadorActivo = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_temporizador, container, false)
        
        notificationService = NotificationService(requireContext())
        
        inicializarVistas(view)
        configurarNumberPickers()
        configurarBotones()
        
        return view
    }

    private fun inicializarVistas(view: View) {
        npHoras = view.findViewById(R.id.npHoras)
        npMinutos = view.findViewById(R.id.npMinutos)
        npSegundos = view.findViewById(R.id.npSegundos)
        tvTiempoRestante = view.findViewById(R.id.tvTiempoRestante)
        btnIniciar = view.findViewById(R.id.btnIniciar)
        btnCancelar = view.findViewById(R.id.btnCancelar)
    }

    private fun configurarNumberPickers() {
        npHoras.apply {
            minValue = 0
            maxValue = 23
            wrapSelectorWheel = true
        }
        
        npMinutos.apply {
            minValue = 0
            maxValue = 59
            wrapSelectorWheel = true
        }
        
        npSegundos.apply {
            minValue = 0
            maxValue = 59
            wrapSelectorWheel = true
        }
    }

    private fun configurarBotones() {
        btnIniciar.setOnClickListener {
            if (!temporizadorActivo) {
                iniciarTemporizador()
            } else {
                pausarTemporizador()
            }
        }

        btnCancelar.setOnClickListener {
            cancelarTemporizador()
        }
    }

    private fun iniciarTemporizador() {
        val horas = npHoras.value
        val minutos = npMinutos.value
        val segundos = npSegundos.value
        
        if (horas == 0 && minutos == 0 && segundos == 0) {
            return
        }
        
        tiempoTotalMillis = TimeUnit.HOURS.toMillis(horas.toLong()) +
                           TimeUnit.MINUTES.toMillis(minutos.toLong()) +
                           TimeUnit.SECONDS.toMillis(segundos.toLong())
        
        countDownTimer = object : CountDownTimer(tiempoTotalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                actualizarVisualizacionTiempo(millisUntilFinished)
            }

            override fun onFinish() {
                temporizadorCompleto()
            }
        }.start()
        
        temporizadorActivo = true
        btnIniciar.setText(R.string.pause)
        deshabilitarPickers(false)
    }

    private fun pausarTemporizador() {
        countDownTimer?.cancel()
        temporizadorActivo = false
        btnIniciar.setText(R.string.start)
    }

    private fun cancelarTemporizador() {
        countDownTimer?.cancel()
        temporizadorActivo = false
        btnIniciar.setText(R.string.start)
        deshabilitarPickers(true)
        tvTiempoRestante.text = "00:00:00"
        npHoras.value = 0
        npMinutos.value = 0
        npSegundos.value = 0
    }

    private fun actualizarVisualizacionTiempo(millisUntilFinished: Long) {
        val horas = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
        val minutos = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
        val segundos = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
        
        tvTiempoRestante.text = String.format("%02d:%02d:%02d", horas, minutos, segundos)
    }

    private fun temporizadorCompleto() {
        temporizadorActivo = false
        btnIniciar.setText(R.string.start)
        deshabilitarPickers(true)
        tvTiempoRestante.text = "00:00:00"
        
        notificationService.mostrarNotificacionTemporizador(
            getString(R.string.timer_complete),
            getString(R.string.timer_message)
        )
    }

    private fun deshabilitarPickers(enabled: Boolean) {
        npHoras.isEnabled = enabled
        npMinutos.isEnabled = enabled
        npSegundos.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}