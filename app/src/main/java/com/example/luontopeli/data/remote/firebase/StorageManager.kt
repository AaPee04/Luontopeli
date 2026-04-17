package com.example.luontopeli.data.remote.firebase

// 📁 data/remote/firebase/StorageManager.kt

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class StorageManager @Inject constructor() {
    private val storage = FirebaseStorage.getInstance()

    // Lataa kuva Storageen ja palauta julkinen URL
    suspend fun uploadImage(localFilePath: String, spotId: String): Result<String> {
        return try {
            val file = Uri.fromFile(File(localFilePath))

            // Tallennuspolku: spots/{spotId}/image.jpg
            val storageRef = storage.reference
                .child("spots")
                .child(spotId)
                .child("image.jpg")

            // Lataa tiedosto
            storageRef.putFile(file).await()

            // Hae julkinen URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Poista kuva Storagesta
    suspend fun deleteImage(spotId: String): Result<Unit> {
        return try {
            storage.reference.child("spots/${spotId}/image.jpg")
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}