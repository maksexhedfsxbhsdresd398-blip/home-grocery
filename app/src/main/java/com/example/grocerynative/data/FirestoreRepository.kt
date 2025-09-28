package com.example.grocerynative.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private val docRef = db.document("groceryLists/shared_list")

    suspend fun ensureAuth(): String {
        val user = auth.currentUser ?: auth.signInAnonymously().await().user
        return requireNotNull(user).uid
    }

    fun listen(onUpdate: (GroceryList) -> Unit, onError: (Exception) -> Unit) =
        docRef.addSnapshotListener { snap, e ->
            if (e != null) { onError(e); return@addSnapshotListener }
            if (snap != null && snap.exists()) {
                onUpdate(snap.toObject(GroceryList::class.java) ?: GroceryList())
            } else {
                // Initialize document if it's missing
                docRef.set(
                    mapOf(
                        "name" to "Shared Grocery List",
                        "items" to emptyList<Item>(),
                        "lastUpdated" to FieldValue.serverTimestamp(),
                        "ownerId" to auth.currentUser?.uid
                    ),
                    SetOptions.merge()
                )
            }
        }

    suspend fun replaceItems(items: List<Item>) {
        ensureAuth()
        docRef.set(
            mapOf(
                "items" to items,
                "lastUpdated" to FieldValue.serverTimestamp(),
                "ownerId" to auth.currentUser?.uid
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun addItem(item: Item) {
        ensureAuth()
        val existing = docRef.get().await().toObject(GroceryList::class.java)
        val newItems = (existing?.items ?: emptyList()).toMutableList().apply { add(0, item) }
        replaceItems(newItems)
    }

    suspend fun clearAll() {
        replaceItems(emptyList())
    }
}
