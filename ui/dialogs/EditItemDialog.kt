package com.example.grocerynative.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.grocerynative.R
import com.example.grocerynative.data.Item
import com.example.grocerynative.util.splitQuantity

class EditItemDialog(
    private val item: Item,
    private val onSave:(newName:String, newVal:Double, newUnit:String)->Unit
): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val etName: EditText = view.findViewById(R.id.etName)
        val etVal: EditText  = view.findViewById(R.id.etQtyVal)
        val etUnit: EditText = view.findViewById(R.id.etQtyUnit)

        etName.setText(item.name)
        val (v, u) = splitQuantity(item.quantity)
        etVal.setText(v.toString())
        etUnit.setText(u)

        return AlertDialog.Builder(requireContext())
            .setTitle("Edit Item")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString()
                val newV = etVal.text.toString().toDoubleOrNull() ?: 0.0
                val newU = etUnit.text.toString().ifBlank { "item" }
                onSave(name, newV, newU)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
