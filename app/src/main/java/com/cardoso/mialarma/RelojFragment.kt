package com.cardoso.mialarma

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cardoso.mialarma.adapters.ZonaHorariaAdapter
import com.cardoso.mialarma.data.ZonaHoraria
import com.cardoso.mialarma.data.ZonaHorariaManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class RelojFragment : Fragment() {
    private lateinit var tvHora: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var zonaHorariaAdapter: ZonaHorariaAdapter
    private lateinit var zonaHorariaManager: ZonaHorariaManager
    private val handler = Handler(Looper.getMainLooper())
    private val actualizarHora = object : Runnable {
        override fun run() {
            actualizarHoras()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reloj, container, false)
        
        tvHora = view.findViewById(R.id.tvHora)
        recyclerView = view.findViewById(R.id.recyclerViewZonasHorarias)
        val fabAgregarZona = view.findViewById<FloatingActionButton>(R.id.fabAgregarZona)
        
        zonaHorariaManager = ZonaHorariaManager(requireContext())
        
        configurarRecyclerView()
        configurarClickListeners()
        
        fabAgregarZona.setOnClickListener {
            mostrarDialogoSeleccionZona()
        }
        
        return view
    }

    private fun configurarRecyclerView() {
        zonaHorariaAdapter = ZonaHorariaAdapter(
            onZonaHorariaClick = { zonaHoraria ->
                // Click normal no hace nada por ahora
            },
            onZonaHorariaLongClick = { zonaHoraria ->
                mostrarDialogoEliminarZona(zonaHoraria)
                true
            }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = zonaHorariaAdapter
        }
        
        actualizarListaZonas()
    }

    private fun configurarClickListeners() {
        tvHora.setOnLongClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.open_time_settings_question)
                .setPositiveButton(R.string.yes) { _, _ ->
                    try {
                        startActivity(Intent(Settings.ACTION_DATE_SETTINGS))
                    } catch (e: Exception) {
                        Toast.makeText(context, R.string.cannot_open_settings, Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(R.string.no, null)
                .show()
            true
        }
    }

    private fun actualizarHoras() {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        tvHora.text = sdf.format(Date())
        zonaHorariaAdapter.notifyDataSetChanged() // Actualiza las horas de todas las zonas
    }

    private fun actualizarListaZonas() {
        val zonasFavoritas = zonaHorariaManager.obtenerZonasFavoritas()
        val zonasDisponibles = ZonaHorariaManager.obtenerZonasHorariasDisponibles()
        val zonasFiltradas = zonasDisponibles.filter { zonasFavoritas.contains(it.id) }
        zonaHorariaAdapter.submitList(zonasFiltradas)
    }

    private fun mostrarDialogoSeleccionZona() {
        val zonasDisponibles = ZonaHorariaManager.obtenerZonasHorariasDisponibles()
        val zonasFavoritas = zonaHorariaManager.obtenerZonasFavoritas()
        val zonasNoFavoritas = zonasDisponibles.filter { !zonasFavoritas.contains(it.id) }
        
        if (zonasNoFavoritas.isEmpty()) {
            Toast.makeText(context, "Ya has añadido todas las zonas horarias disponibles", Toast.LENGTH_SHORT).show()
            return
        }
        
        val nombres = zonasNoFavoritas.map { it.nombre }.toTypedArray()
        
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.timezone_management)
            .setItems(nombres) { _, which ->
                val zonaSeleccionada = zonasNoFavoritas[which]
                zonaHorariaManager.agregarZonaFavorita(zonaSeleccionada.id)
                actualizarListaZonas()
                Toast.makeText(context, "Zona horaria añadida", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun mostrarDialogoEliminarZona(zonaHoraria: ZonaHoraria) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.timezone_management)
            .setMessage(R.string.remove_timezone_question)
            .setPositiveButton(R.string.yes) { _, _ ->
                zonaHorariaManager.eliminarZonaFavorita(zonaHoraria.id)
                actualizarListaZonas()
                Toast.makeText(context, R.string.timezone_removed, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        handler.post(actualizarHora)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(actualizarHora)
    }
}