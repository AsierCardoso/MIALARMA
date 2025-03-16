package com.cardoso.mialarma

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cardoso.mialarma.adapters.AlarmaAdapter
import com.cardoso.mialarma.data.Alarma
import com.cardoso.mialarma.data.AlarmaDatabase
import com.cardoso.mialarma.services.AlarmaService
import com.cardoso.mialarma.databinding.FragmentAlarmaBinding
import com.cardoso.mialarma.viewmodels.AlarmaViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first

class AlarmaFragment : Fragment() {
    private var _binding: FragmentAlarmaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlarmaViewModel by viewModels()
    private lateinit var adapter: AlarmaAdapter
    private lateinit var database: AlarmaDatabase
    private var serviceIntent: Intent? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        database = AlarmaDatabase.getDatabase(requireContext())
        serviceIntent = Intent(requireContext(), AlarmaService::class.java)
        
        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Iniciar el servicio si hay alarmas activas
        lifecycleScope.launch {
            val alarmasActivas = database.alarmaDao().obtenerAlarmasActivas().first()
            if (alarmasActivas.isNotEmpty()) {
                requireContext().startService(serviceIntent)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AlarmaAdapter(
            onAlarmaClick = { alarma ->
                mostrarDialogoEditarAlarma(alarma)
            },
            onSwitchChange = { alarma, activa ->
                viewModel.cambiarEstadoAlarma(alarma.id, activa)
            }
        )

        binding.recyclerViewAlarmas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AlarmaFragment.adapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.todasLasAlarmas.collect { alarmas ->
                adapter.submitList(alarmas)
                actualizarVistaVacia(alarmas.isEmpty())
            }
        }
    }

    private fun setupListeners() {
        binding.fabAgregarAlarma.setOnClickListener {
            mostrarDialogoNuevaAlarma()
        }
    }

    private fun actualizarVistaVacia(isEmpty: Boolean) {
        binding.apply {
            if (isEmpty) {
                recyclerViewAlarmas.visibility = View.GONE
                tvSinAlarmas.visibility = View.VISIBLE
            } else {
                recyclerViewAlarmas.visibility = View.VISIBLE
                tvSinAlarmas.visibility = View.GONE
            }
        }
    }

    private fun mostrarDialogoNuevaAlarma() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_alarma, null)

        // Inicializar vistas
        val etNombreAlarma = dialogView.findViewById<TextInputEditText>(R.id.etNombreAlarma)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val rgRepeticion = dialogView.findViewById<RadioGroup>(R.id.rgRepeticion)
        val rbLunesViernes = dialogView.findViewById<RadioButton>(R.id.rbLunesViernes)
        val rbTodosDias = dialogView.findViewById<RadioButton>(R.id.rbTodosDias)
        
        val checkBoxes = listOf(
            dialogView.findViewById<CheckBox>(R.id.cbDomingo),
            dialogView.findViewById<CheckBox>(R.id.cbLunes),
            dialogView.findViewById<CheckBox>(R.id.cbMartes),
            dialogView.findViewById<CheckBox>(R.id.cbMiercoles),
            dialogView.findViewById<CheckBox>(R.id.cbJueves),
            dialogView.findViewById<CheckBox>(R.id.cbViernes),
            dialogView.findViewById<CheckBox>(R.id.cbSabado)
        )

        timePicker.setIs24HourView(true)

        // Manejar cambios en el RadioGroup
        rgRepeticion.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbUnaVez -> checkBoxes.forEach { it.isChecked = false }
                R.id.rbLunesViernes -> {
                    checkBoxes.forEachIndexed { index, checkBox ->
                        checkBox.isChecked = index in 1..5
                    }
                }
                R.id.rbTodosDias -> checkBoxes.forEach { it.isChecked = true }
                else -> { /* No hacer nada */ }
            }
        }

        // Manejar cambios en los CheckBox
        checkBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val seleccionados = checkBoxes.count { it.isChecked }
                rgRepeticion.clearCheck()
                when {
                    seleccionados == 0 -> rgRepeticion.check(R.id.rbUnaVez)
                    seleccionados == 7 -> rgRepeticion.check(R.id.rbTodosDias)
                    checkBoxes.slice(1..5).all { it.isChecked } && 
                    !checkBoxes[0].isChecked && !checkBoxes[6].isChecked ->
                        rgRepeticion.check(R.id.rbLunesViernes)
                    else -> { /* No hacer nada */ }
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.new_alarm))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val diasSeleccionados = checkBoxes.mapIndexedNotNull { index, checkBox ->
                    if (checkBox.isChecked) (index + 1).toString() else null
                }.joinToString(",")

                val nuevaAlarma = Alarma(
                    hora = timePicker.hour,
                    minuto = timePicker.minute,
                    activa = true,
                    titulo = etNombreAlarma.text?.toString() ?: "",
                    diasSemana = diasSeleccionados
                )
                lifecycleScope.launch {
                    database.alarmaDao().insertarAlarma(nuevaAlarma)
                    // Iniciar el servicio
                    requireContext().startService(Intent(requireContext(), AlarmaService::class.java))
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun mostrarDialogoEditarAlarma(alarma: Alarma) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_alarma, null)

        // Inicializar vistas
        val etNombreAlarma = dialogView.findViewById<TextInputEditText>(R.id.etNombreAlarma)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val rgRepeticion = dialogView.findViewById<RadioGroup>(R.id.rgRepeticion)
        val rbLunesViernes = dialogView.findViewById<RadioButton>(R.id.rbLunesViernes)
        val rbTodosDias = dialogView.findViewById<RadioButton>(R.id.rbTodosDias)
        
        val checkBoxes = listOf(
            dialogView.findViewById<CheckBox>(R.id.cbDomingo),
            dialogView.findViewById<CheckBox>(R.id.cbLunes),
            dialogView.findViewById<CheckBox>(R.id.cbMartes),
            dialogView.findViewById<CheckBox>(R.id.cbMiercoles),
            dialogView.findViewById<CheckBox>(R.id.cbJueves),
            dialogView.findViewById<CheckBox>(R.id.cbViernes),
            dialogView.findViewById<CheckBox>(R.id.cbSabado)
        )

        // Establecer valores actuales
        etNombreAlarma.setText(alarma.titulo)
        timePicker.setIs24HourView(true)
        timePicker.hour = alarma.hora
        timePicker.minute = alarma.minuto

        // Establecer días seleccionados
        val diasSeleccionados = alarma.diasSemana.split(",").filter { it.isNotEmpty() }
        checkBoxes.forEachIndexed { index, checkBox ->
            checkBox.isChecked = (index + 1).toString() in diasSeleccionados
        }

        // Actualizar RadioGroup según los días seleccionados
        when {
            diasSeleccionados.isEmpty() -> rgRepeticion.check(R.id.rbUnaVez)
            diasSeleccionados.size == 7 -> rgRepeticion.check(R.id.rbTodosDias)
            diasSeleccionados.containsAll(listOf("2", "3", "4", "5", "6")) &&
            !diasSeleccionados.contains("1") && !diasSeleccionados.contains("7") ->
                rgRepeticion.check(R.id.rbLunesViernes)
        }

        // Manejar cambios en el RadioGroup
        rgRepeticion.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbUnaVez -> checkBoxes.forEach { it.isChecked = false }
                R.id.rbLunesViernes -> {
                    checkBoxes.forEachIndexed { index, checkBox ->
                        checkBox.isChecked = index in 1..5
                    }
                }
                R.id.rbTodosDias -> checkBoxes.forEach { it.isChecked = true }
                else -> { /* No hacer nada */ }
            }
        }

        // Manejar cambios en los CheckBox
        checkBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val seleccionados = checkBoxes.count { it.isChecked }
                rgRepeticion.clearCheck()
                when {
                    seleccionados == 0 -> rgRepeticion.check(R.id.rbUnaVez)
                    seleccionados == 7 -> rgRepeticion.check(R.id.rbTodosDias)
                    checkBoxes.slice(1..5).all { it.isChecked } && 
                    !checkBoxes[0].isChecked && !checkBoxes[6].isChecked ->
                        rgRepeticion.check(R.id.rbLunesViernes)
                    else -> { /* No hacer nada */ }
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_alarm))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val diasActualizados = checkBoxes.mapIndexedNotNull { index, checkBox ->
                    if (checkBox.isChecked) (index + 1).toString() else null
                }.joinToString(",")

                val alarmaActualizada = alarma.copy(
                    hora = timePicker.hour,
                    minuto = timePicker.minute,
                    titulo = etNombreAlarma.text?.toString() ?: "",
                    diasSemana = diasActualizados
                )
                lifecycleScope.launch {
                    database.alarmaDao().actualizarAlarma(alarmaActualizada)
                    // Iniciar el servicio si la alarma está activa
                    if (alarmaActualizada.activa) {
                        requireContext().startService(Intent(requireContext(), AlarmaService::class.java))
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .setNeutralButton(getString(R.string.delete_alarm)) { _, _ ->
                lifecycleScope.launch {
                    database.alarmaDao().eliminarAlarma(alarma)
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}