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
    val quantity: String = "0 item",          // "2 kg", "1 pack", "1 dozen"
    val estimatedPrice: Double? = 0.0,
    val actualUnitPrice: Double = 0.0,
    /** IMPORTANT: must be 'purchased' to match Firestore field */
    val purchased: Boolean = false,
    val addedBy: String? = null,
    val timestamp: String? = null
)
