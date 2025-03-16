package com.cardoso.mialarma

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Locale

class CronometroFragment : Fragment() {
    private lateinit var tvTiempo: TextView
    private lateinit var tvVueltas: TextView
    private lateinit var btnIniciar: Button
    private lateinit var btnVuelta: Button
    private lateinit var btnReiniciar: Button
    
    private var tiempoInicio = 0L
    private var tiempoPausado = 0L
    private var corriendo = false
    private val handler = Handler(Looper.getMainLooper())
    private val vueltas = mutableListOf<String>()
    
    private val actualizarTiempo = object : Runnable {
        override fun run() {
            val tiempoActual = if (corriendo) {
                SystemClock.elapsedRealtime() - tiempoInicio
            } else {
                tiempoPausado
            }
            actualizarVisualizacionTiempo(tiempoActual)
            handler.postDelayed(this, 10) // Actualizar cada 10ms para mostrar centésimas
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cronometro, container, false)
        
        inicializarVistas(view)
        configurarBotones()
        
        return view
    }

    private fun inicializarVistas(view: View) {
        tvTiempo = view.findViewById(R.id.tvTiempo)
        tvVueltas = view.findViewById(R.id.tvVueltas)
        btnIniciar = view.findViewById(R.id.btnIniciar)
        btnVuelta = view.findViewById(R.id.btnVuelta)
        btnReiniciar = view.findViewById(R.id.btnReiniciar)
        
        tvTiempo.text = "00:00:00.00"
        btnVuelta.isEnabled = false
    }

    private fun configurarBotones() {
        btnIniciar.setOnClickListener {
            if (!corriendo) {
                iniciarCronometro()
            } else {
                pausarCronometro()
            }
        }

        btnVuelta.setOnClickListener {
            registrarVuelta()
        }

        btnReiniciar.setOnClickListener {
            reiniciarCronometro()
        }
    }

    private fun iniciarCronometro() {
        if (!corriendo) {
            tiempoInicio = SystemClock.elapsedRealtime() - tiempoPausado
            handler.post(actualizarTiempo)
            corriendo = true
            btnIniciar.setText(R.string.pause)
            btnVuelta.isEnabled = true
        }
    }

    private fun pausarCronometro() {
        if (corriendo) {
            tiempoPausado = SystemClock.elapsedRealtime() - tiempoInicio
            handler.removeCallbacks(actualizarTiempo)
            corriendo = false
            btnIniciar.setText(R.string.start)
            btnVuelta.isEnabled = false
        }
    }

    private fun reiniciarCronometro() {
        handler.removeCallbacks(actualizarTiempo)
        corriendo = false
        tiempoInicio = SystemClock.elapsedRealtime()
        tiempoPausado = 0L
        actualizarVisualizacionTiempo(0)
        btnIniciar.setText(R.string.start)
        btnVuelta.isEnabled = false
        vueltas.clear()
        actualizarVisualizacionVueltas()
    }

    private fun registrarVuelta() {
        val tiempoActual = SystemClock.elapsedRealtime() - tiempoInicio
        val numeroVuelta = vueltas.size + 1
        
        val textoVuelta = String.format(
            Locale.getDefault(),
            getString(R.string.lap_format),
            numeroVuelta,
            formatearTiempo(tiempoActual)
        )
        
        vueltas.add(0, textoVuelta) // Añadir al principio de la lista
        actualizarVisualizacionVueltas()
    }

    private fun actualizarVisualizacionTiempo(tiempoMillis: Long) {
        tvTiempo.text = formatearTiempo(tiempoMillis)
    }

    private fun actualizarVisualizacionVueltas() {
        tvVueltas.text = vueltas.joinToString("\n")
    }

    private fun formatearTiempo(tiempoMillis: Long): String {
        val centesimas = (tiempoMillis / 10) % 100
        val segundos = (tiempoMillis / 1000) % 60
        val minutos = (tiempoMillis / (1000 * 60)) % 60
        val horas = (tiempoMillis / (1000 * 60 * 60)) % 24
        
        return String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d.%02d",
            horas, minutos, segundos, centesimas
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(actualizarTiempo)
    }
}