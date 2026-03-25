package com.example.luontopeli.viewmodel

// 📁 viewmodel/CameraViewModel.kt

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.repository.NatureSpotRepository
import com.example.luontopeli.ml.ClassificationResult
import com.example.luontopeli.ml.PlantClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraViewModel(
    private val repository: NatureSpotRepository
) : ViewModel() {

    private val classifier = PlantClassifier()

    // Tunnistustulos
    private val _classificationResult = MutableStateFlow<ClassificationResult?>(null)
    val classificationResult: StateFlow<ClassificationResult?> = _classificationResult.asStateFlow()

    // Päivitetty saveCurrentSpot – ajaa ML-tunnistuksen ensin
    fun takePhotoAndClassify(context: Context, imageCapture: ImageCapture) {
        _isLoading.value = true
        viewModelScope.launch {
            // 1. Ota kuva
            val imagePath = takePhotoSuspend(context, imageCapture)
            if (imagePath == null) { _isLoading.value = false; return@launch }

            _capturedImagePath.value = imagePath

            // 2. Tunnista kasvi kuvasta
            try {
                val uri = Uri.fromFile(File(imagePath))
                val result = classifier.classify(uri, context)
                _classificationResult.value = result
            } catch (e: Exception) {
                _classificationResult.value = ClassificationResult.Error(e.message ?: "Tuntematon virhe")
            }

            _isLoading.value = false
        }
    }

    fun saveCurrentSpot() {
        val imagePath = _capturedImagePath.value ?: return
        viewModelScope.launch {
            val result = _classificationResult.value

            val spot = NatureSpot(
                name = when (result) {
                    is ClassificationResult.Success -> result.label
                    else -> "Luontolöytö"
                },
                latitude = currentLatitude,
                longitude = currentLongitude,
                imageLocalPath = imagePath,
                plantLabel = (result as? ClassificationResult.Success)?.label,
                confidence = (result as? ClassificationResult.Success)?.confidence
            )
            repository.insertSpot(spot)
            clearCapturedImage()
            _classificationResult.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        classifier.close()
    }
}}