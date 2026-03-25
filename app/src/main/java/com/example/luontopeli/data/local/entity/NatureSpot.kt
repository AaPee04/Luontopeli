package com.example.luontopeli.data.local.entity

// 📁 data/local/entity/NatureSpot.kt

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// NatureSpot = yksi löydetty luontokohde
// Jokainen kuvattu kasvi/luontokohde tallennetaan omana rivinä
@Entity(tableName = "nature_spots")
data class NatureSpot(
    // UUID pääavaimena (globaalisti uniikki, sopii myös Firestoreen)
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    val name: String,                        // Käyttäjän antama nimi tai automaattinen
    val latitude: Double,                    // GPS-koordinaatti
    val longitude: Double,                   // GPS-koordinaatti

    val imageLocalPath: String? = null,      // Polku laitteen tiedostoon
    val imageFirebaseUrl: String? = null,    // Firebase Storage URL (lisätään viikolla 6)

    val plantLabel: String? = null,          // ML Kitin tunnistama kasvilaji (viikko 5)
    val confidence: Float? = null,           // Tunnistuksen varmuus 0-1

    val userId: String? = null,              // Firebase Auth UID (viikko 6)
    val timestamp: Long = System.currentTimeMillis(),

    // synced = false kun vain paikallinen, true kun synkronoitu Firestoreen
    val synced: Boolean = false
)