package com.example.luontopeli.viewmodel

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: NatureSpotRepository
) : ViewModel() {

    private val classifier = PlantClassifier()

    var currentLatitude: Double = 0.0
    var currentLongitude: Double = 0.0

    private val _capturedImagePath = MutableStateFlow<String?>(null)
    val capturedImagePath: StateFlow<String?> = _capturedImagePath

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _classificationResult = MutableStateFlow<ClassificationResult?>(null)
    val classificationResult: StateFlow<ClassificationResult?> = _classificationResult

    fun takePhoto(context: Context, imageCapture: ImageCapture) {
        takePhotoAndClassify(context, imageCapture)
    }

    fun takePhotoAndClassify(context: Context, imageCapture: ImageCapture) {
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imagePath = takePhotoSuspend(context, imageCapture)
                _capturedImagePath.value = imagePath

                try {
                    val uri = Uri.fromFile(File(imagePath))
                    val result = classifier.classify(uri, context)
                    _classificationResult.value = result
                } catch (e: Exception) {
                    _classificationResult.value =
                        ClassificationResult.Error(e.message ?: "Tuntematon virhe")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun takePhotoSuspend(
        context: Context,
        imageCapture: ImageCapture
    ): String = suspendCancellableCoroutine { cont ->

        val outputDir = context.cacheDir
        val fileName = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date()) + ".jpg"

        val file = File(outputDir, fileName)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                    if (cont.isActive) {
                        cont.resume(file.absolutePath)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    if (cont.isActive) {
                        cont.resumeWithException(exception)
                    }
                }
            }
        )
    }

    fun clearCapturedImage() {
        _capturedImagePath.value = null
        _classificationResult.value = null
    }

    fun saveCurrentSpot() {
        val imagePath = _capturedImagePath.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val result = _classificationResult.value

            val spot = NatureSpot(
                name = (result as? ClassificationResult.Success)?.label ?: "Luontolöytö",
                latitude = currentLatitude,
                longitude = currentLongitude,
                imageLocalPath = imagePath,
                plantLabel = (result as? ClassificationResult.Success)?.label,
                confidence = (result as? ClassificationResult.Success)?.confidence
            )

            repository.insertSpot(spot)
            clearCapturedImage()
        }
    }

    override fun onCleared() {
        classifier.close()
        super.onCleared()
    }
}
