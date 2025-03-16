package com.cardoso.mialarma.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cardoso.mialarma.R
import com.cardoso.mialarma.data.Alarma
import com.cardoso.mialarma.databinding.ItemAlarmaBinding
import java.util.*

/**
 * Adaptador para el RecyclerView de alarmas
 * Implementa ListAdapter para manejar eficientemente los cambios en la lista
 */
class AlarmaAdapter(
    private val onAlarmaClick: (Alarma) -> Unit,
    private val onSwitchChange: (Alarma, Boolean) -> Unit
) : ListAdapter<Alarma, AlarmaAdapter.AlarmaViewHolder>(AlarmaDiffCallback()) {

    /**
     * ViewHolder para las vistas de alarma
     * @property binding ViewBinding para el layout del item
     */
    inner class AlarmaViewHolder(
        private val binding: ItemAlarmaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de la alarma con la vista
         * @param alarma La alarma a mostrar
         */
        fun bind(alarma: Alarma) {
            binding.apply {
                // Formato de hora
                val horaFormateada = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    alarma.hora,
                    alarma.minuto
                )
                tvHora.text = horaFormateada
                
                // Título de la alarma
                tvTitulo.text = alarma.titulo.ifEmpty { 
                    root.context.getString(R.string.alarm) 
                }
                
                // Días de la semana
                val dias = alarma.diasSemana.split(",").filter { it.isNotEmpty() }
                val diasTexto = when {
                    dias.isEmpty() -> root.context.getString(R.string.once)
                    dias.size == 7 -> root.context.getString(R.string.everyday)
                    dias.size == 5 && dias.containsAll(listOf("2", "3", "4", "5", "6")) ->
                        root.context.getString(R.string.weekdays)
                    else -> dias.joinToString(", ") { numeroDiaATexto(it.toInt(), root.context) }
                }
                tvDias.text = diasTexto
                
                // Switch de activación
                switchAlarma.isChecked = alarma.activa
                switchAlarma.setOnCheckedChangeListener { _, isChecked ->
                    onSwitchChange(alarma, isChecked)
                }
                
                // Click en el item
                root.setOnClickListener {
                    onAlarmaClick(alarma)
                }
            }
        }

        /**
         * Convierte el número de día a texto
         * @param numeroDia Número del día (1-7, donde 1 es domingo)
         * @return Nombre corto del día
         */
        private fun numeroDiaATexto(numeroDia: Int, context: android.content.Context): String {
            return when (numeroDia) {
                1 -> context.getString(R.string.sunday_short)
                2 -> context.getString(R.string.monday_short)
                3 -> context.getString(R.string.tuesday_short)
                4 -> context.getString(R.string.wednesday_short)
                5 -> context.getString(R.string.thursday_short)
                6 -> context.getString(R.string.friday_short)
                7 -> context.getString(R.string.saturday_short)
                else -> ""
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmaViewHolder {
        return AlarmaViewHolder(
            ItemAlarmaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AlarmaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

/**
 * DiffUtil.ItemCallback para comparar alarmas
 * Ayuda a optimizar las actualizaciones del RecyclerView
 */
class AlarmaDiffCallback : DiffUtil.ItemCallback<Alarma>() {
    override fun areItemsTheSame(oldItem: Alarma, newItem: Alarma): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Alarma, newItem: Alarma): Boolean {
        return oldItem == newItem
    }
} 