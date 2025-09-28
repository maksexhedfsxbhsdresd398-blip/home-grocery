package com.example.grocerynative.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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
                val data = snap.toObject(GroceryList::class.java) ?: GroceryList()
                onUpdate(data)
            } else {
                // initialize if missing
                docRef.set(
                    mapOf(
                        "name" to "Shared Grocery List",
                        "items" to emptyList<Item>(),
                        "lastUpdated" to FieldValue.serverTimestamp(),
                        "ownerId" to auth.currentUser?.uid
                    )
                )
            }
        }

    suspend fun replaceItems(items: List<Item>) {
        docRef.update(
            mapOf(
                "items" to items,
                "lastUpdated" to FieldValue.serverTimestamp(),
                "ownerId" to auth.currentUser?.uid
            )
        ).await()
    }

    suspend fun addItem(item: Item) {
        val list = docRef.get().await().toObject(GroceryList::class.java) ?: GroceryList()
        replaceItems(list.items.toMutableList().apply { add(0, item) })
    }

    suspend fun clearAll() {
        replaceItems(emptyList())
    }
}
