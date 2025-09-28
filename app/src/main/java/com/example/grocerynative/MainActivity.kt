package com.example.grocerynative

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.grocerynative.data.FirestoreRepository
import com.example.grocerynative.data.Item
import com.example.grocerynative.databinding.ActivityMainBinding
import com.example.grocerynative.ui.GroceryViewModel
import com.example.grocerynative.ui.Mode
import com.example.grocerynative.ui.adapters.ItemsAdapter
import com.example.grocerynative.ui.dialogs.AddItemDialog
import com.example.grocerynative.ui.dialogs.ConfirmClearDialog
import com.example.grocerynative.ui.dialogs.ConfirmDeleteDialog
import com.example.grocerynative.ui.dialogs.EditItemDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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

    private lateinit var adapter: ItemsAdapter

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

        // Top: manage form vs shopping summary
        setupTopViews()

        // Bottom nav
        binding.btnManage.setOnClickListener { viewModel.switchMode(Mode.MANAGE) }
        binding.btnShopping.setOnClickListener { viewModel.switchMode(Mode.SHOPPING) }

        lifecycleScope.launch {
            viewModel.mode.collectLatest { m ->
                setTopForMode(m)
                adapter.update(viewModel.state.value.items, m)
            }
        }
        lifecycleScope.launch {
            viewModel.state.collectLatest { list ->
                adapter.update(list.items, viewModel.mode.value)
                updateShoppingSummary()
            }
        }

        viewModel.start()
    }

    private fun setupTopViews() {
        // Inflate manage form (reusing dialog layout as a simple inline form)
        val manage = layoutInflater.inflate(R.layout.dialog_add_item, binding.topContainer, false)

        // Add "Clear All" button
        val clear = com.google.android.material.button.MaterialButton(this).apply {
            text = "Clear All"
            setOnClickListener {
                ConfirmClearDialog { viewModel.clearAll() }
                    .show(supportFragmentManager, "clear")
            }
        }
        (manage as LinearLayout).addView(clear)

        binding.topContainer.addView(manage, 0)

        // Inflate shopping summary using a simple built-in layout (two lines)
        val shopping = layoutInflater.inflate(android.R.layout.simple_list_item_2, binding.topContainer, false)
        shopping.id = View.generateViewId()
        binding.topContainer.addView(shopping, 1)
    }

    private fun setTopForMode(mode: Mode) {
        binding.topContainer.getChildAt(0).visibility =
            if (mode == Mode.MANAGE) View.VISIBLE else View.GONE
        binding.topContainer.getChildAt(1).visibility =
            if (mode == Mode.SHOPPING) View.VISIBLE else View.GONE

        if (mode == Mode.MANAGE) {
            AddItemDialog { name, v, u ->
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                viewModel.addItem(name, v, u, uid)
            }.show(supportFragmentManager, "add")
        } else {
            updateShoppingSummary()
        }
    }

    private fun updateShoppingSummary() {
        val shopping = binding.topContainer.getChildAt(1)
        val title = shopping.findViewById<TextView>(android.R.id.text1)
        val subtitle = shopping.findViewById<TextView>(android.R.id.text2)
        title.text = "Shopping Cart Summary"
        subtitle.text = "Actual Paid: Rs. %.2f".format(viewModel.actualTotalRs())
    }
}
