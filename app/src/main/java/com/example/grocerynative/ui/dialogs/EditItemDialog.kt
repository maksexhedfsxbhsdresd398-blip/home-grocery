package com.example.grocerynative.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.grocerynative.R
import com.example.grocerynative.data.Item
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EditItemDialog(
    private val item: Item,
    private val onSave: (name: String, qtyVal: Double, unit: String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val ctx = requireContext()
        val view = LayoutInflater.from(ctx).inflate(R.layout.dialog_edit_item, null, false)

        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etQtyVal = view.findViewById<TextInputEditText>(R.id.etQtyVal)
        val unitLayout = view.findViewById<TextInputLayout>(R.id.unitLayout)
        val autoUnit = view.findViewById<MaterialAutoCompleteTextView>(R.id.autoUnit)

        // init fields
        etName.setText(item.name)
        val parts = item.quantity.trim().split(" ")
        etQtyVal.setText(parts.getOrNull(0) ?: "1")
        autoUnit.setSimpleItems(resources.getStringArray(R.array.units_array))
        autoUnit.setText(parts.getOrNull(1) ?: getString(R.string.unit_kg), false)
        unitLayout.setEndIconOnClickListener { autoUnit.showDropDown() }
        autoUnit.setOnClickListener { autoUnit.showDropDown() }

        val dlg = AlertDialog.Builder(ctx)
            .setView(view)
            .create()

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel)
            .setOnClickListener { dlg.dismiss() }

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSave)
            .setOnClickListener {
                val name = etName.text?.toString()?.trim().orEmpty()
                val qtyVal = etQtyVal.text?.toString()?.toDoubleOrNull() ?: 0.0
                val unit = autoUnit.text?.toString()?.ifBlank { "kg" } ?: "kg"
                if (name.isNotEmpty() && qtyVal > 0) {
                    onSave(name, qtyVal, unit)
                    dlg.dismiss()
                }
            }

        return dlg
    }
}
