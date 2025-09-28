package com.example.grocerynative.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ConfirmClearDialog(private val onOk:()->Unit): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Clear List")
            .setMessage("Delete ALL items permanently?")
            .setPositiveButton("Clear") {_,_-> onOk() }
            .setNegativeButton("Cancel", null)
            .create()
}
