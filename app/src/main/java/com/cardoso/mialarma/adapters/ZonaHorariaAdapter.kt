package com.cardoso.mialarma.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cardoso.mialarma.R
import com.cardoso.mialarma.data.ZonaHoraria
import java.text.SimpleDateFormat
import java.util.*

class ZonaHorariaAdapter(
    private val onZonaHorariaClick: (ZonaHoraria) -> Unit,
    private val onZonaHorariaLongClick: (ZonaHoraria) -> Boolean
) : ListAdapter<ZonaHoraria, ZonaHorariaAdapter.ZonaHorariaViewHolder>(ZonaHorariaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZonaHorariaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_zona_horaria, parent, false)
        return ZonaHorariaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ZonaHorariaViewHolder, position: Int) {
        val zonaHoraria = getItem(position)
        holder.bind(zonaHoraria)
    }

    inner class ZonaHorariaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCiudad: TextView = itemView.findViewById(R.id.tvCiudad)
        private val tvHora: TextView = itemView.findViewById(R.id.tvHora)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onZonaHorariaClick(getItem(position))
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onZonaHorariaLongClick(getItem(position))
                } else {
                    false
                }
            }
        }

        fun bind(zonaHoraria: ZonaHoraria) {
            tvCiudad.text = zonaHoraria.nombre
            
            val timeZone = TimeZone.getTimeZone(zonaHoraria.id)
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.timeZone = timeZone
            tvHora.text = sdf.format(Date())
        }
    }
}

class ZonaHorariaDiffCallback : DiffUtil.ItemCallback<ZonaHoraria>() {
    override fun areItemsTheSame(oldItem: ZonaHoraria, newItem: ZonaHoraria): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ZonaHoraria, newItem: ZonaHoraria): Boolean {
        return oldItem == newItem
    }
} 