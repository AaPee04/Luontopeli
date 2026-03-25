package com.example.luontopeli.data.remote.firebase

// 📁 data/remote/firebase/AuthManager.kt

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthManager {
    private val auth = FirebaseAuth.getInstance()

    // Nykyinen kirjautunut käyttäjä (null jos ei kirjautunut)
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val isSignedIn: Boolean
        get() = auth.currentUser != null

    // Kirjaudu sisään anonyymisti (tai palauta olemassa oleva sessio)
    suspend fun signInAnonymously(): Result<String> {
        return try {
            // Jos jo kirjautunut, palauta nykyinen UID
            val existingUser = auth.currentUser
            if (existingUser != null) {
                return Result.success(existingUser.uid)
            }

            // Luo uusi anonyymi käyttäjä
            val result = auth.signInAnonymously().await()
            val uid = result.user?.uid ?: return Result.failure(Exception("UID puuttuu"))
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}