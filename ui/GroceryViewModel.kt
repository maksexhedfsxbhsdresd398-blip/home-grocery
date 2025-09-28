package com.example.grocerynative.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerynative.data.FirestoreRepository
import com.example.grocerynative.data.GroceryList
import com.example.grocerynative.data.Item
import com.example.grocerynative.util.normalizeToKgOrL
import com.example.grocerynative.util.splitQuantity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class Mode { MANAGE, SHOPPING }

class GroceryViewModel(private val repo: FirestoreRepository): ViewModel() {

    private val _mode = MutableStateFlow(Mode.MANAGE)
    val mode: StateFlow<Mode> = _mode

    private val _state = MutableStateFlow(GroceryList())
    val state: StateFlow<GroceryList> = _state

    fun start() = viewModelScope.launch {
        repo.ensureAuth()
        repo.listen(
            onUpdate = { _state.value = it },
            onError = { /* could expose a snackbar state */ }
        )
    }

    fun switchMode(m: Mode) { _mode.value = m }

    fun addItem(name: String, qtyValue: Double, qtyUnit: String, userId: String?) = viewModelScope.launch {
        val item = Item(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            quantity = "${qtyValue} ${qtyUnit}",
            estimatedPrice = 0.0,
            actualUnitPrice = 0.0,
            isPurchased = false,
            addedBy = userId,
            timestamp = java.time.Instant.now().toString()
        )
        repo.addItem(item)
    }

    fun deleteItem(id: String) = viewModelScope.launch {
        val newItems = _state.value.items.filterNot { it.id == id }
        repo.replaceItems(newItems)
    }

    fun clearAll() = viewModelScope.launch { repo.clearAll() }

    fun togglePurchased(id: String) = viewModelScope.launch {
        val newItems = _state.value.items.map {
            if (it.id == id) it.copy(isPurchased = !it.isPurchased) else it
        }
        repo.replaceItems(newItems)
    }

    fun updateUnitPrice(id: String, price: Double) = viewModelScope.launch {
        val newItems = _state.value.items.map {
            if (it.id == id) it.copy(actualUnitPrice = price) else it
        }
        repo.replaceItems(newItems)
    }

    fun editItem(id: String, newName: String, newQtyVal: Double, newQtyUnit: String) = viewModelScope.launch {
        val newItems = _state.value.items.map {
            if (it.id == id) it.copy(name = newName.trim(), quantity = "$newQtyVal $newQtyUnit") else it
        }
        repo.replaceItems(newItems)
    }

    fun actualTotalRs(): Double {
        return _state.value.items.filter { it.isPurchased }.sumOf { item ->
            val (v, u) = splitQuantity(item.quantity)
            val normalized = normalizeToKgOrL(v, u)
            normalized * (item.actualUnitPrice)
        }
    }
}
