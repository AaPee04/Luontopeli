package com.example.luontopeli.data.remote.firebase

// 📁 data/remote/firebase/FirestoreManager.kt

import com.example.luontopeli.data.local.entity.NatureSpot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val spotsCollection = db.collection("nature_spots")

    // Tallenna NatureSpot Firestoreen
    suspend fun saveSpot(spot: NatureSpot): Result<Unit> {
        return try {
            val data = mapOf(
                "id" to spot.id,
                "name" to spot.name,
                "latitude" to spot.latitude,
                "longitude" to spot.longitude,
                "plantLabel" to spot.plantLabel,
                "confidence" to spot.confidence,
                "imageFirebaseUrl" to spot.imageFirebaseUrl,
                "userId" to spot.userId,
                "timestamp" to spot.timestamp
            )
            // Dokumentin ID = NatureSpot.id (UUID)
            spotsCollection.document(spot.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Tarkkaile käyttäjän kohteiden muutoksia reaaliajassa (Flow)
    fun getUserSpots(userId: String): Flow<List<NatureSpot>> = callbackFlow {
        val listener = spotsCollection
            .whereEqualTo("userId", userId)   // Vain tämän käyttäjän kohteet
            .orderBy("timestamp",             // Uusimmat ensin
                com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val spots = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        NatureSpot(
                            id = doc.getString("id") ?: return@mapNotNull null,
                            name = doc.getString("name") ?: "",
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            plantLabel = doc.getString("plantLabel"),
                            confidence = doc.getDouble("confidence")?.toFloat(),
                            imageFirebaseUrl = doc.getString("imageFirebaseUrl"),
                            userId = doc.getString("userId"),
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            synced = true
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(spots)
            }
        // Poistetaan kuuntelija kun Flow suljetaan
        awaitClose { listener.remove() }
    }
}