package com.example.grocerynative.ui.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
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
    private val onMarkPaid: (id: String, qtyVal: Double, unit: String, price: Double) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: MutableList<Item> = items.toMutableList()
    private var currentMode: Mode = mode

    override fun getItemViewType(position: Int) = if (currentMode == Mode.MANAGE) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == 0) ManageVH(inf.inflate(R.layout.item_manage_card, parent, false))
        else ShopVH(inf.inflate(R.layout.item_shopping_card, parent, false))
    }

    override fun getItemCount() = data.size
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        val item = data[pos]
        if (holder is ManageVH) holder.bind(item)
        if (holder is ShopVH) holder.bind(item)
    }

    fun update(items: List<Item>, mode: Mode) {
        currentMode = mode
        data.clear(); data.addAll(items)
        notifyDataSetChanged()
    }

    // ---------------- MANAGE ----------------
    inner class ManageVH(v: View) : RecyclerView.ViewHolder(v) {
        private val flTick: View = v.findViewById(R.id.flTick)
        private val ivTick: ImageView = v.findViewById(R.id.ivTick)
        private val tvName: TextView = v.findViewById(R.id.tvName)
        private val tvQty: TextView = v.findViewById(R.id.tvQty)
        private val btnEdit: ImageButton = v.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = v.findViewById(R.id.btnDelete)

        private fun styleTick(paid: Boolean, view: View, check: ImageView) {
            view.background = ContextCompat.getDrawable(view.context,
                if (paid) R.drawable.tick_badge_green else R.drawable.tick_badge_outline)
            check.visibility = if (paid) View.VISIBLE else View.GONE
        }

        fun bind(item: Item) {
            tvName.text = item.name.ifBlank { "(unnamed)" }
            tvQty.text = "Qty: ${item.quantity}"
            styleTick(item.purchased, flTick, ivTick)

            // Toggle by tapping the badge
            flTick.setOnClickListener { onToggle(item.id) }

            btnEdit.setOnClickListener { onEdit(item) }
            btnDelete.setOnClickListener { onDelete(item.id) }
        }
    }

    // ---------------- SHOPPING ----------------
    inner class ShopVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvName: TextView = v.findViewById(R.id.tvName)
        private val tvTotal: TextView = v.findViewById(R.id.tvTotal)
        private val btnEdit: ImageButton = v.findViewById(R.id.btnEdit)
        private val etQtyVal: TextInputEditText = v.findViewById(R.id.etQtyVal)
        private val tvUnit: TextView = v.findViewById(R.id.tvUnit)
        private val etPrice: TextInputEditText = v.findViewById(R.id.etPrice)
        private val btnMark: MaterialButton = v.findViewById(R.id.btnMark)

        private fun calcTotal(q: String, p: String): Double =
            (q.toDoubleOrNull() ?: 0.0) * (p.toDoubleOrNull() ?: 0.0)

        private fun styleButton(paid: Boolean) {
            val ctx = itemView.context
            if (paid) {
                btnMark.text = "PAID"
                btnMark.isEnabled = false
                btnMark.backgroundTintList = ContextCompat.getColorStateList(ctx, R.color.paid_green)
            } else {
                btnMark.text = "MARK"
                btnMark.isEnabled = true
                btnMark.backgroundTintList = ContextCompat.getColorStateList(ctx, R.color.mark_blue)
            }
        }

        fun bind(item: Item) {
            tvName.text = item.name

            val parts = item.quantity.trim().split(" ")
            val qText = parts.getOrNull(0) ?: "1"
            val unit = parts.getOrNull(1) ?: "kg"
            tvUnit.text = unit
            etQtyVal.setText(qText)
            etPrice.setText(if (item.actualUnitPrice == 0.0) "" else item.actualUnitPrice.toString())

            fun updateTotal() {
                val t = calcTotal(etQtyVal.text?.toString() ?: "", etPrice.text?.toString() ?: "")
                tvTotal.text = "Total: Rs. %.2f".format(t)
            }
            updateTotal()

            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { updateTotal() }
                override fun afterTextChanged(s: Editable?) {}
            }
            etQtyVal.addTextChangedListener(watcher)
            etPrice.addTextChangedListener(watcher)

            btnEdit.setOnClickListener { onEdit(item) }

            styleButton(item.purchased)
            btnMark.setOnClickListener {
                val qtyVal = etQtyVal.text?.toString()?.toDoubleOrNull() ?: 0.0
                val price = etPrice.text?.toString()?.toDoubleOrNull() ?: 0.0
                onMarkPaid(item.id, qtyVal, unit, price)   // atomic: qty + price + purchased=true
                styleButton(true)                          // instant success style
                updateTotal()
            }
        }
    }
}
