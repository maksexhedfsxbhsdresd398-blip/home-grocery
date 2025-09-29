package com.example.grocerynative.data

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class GroceryList(
    val name: String = "Shared Grocery List",
    val items: List<Item> = emptyList(),
    val ownerId: String? = null
)

@IgnoreExtraProperties
data class Item(
    val id: String = "",
    val name: String = "",
    val quantity: String = "0 item",    // e.g. "2 kg"
    val estimatedPrice: Double? = 0.0,
    val actualUnitPrice: Double = 0.0,
    val purchased: Boolean = false,     // <-- must be 'purchased'
    val addedBy: String? = null,
    val timestamp: String? = null
)
