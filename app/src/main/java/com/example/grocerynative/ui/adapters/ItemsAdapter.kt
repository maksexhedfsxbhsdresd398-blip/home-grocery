package com.example.grocerynative.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerynative.R
import com.example.grocerynative.data.Item
import com.example.grocerynative.ui.Mode
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ItemsAdapter(
    items: List<Item>,
    mode: Mode,
    private val onToggle: (String) -> Unit,
    private val onDelete: (String) -> Unit,
    private val onEdit: (Item) -> Unit,
    private val onPriceChange: (String, Double) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: MutableList<Item> = items.toMutableList()
    private var currentMode: Mode = mode

    override fun getItemViewType(position: Int): Int =
        if (currentMode == Mode.MANAGE) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            val v = inf.inflate(R.layout.item_manage_card, parent, false)
            ManageVH(v)
        } else {
            val v = inf.inflate(R.layout.item_shopping_card, parent, false)
            ShopVH(v)
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        if (holder is ManageVH) holder.bind(item)
        if (holder is ShopVH) holder.bind(item)
    }

    fun update(items: List<Item>, mode: Mode) {
        currentMode = mode
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    inner class ManageVH(v: View) : RecyclerView.ViewHolder(v) {
        private val ivCheck: ImageView = v.findViewById(R.id.ivCheck)
        private val tvName: TextView = v.findViewById(R.id.tvName)
        private val tvQty: TextView = v.findViewById(R.id.tvQty)
        private val btnEdit: ImageButton = v.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = v.findViewById(R.id.btnDelete)

        fun bind(item: Item) {
            tvName.text = item.name.ifBlank { "(unnamed)" }
            tvQty.text = "Qty: ${item.quantity}"
            ivCheck.setImageResource(
                if (item.isPurchased) android.R.drawable.checkbox_on_background
                else android.R.drawable.checkbox_off_background
            )
            ivCheck.setOnClickListener { onToggle(item.id) }
            btnEdit.setOnClickListener { onEdit(item) }
            btnDelete.setOnClickListener { onDelete(item.id) }
        }
    }

    inner class ShopVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvName: TextView = v.findViewById(R.id.tvName)
        private val tvTotal: TextView = v.findViewById(R.id.tvTotal)
        private val btnEdit: ImageButton = v.findViewById(R.id.btnEdit)
        private val etQtyVal: TextInputEditText = v.findViewById(R.id.etQtyVal)
        private val tvUnit: TextView = v.findViewById(R.id.tvUnit)
        private val etPrice: TextInputEditText = v.findViewById(R.id.etPrice)
        private val btnMark: MaterialButton = v.findViewById(R.id.btnMark)

        fun bind(item: Item) {
            tvName.text = item.name
            // parse "3 kg"
            val parts = item.quantity.trim().split(" ")
            val qty = parts.getOrNull(0) ?: "1"
            val unit = parts.getOrNull(1) ?: "kg"
            etQtyVal.setText(qty)
            tvUnit.text = unit
            etPrice.setText(if (item.actualUnitPrice == 0.0) "" else item.actualUnitPrice.toString())

            // Total = qty * unit price (simple)
            val total = (etQtyVal.text?.toString()?.toDoubleOrNull() ?: 0.0) *
                    (etPrice.text?.toString()?.toDoubleOrNull() ?: 0.0)
            tvTotal.text = "Total: Rs. %.2f".format(total)

            btnEdit.setOnClickListener { onEdit(item) }

            btnMark.text = if (item.isPurchased) "MARKED" else "MARK"
            btnMark.isEnabled = true
            btnMark.setOnClickListener {
                val price = etPrice.text?.toString()?.toDoubleOrNull() ?: 0.0
                onPriceChange(item.id, price)
                onToggle(item.id)
            }
        }
    }
}
