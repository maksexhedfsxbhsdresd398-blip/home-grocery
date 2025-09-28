package com.example.grocerynative.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerynative.data.Item
import com.example.grocerynative.ui.Mode

class ItemsAdapter(
    items: List<Item>,
    mode: Mode,
    private val onToggle: (String) -> Unit,
    private val onDelete: (String) -> Unit,
    private val onEdit: (Item) -> Unit,
    private val onPriceChange: (String, Double) -> Unit
) : RecyclerView.Adapter<ItemsAdapter.VH>() {

    private var data: MutableList<Item> = items.toMutableList()
    private var currentMode: Mode = mode

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(android.R.id.text1)
        val subtitle: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        holder.title.text = item.name.ifBlank { "(unnamed)" }
        holder.subtitle.text = item.quantity
        // (Optional) you can attach clicks here later for edit/delete/toggle
    }

    override fun getItemCount(): Int = data.size

    fun update(items: List<Item>, mode: Mode) {
        currentMode = mode
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }
}
