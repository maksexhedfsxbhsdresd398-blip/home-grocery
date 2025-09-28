package com.example.grocerynative

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.grocerynative.data.FirestoreRepository
import com.example.grocerynative.databinding.ActivityMainBinding
import com.example.grocerynative.ui.GroceryViewModel
import com.example.grocerynative.ui.Mode
import com.example.grocerynative.ui.adapters.ItemsAdapter
import com.example.grocerynative.ui.dialogs.ConfirmClearDialog
import com.example.grocerynative.ui.dialogs.ConfirmDeleteDialog
import com.example.grocerynative.ui.dialogs.EditItemDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ItemsAdapter

    private val viewModel: GroceryViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return GroceryViewModel(
                    FirestoreRepository(
                        FirebaseAuth.getInstance(),
                        FirebaseFirestore.getInstance()
                    )
                ) as T
            }
        }
    }

    private var etName: TextInputEditText? = null
    private var etQtyVal: TextInputEditText? = null
    private var autoUnit: MaterialAutoCompleteTextView? = null
    private var unitLayout: TextInputLayout? = null
    private var btnAddItem: MaterialButton? = null
    private var btnClearAllHeader: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        adapter = ItemsAdapter(
            items = emptyList(),
            mode = Mode.MANAGE,
            onToggle = { id -> viewModel.togglePurchased(id) },
            onDelete = { id ->
                ConfirmDeleteDialog { viewModel.deleteItem(id) }
                    .show(supportFragmentManager, "del")
            },
            onEdit = { item ->
                EditItemDialog(item) { name, v, u ->
                    viewModel.editItem(item.id, name, v, u)
                }.show(supportFragmentManager, "edit")
            },
            onPriceChange = { id, price -> viewModel.updateUnitPrice(id, price) }
        )
        binding.recycler.adapter = adapter

        setupTopViews()

        binding.btnManage.setOnClickListener { viewModel.switchMode(Mode.MANAGE) }
        binding.btnShopping.setOnClickListener { viewModel.switchMode(Mode.SHOPPING) }
        applyNavStyle(activeManage = true)

        lifecycleScope.launch {
            viewModel.mode.collectLatest { m ->
                setTopForMode(m)
                adapter.update(viewModel.state.value.items, m)
                applyNavStyle(activeManage = (m == Mode.MANAGE))
            }
        }
        lifecycleScope.launch {
            viewModel.state.collectLatest { list ->
                adapter.update(list.items, viewModel.mode.value)
                updateShoppingSummary()
                binding.emptyState.visibility =
                    if (list.items.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewModel.start()
    }

    private fun setupTopViews() {
        val manage = layoutInflater.inflate(R.layout.manage_top, binding.topContainer, false)
        etName = manage.findViewById(R.id.etName)
        etQtyVal = manage.findViewById(R.id.etQtyVal)
        autoUnit = manage.findViewById(R.id.autoUnit)
        unitLayout = manage.findViewById(R.id.unitLayout)
        btnAddItem = manage.findViewById(R.id.btnAddItem)
        btnClearAllHeader = manage.findViewById(R.id.btnClearAll)

        autoUnit?.setSimpleItems(resources.getStringArray(R.array.units_array))
        autoUnit?.setText(getString(R.string.unit_kg), false)
        autoUnit?.setOnClickListener { autoUnit?.showDropDown() }
        autoUnit?.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) autoUnit?.showDropDown() }
        unitLayout?.setEndIconOnClickListener { autoUnit?.showDropDown() }

        btnAddItem?.setOnClickListener {
            val name = etName?.text?.toString()?.trim().orEmpty()
            val qty = etQtyVal?.text?.toString()?.toDoubleOrNull() ?: 0.0
            val unit = autoUnit?.text?.toString()?.ifBlank { "item" } ?: "item"
            if (name.isNotEmpty() && qty > 0) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                viewModel.addItem(name, qty, unit, uid)
                etName?.setText("")
                etQtyVal?.setText("")
                autoUnit?.setText(getString(R.string.unit_kg), false)
                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Enter name and value", Toast.LENGTH_SHORT).show()
            }
        }

        btnClearAllHeader?.setOnClickListener {
            ConfirmClearDialog { viewModel.clearAll() }.show(supportFragmentManager, "clear")
        }

        binding.topContainer.addView(manage, 0)

        val shopping = layoutInflater.inflate(android.R.layout.simple_list_item_2, binding.topContainer, false)
        shopping.id = View.generateViewId()
        binding.topContainer.addView(shopping, 1)
    }

    private fun setTopForMode(mode: Mode) {
        binding.topContainer.getChildAt(0).visibility =
            if (mode == Mode.MANAGE) View.VISIBLE else View.GONE
        binding.topContainer.getChildAt(1).visibility =
            if (mode == Mode.SHOPPING) View.VISIBLE else View.GONE
        if (mode == Mode.SHOPPING) updateShoppingSummary()
    }

    private fun updateShoppingSummary() {
        val shopping = binding.topContainer.getChildAt(1)
        val title = shopping.findViewById<TextView>(android.R.id.text1)
        val subtitle = shopping.findViewById<TextView>(android.R.id.text2)
        title.text = "Shopping Cart Summary"
        subtitle.text = "Actual Paid: Rs. %.2f".format(viewModel.actualTotalRs())
    }

    private fun applyNavStyle(activeManage: Boolean) {
        val blue = ContextCompat.getColor(this, R.color.primary_blue)
        val white = ContextCompat.getColor(this, android.R.color.white)
        val grayText = ContextCompat.getColor(this, R.color.gray_600)
        fun style(btn: MaterialButton, bg: Int, fg: Int) {
            btn.backgroundTintList = ColorStateList.valueOf(bg)
            btn.setTextColor(fg)
        }
        if (activeManage) {
            style(binding.btnManage, blue, white)
            style(binding.btnShopping, white, grayText)
        } else {
            style(binding.btnManage, white, grayText)
            style(binding.btnShopping, blue, white)
        }
    }
}
