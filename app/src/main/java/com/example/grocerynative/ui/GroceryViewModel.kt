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
import kotlinx.coroutines.flow.update
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
            onUpdate = { incoming -> _state.value = incoming },
            onError = { /* surface if needed */ }
        )
    }

    fun switchMode(m: Mode) { _mode.value = m }

    fun addItem(name: String, qtyValue: Double, qtyUnit: String, userId: String?) {
        val item = Item(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            quantity = "$qtyValue $qtyUnit",
            estimatedPrice = 0.0,
            actualUnitPrice = 0.0,
            isPurchased = false,
            addedBy = userId,
            timestamp = java.time.Instant.now().toString()
        )
        _state.update { it.copy(items = listOf(item) + it.items) }
        viewModelScope.launch { repo.addItem(item) }
    }

    fun deleteItem(id: String) = viewModelScope.launch {
        val newItems = _state.value.items.filterNot { it.id == id }
        _state.value = _state.value.copy(items = newItems)
        repo.replaceItems(newItems)
    }

    fun clearAll() = viewModelScope.launch {
        _state.value = _state.value.copy(items = emptyList())
        repo.clearAll()
    }

    fun togglePurchased(id: String) = viewModelScope.launch {
        val newItems = _state.value.items.map {
            if (it.id == id) it.copy(isPurchased = !it.isPurchased) else it
        }
        _state.value = _state.value.copy(items = newItems)
        repo.replaceItems(newItems)
    }

    fun updateUnitPrice(id: String, price: Double) = viewModelScope.launch {
        val newItems = _state.value.items.map {
            if (it.id == id) it.copy(actualUnitPrice = price) else it
        }
        _state.value = _state.value.copy(items = newItems)
        repo.replaceItems(newItems)
    }

    /** ✅ Atomic: set quantity + price + purchased in one write (fixes your MARK issue) */
    fun markItemPaid(id: String, qtyVal: Double, unit: String, price: Double) = viewModelScope.launch {
        val newItems = _state.value.items.map {
            if (it.id == id) it.copy(
                quantity = "$qtyVal $unit",
                actualUnitPrice = price,
                isPurchased = true
            ) else it
        }
        _state.value = _state.value.copy(items = newItems)
        repo.replaceItems(newItems)
    }

    fun editItem(id: String, newName: String, newQtyVal: Double, newQtyUnit: String) = viewModelScope.launch {
        val newItems = _state.value.items.map {
            if (it.id == id) it.copy(name = newName.trim(), quantity = "$newQtyVal $newQtyUnit") else it
        }
        _state.value = _state.value.copy(items = newItems)
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
