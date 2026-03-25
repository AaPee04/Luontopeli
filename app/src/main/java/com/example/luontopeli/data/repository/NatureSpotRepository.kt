package com.example.luontopeli.data.repository

// 📁 data/repository/NatureSpotRepository.kt

import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.entity.NatureSpot
import kotlinx.coroutines.flow.Flow

class NatureSpotRepository(private val dao: NatureSpotDao) {

    val allSpots: Flow<List<NatureSpot>> = dao.getAllSpots()
    val spotsWithLocation: Flow<List<NatureSpot>> = dao.getSpotsWithLocation()

    suspend fun insertSpot(spot: NatureSpot) {
        dao.insert(spot)
    }

    // Päivitä ML-tunnistustulos (lisätään viikolla 5)
    suspend fun updatePlantLabel(id: String, label: String, confidence: Float) {
        dao.insert(  // REPLACE strategia päivittää olemassa olevan
            NatureSpot(id = id, name = label, latitude = 0.0, longitude = 0.0,
                plantLabel = label, confidence = confidence)
        )
    }

    suspend fun deleteSpot(spot: NatureSpot) {
        dao.delete(spot)
    }

    suspend fun getUnsyncedSpots(): List<NatureSpot> {
        return dao.getUnsyncedSpots()
    }
}