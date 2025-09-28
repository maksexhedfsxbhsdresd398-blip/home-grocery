package com.example.grocerynative.ui.adapters

import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerynative.R
import com.example.grocerynative.data.Item
import com.example.grocerynative.ui.Mode
import com.example.grocerynative.util.splitQuantity
import com.example.grocerynative.util.normalizeToKgOrL

class ItemsAdapter(
    private var items: List<Item>,
    private var mode: Mode,
    private val onToggle: (String) -> Unit,
    private val onDelete: (String) -> Unit,
    private val onEdit: (Item) -> Unit,
    private val onPriceChange: (String, Double) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun update(newItems: List<Item>, newMode: Mode) {
        items = newItems.sortedBy { it.isPurchased }  // unpurchased first
        mode = newMode
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
    override fun getItemViewType(position: Int) = if (mode == Mode.MANAGE) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_manage, parent, false)
            ManageVH(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping, parent, false)
            ShopVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is ManageVH) holder.bind(item)
        if (holder is ShopVH) holder.bind(item)
    }

    inner class ManageVH(v: View): RecyclerView.ViewHolder(v) {
        private val name: TextView = v.findViewById(R.id.tvName)
        private val qty: TextView = v.findViewById(R.id.tvQty)
        private val edit: ImageButton = v.findViewById(R.id.btnEdit)
        private val del: ImageButton = v.findViewById(R.id.btnDelete)
        private val check: CheckBox = v.findViewById(R.id.checkPurchased)

        fun bind(i: Item) {
            name.text = i.name
            name.paint.isStrikeThruText = i.isPurchased
            qty.text = "Qty: ${i.quantity}"
            check.isChecked = i.isPurchased

            check.setOnCheckedChangeListener { _, _ -> onToggle(i.id) }
            edit.setOnClickListener { onEdit(i) }
            del.setOnClickListener { onDelete(i.id) }
        }
    }

    inner class ShopVH(v: View): RecyclerView.ViewHolder(v) {
        private val name: TextView = v.findViewById(R.id.tvName)
        private val total: TextView = v.findViewById(R.id.tvTotal)
        private val etQtyValue: EditText = v.findViewById(R.id.etQtyValue)
        private val etQtyUnit: EditText = v.findViewById(R.id.etQtyUnit)
        private val etUnitPrice: EditText = v.findViewById(R.id.etUnitPrice)
        private val btn: Button = v.findViewById(R.id.btnTogglePaid)

        fun bind(i: Item) {
            name.text = i.name
            val (v, u) = splitQuantity(i.quantity)
            etQtyValue.setText(v.toString())
            etQtyUnit.setText(u)
            etQtyValue.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            etUnitPrice.setText(String.format("%.2f", i.actualUnitPrice))

            val normalized = normalizeToKgOrL(v, u)
            val itemTotal = normalized * i.actualUnitPrice
            total.text = "Total: Rs. ${String.format("%.2f", itemTotal)}"

            btn.text = if (i.isPurchased) "PAID" else "MARK"
            btn.setOnClickListener {
                // update price first then toggle
                val price = etUnitPrice.text.toString().toDoubleOrNull() ?: 0.0
                onPriceChange(i.id, price)
                onToggle(i.id)
            }

            etUnitPrice.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val price = etUnitPrice.text.toString().toDoubleOrNull() ?: 0.0
                    onPriceChange(i.id, price)
                }
            }
        }
    }
}
