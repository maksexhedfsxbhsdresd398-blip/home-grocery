package com.example.grocerynative.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ConfirmDeleteDialog(private val onOk:()->Unit): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Delete this item permanently?")
            .setPositiveButton("Delete") {_,_-> onOk() }
            .setNegativeButton("Cancel", null)
            .create()
}
