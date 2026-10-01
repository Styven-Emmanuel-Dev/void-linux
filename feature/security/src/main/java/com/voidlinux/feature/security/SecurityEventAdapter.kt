package com.voidlinux.feature.security

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.voidlinux.feature.security.databinding.ItemSecurityEventBinding

class SecurityEventAdapter : RecyclerView.Adapter<SecurityEventAdapter.VH>() {

    private val items = mutableListOf<SecurityEvent>()

    fun submit(newItems: List<SecurityEvent>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSecurityEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class VH(private val binding: ItemSecurityEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: SecurityEvent) {
            binding.title.text = event.title
            binding.description.text = event.description

            binding.severity.text = event.severity.name
            binding.severity.setTextColor(
                when (event.severity) {
                    SecurityEvent.Severity.CRITICAL -> 0xFFFF3B3B.toInt()
                    SecurityEvent.Severity.HIGH -> 0xFFFF3B3B.toInt()
                    SecurityEvent.Severity.MEDIUM -> 0xFFFFB800.toInt()
                    SecurityEvent.Severity.LOW -> 0xFF8888A0.toInt()
                }
            )

            binding.timestamp.text = android.text.format.DateFormat.format(
                "HH:mm:ss", event.timestamp
            )
        }
    }
}