// 📁 viewmodel/MapViewModel.kt
package com.example.luontopeli.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.location.LocationManager
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// AndroidViewModel saa Application-kontekstin — tarvitaan LocationManagerille
class MapViewModel(application: Application) : AndroidViewModel(application) {

    // LocationManager-instanssi — käyttää Applicationin kontekstia
    private val locationManager = LocationManager(application)
    private val db = AppDatabase.getDatabase(application)

    // Delegoidaan StateFlowt suoraan LocationManagerilta
    val routePoints: StateFlow<List<GeoPoint>> = locationManager.routePoints
    val currentLocation: StateFlow<Location?> = locationManager.currentLocation

    // Luontokohteet kartalla
    private val _natureSpots = MutableStateFlow<List<NatureSpot>>(emptyList())
    val natureSpots: StateFlow<List<NatureSpot>> = _natureSpots.asStateFlow()

    init {
        // Lataa luontokohteet heti kun ViewModel luodaan
        loadNatureSpots()
    }

    fun startTracking() = locationManager.startTracking()
    fun stopTracking() = locationManager.stopTracking()
    fun resetRoute() = locationManager.resetRoute()

    private fun loadNatureSpots() {
        viewModelScope.launch {
            // Kuuntelee Room-muutoksia reaaliajassa Flow:n avulla
            db.natureSpotDao().getSpotsWithLocation().collect { spots ->
                _natureSpots.value = spots
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Pysäytä sijainnin seuranta kun ViewModel tuhotaan
        locationManager.stopTracking()
    }
}

// Apufunktio: muuntaa Long-aikaleiman luettavaksi merkkijonoksi
fun Long.toFormattedDate(): String {
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}