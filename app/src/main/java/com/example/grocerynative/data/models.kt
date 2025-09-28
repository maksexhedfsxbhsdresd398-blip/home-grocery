package com.example.grocerynative.data

data class GroceryList(
    val name: String = "Shared Grocery List",
    val items: List<Item> = emptyList(),
    val ownerId: String? = null
)

data class Item(
    val id: String = "",
    val name: String = "",
    val quantity: String = "0 item",          // "value unit" (e.g., "2 kg", "500 g")
    val estimatedPrice: Double? = 0.0,
    val actualUnitPrice: Double = 0.0,
    val isPurchased: Boolean = false,
    val addedBy: String? = null,
    val timestamp: String? = null
)
