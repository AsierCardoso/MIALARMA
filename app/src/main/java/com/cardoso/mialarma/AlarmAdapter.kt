package com.cardoso.mialarma

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlarmaAdapter(private val alarmas: List<Alarma>) : RecyclerView.Adapter<AlarmaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHora: TextView = view.findViewById(R.id.tvHora)
        val switch: Switch = view.findViewById(R.id.switchAlarma)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarma, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alarma = alarmas[position]
        holder.tvHora.text = alarma.hora
        holder.switch.isChecked = alarma.activa
    }

    override fun getItemCount() = alarmas.size
}