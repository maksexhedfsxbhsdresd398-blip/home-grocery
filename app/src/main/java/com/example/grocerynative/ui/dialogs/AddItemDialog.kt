package com.example.grocerynative.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.grocerynative.R

class AddItemDialog(private val onAdd:(name:String, qtyVal:Double, qtyUnit:String)->Unit): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val etName: EditText = view.findViewById(R.id.etName)
        val etVal: EditText  = view.findViewById(R.id.etQtyVal)
        val etUnit: EditText = view.findViewById(R.id.etQtyUnit)

        return AlertDialog.Builder(requireContext())
            .setTitle("Add New Item")
            .setView(view)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString()
                val v = etVal.text.toString().toDoubleOrNull() ?: 0.0
                val u = etUnit.text.toString().ifBlank { "item" }
                onAdd(name, v, u)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
