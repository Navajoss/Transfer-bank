package com.example.fileuploaderapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.net.InetAddress

class MainActivity : AppCompatActivity() {

    // Variables para la UI
    private lateinit var statusTextView: TextView
    private lateinit var selectFileButton: Button
    private lateinit var uploadButton: Button

    // Ruta del archivo seleccionado
    private var selectedFileUri: Uri? = null

    // Cliente HTTP
    private val client = OkHttpClient.Builder().build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Asignar referencias a los elementos de la UI
        statusTextView = findViewById(R.id.statusTextView)
        selectFileButton = findViewById(R.id.selectFileButton)
        uploadButton = findViewById(R.id.uploadButton)

        // Deshabilitar el botón de subir archivo hasta que se seleccione un archivo
        uploadButton.isEnabled = false

        // Seleccionar archivo
        selectFileButton.setOnClickListener {
            selectFile()
        }

        // Subir archivo
        uploadButton.setOnClickListener {
            selectedFileUri?.let {
                uploadFile(it)
            } ?: run {
                Toast.makeText(this, "Por favor selecciona un archivo primero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para seleccionar un archivo desde el almacenamiento
    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        resultLauncher.launch(intent)
    }

    // Launcher para manejar el resultado de la selección de archivo
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            selectedFileUri = data?.data
            if (selectedFileUri != null) {
                uploadButton.isEnabled = true
                statusTextView.text = "Archivo seleccionado: ${selectedFileUri!!.lastPathSegment}"
            } else {
                Toast.makeText(this, "No se seleccionó archivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para subir el archivo al servidor
    private fun uploadFile(fileUri: Uri) {
        val file = File(fileUri.path ?: return)
        val requestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverIp = discoverServerIp()  // Descubrir la IP del servidor automáticamente
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://$serverIp:5000/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(FileUploadService::class.java)
                val response = service.uploadFile(body)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        statusTextView.text = "Archivo subido con éxito"
                    } else {
                        statusTextView.text = "Error al subir archivo"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusTextView.text = "Error: ${e.message}"
                }
            }
        }
    }

    // Función para descubrir la IP del servidor en la red
    private fun discoverServerIp(): String {
        // Implementación para descubrir el servidor
        // Puedes agregar la lógica para hacer el descubrimiento mDNS o NetBIOS
        // Por ahora, puedes poner manualmente la IP para pruebas:
        return "192.168.0.100"  // Cambiar según tu configuración
    }
}


