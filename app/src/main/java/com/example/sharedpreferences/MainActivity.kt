package com.example.sharedpreferences

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.sharedpreferences.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity(), LocationListener {

    // Variables para el manejo de ubicación
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    // Variables para el tiempo de uso
    private var startTime: Long = 0
    private var totalUsageTime: Long = 0
    private var isFirstRun = true

    // Variables para mantener el estado de la aplicación
    private var userName by mutableStateOf("")
    private var isDarkMode by mutableStateOf(false)
    private var selectedLanguageIndex by mutableIntStateOf(0)
    private var notificationVolume by mutableIntStateOf(50)
    private var lastAccess by mutableStateOf("Nunca")
    private var lastLocation by mutableStateOf("Desconocida")
    private var totalUsageTimeText by mutableStateOf("00:00:00")

    // Clave maestra para encriptación
    private val masterKeyAlias by lazy {
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    }

    // SharedPreferences encriptadas
    private val encryptedSharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            "encrypted_user_prefs",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargar preferencias guardadas
        loadPreferences()

        // Registrar la hora de inicio para calcular el tiempo de uso
        startTime = System.currentTimeMillis()

        // Inicializar el LocationManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setContent {
            // Aplicar tema según la preferencia de modo oscuro
            AppTheme(darkTheme = isDarkMode) {
                UserPreferencesScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UserPreferencesScreen() {
        val context = LocalContext.current

        // Lista de idiomas disponibles
        val languages = listOf("Español", "English", "Français", "Deutsch", "Italiano")

        // Estado para el TextField del nombre de usuario
        var userNameState by remember { mutableStateOf(userName) }

        // Estado para el Switch de modo oscuro
        var darkModeState by remember { mutableStateOf(isDarkMode) }

        // Estado para el dropdown de idiomas
        var expandedDropdown by remember { mutableStateOf(false) }

        // Estado para el slider de volumen
        var volumeState by remember { mutableFloatStateOf(notificationVolume.toFloat()) }

        // Solicitar permiso de ubicación usando el enfoque moderno de Compose
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permiso concedido, obtener ubicación
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    try {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            5000,
                            5f,
                            this@MainActivity
                        )
                    } catch (e: SecurityException) {
                        Toast.makeText(context, "Error al obtener ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(
                    context,
                    "Permiso de ubicación denegado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Efecto para solicitar permisos al iniciar
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        5f,
                        this@MainActivity
                    )
                } catch (e: SecurityException) {
                    Toast.makeText(context, "Error al obtener ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Text(
                    text = "Perfil de Usuario (Datos Encriptados)",
                    fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Nombre de usuario
                Text(text = "Nombre de usuario:")
                TextField(
                    value = userNameState,
                    onValueChange = { userNameState = it },
                    modifier = Modifier.fillMaxWidth()
                )

                // Tema oscuro
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Tema oscuro:")
                    Switch(
                        checked = darkModeState,
                        onCheckedChange = {
                            darkModeState = it
                        }
                    )
                }

                // Idioma preferido
                Text(text = "Idioma preferido:")
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = languages[selectedLanguageIndex],
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        languages.forEachIndexed { index, language ->
                            DropdownMenuItem(
                                text = { Text(language) },
                                onClick = {
                                    selectedLanguageIndex = index
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                // Volumen de notificaciones
                Text(text = "Volumen de notificaciones: ${volumeState.toInt()}%")
                Slider(
                    value = volumeState,
                    onValueChange = { volumeState = it },
                    valueRange = 0f..100f,
                    steps = 100,
                    modifier = Modifier.fillMaxWidth()
                )

                // Información sobre último acceso
                Text(text = "Último acceso: $lastAccess")

                // Información sobre última ubicación
                Text(text = "Última ubicación: $lastLocation")

                // Información sobre tiempo total de uso
                Text(text = "Tiempo total de uso: $totalUsageTimeText")

                // Botón para guardar preferencias
                Button(
                    onClick = {
                        // Actualizar variables
                        userName = userNameState
                        isDarkMode = darkModeState
                        notificationVolume = volumeState.toInt()
                        // Guardar preferencias
                        savePreferences()
                        Toast.makeText(
                            context,
                            "Preferencias guardadas de forma segura",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar preferencias")
                }
            }
        }
    }

    private fun loadPreferences() {
        try {
            // Cargar nombre de usuario
            userName = encryptedSharedPreferences.getString("userName", "") ?: ""

            // Cargar modo oscuro
            isDarkMode = encryptedSharedPreferences.getBoolean("darkMode", false)

            // Cargar idioma preferido
            selectedLanguageIndex = encryptedSharedPreferences.getInt("language", 0)

            // Cargar volumen de notificaciones
            notificationVolume = encryptedSharedPreferences.getInt("volume", 50)

            // Cargar última fecha y hora de acceso
            lastAccess = encryptedSharedPreferences.getString("lastAccess", "Nunca") ?: "Nunca"

            // Cargar última ubicación
            lastLocation = encryptedSharedPreferences.getString("lastLocation", "Desconocida") ?: "Desconocida"

            // Cargar tiempo total de uso
            totalUsageTime = encryptedSharedPreferences.getLong("totalUsageTime", 0)
            updateTotalUsageTimeDisplay()

            // Actualizar la fecha y hora de acceso actual
            val currentDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val currentDate = currentDateFormat.format(Date())

            // Guardar la fecha actual como último acceso solo si no es la primera ejecución
            if (!isFirstRun) {
                val editor = encryptedSharedPreferences.edit()
                editor.putString("lastAccess", currentDate)
                editor.apply()
                lastAccess = currentDate
            } else {
                isFirstRun = false
            }
        } catch (e: Exception) {
            // En caso de error al desencriptar, mostrar un mensaje y usar valores predeterminados
            Toast.makeText(
                this,
                "Error al cargar preferencias: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun savePreferences() {
        try {
            val editor = encryptedSharedPreferences.edit()

            // Guardar nombre de usuario
            editor.putString("userName", userName)

            // Guardar modo oscuro
            editor.putBoolean("darkMode", isDarkMode)

            // Guardar idioma preferido
            editor.putInt("language", selectedLanguageIndex)

            // Guardar volumen de notificaciones
            editor.putInt("volume", notificationVolume)

            // Guardar fecha y hora de acceso actual
            val currentDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val currentDate = currentDateFormat.format(Date())
            editor.putString("lastAccess", currentDate)
            lastAccess = currentDate

            // Actualizar y guardar el tiempo total de uso
            updateTotalUsageTime()
            editor.putLong("totalUsageTime", totalUsageTime)

            editor.apply()
        } catch (e: Exception) {
            // En caso de error al encriptar, mostrar un mensaje
            Toast.makeText(
                this,
                "Error al guardar preferencias: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onLocationChanged(location: Location) {
        // Actualizar la última ubicación conocida
        val locationText = "Lat: ${location.latitude}, Long: ${location.longitude}"
        lastLocation = locationText

        try {
            // Guardar la ubicación en las preferencias encriptadas
            val editor = encryptedSharedPreferences.edit()
            editor.putString("lastLocation", locationText)
            editor.apply()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al guardar ubicación: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onPause() {
        super.onPause()
        // Actualizar y guardar el tiempo total de uso cuando la aplicación pasa a segundo plano
        updateTotalUsageTime()
        savePreferences()
    }

    override fun onResume() {
        super.onResume()
        // Reiniciar el contador de tiempo cuando la aplicación vuelve a primer plano
        startTime = System.currentTimeMillis()
        loadPreferences()
    }

    private fun updateTotalUsageTime() {
        val currentTime = System.currentTimeMillis()
        val sessionTime = currentTime - startTime
        totalUsageTime += sessionTime
        startTime = currentTime
        updateTotalUsageTimeDisplay()
    }

    private fun updateTotalUsageTimeDisplay() {
        // Convertir milisegundos a formato legible
        val seconds = (totalUsageTime / 1000) % 60
        val minutes = (totalUsageTime / (1000 * 60)) % 60
        val hours = (totalUsageTime / (1000 * 60 * 60))

        totalUsageTimeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    // Métodos vacíos requeridos por LocationListener
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}