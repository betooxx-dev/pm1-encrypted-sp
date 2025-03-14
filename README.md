# 🔐 User Preferences Manager 📱

Una aplicación Android moderna desarrollada con Kotlin y Jetpack Compose que permite a los usuarios guardar sus preferencias de manera segura utilizando encriptación avanzada.

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)

## ✨ Características Principales

- 👤 **Gestión de perfil de usuario**: Guarda el nombre del usuario
- 🌓 **Tema oscuro/claro**: Permite alternar entre modos de visualización
- 🌎 **Multi-idioma**: Soporte para selección de varios idiomas (Español, English, Français, Deutsch, Italiano)
- 🔊 **Control de volumen**: Ajusta el volumen de notificaciones con un slider interactivo
- 🕒 **Registro de tiempo**: Guarda la última fecha y hora de acceso automáticamente
- 📍 **Geolocalización**: Registra la última ubicación donde se activó la aplicación
- ⏱️ **Tiempo de uso**: Mide y muestra el tiempo total que el usuario ha usado la aplicación
- 🔒 **Encriptación de datos**: Implementa AES-256 para proteger toda la información del usuario

## 🛠️ Tecnologías Utilizadas

- **Kotlin**: Lenguaje principal de desarrollo
- **Jetpack Compose**: Framework moderno para interfaces de usuario declarativas
- **EncryptedSharedPreferences**: Almacenamiento seguro de datos con cifrado AES-256
- **Android Security Crypto**: Biblioteca para manejo de claves y encriptación
- **LocationManager**: API para gestión de ubicación geográfica
- **Material Design 3**: Componentes modernos de UI siguiendo las guías de Material Design

## 🔍 Cómo Funciona

La aplicación implementa un patrón MVVM (Model-View-ViewModel) simplificado donde:

1. 📱 La interfaz de usuario está construida completamente con Jetpack Compose, proporcionando una experiencia fluida e interactiva.
2. 🔐 Los datos del usuario se almacenan utilizando EncryptedSharedPreferences que:
   - Genera y utiliza una clave maestra AES-256 almacenada en Android Keystore
   - Encripta las claves con AES-256-SIV
   - Encripta los valores con AES-256-GCM
3. 📍 La ubicación del dispositivo se obtiene mediante el LocationManager y se almacena de forma segura
4. ⏱️ El tiempo de uso se calcula midiendo los intervalos entre los eventos onPause y onResume

## 🔒 Seguridad

La aplicación prioriza la seguridad de los datos del usuario:

- **Algoritmo AES-256**: El estándar de la industria para cifrado seguro
- **Android Keystore**: Las claves maestras se almacenan en el hardware seguro del dispositivo cuando está disponible
- **Manejo de Errores**: Control de excepciones para operaciones criptográficas
- **Confidencialidad e Integridad**: El modo GCM asegura que los datos no pueden ser leídos ni modificados sin autorización

## 🏗️ Estructura del Proyecto

```
com.example.sharedpreferences/
├── MainActivity.kt       # Actividad principal con toda la lógica de la aplicación
└── ui/theme/
    ├── Theme.kt          # Configuración de temas claro/oscuro
    ├── Color.kt          # Definición de paleta de colores
    └── Type.kt           # Configuración de tipografía
```

## 📝 Notas Importantes

- La aplicación almacena todos los datos de forma local y encriptada
- Se requieren permisos de ubicación para la funcionalidad completa
- La primera vez que se ejecuta la aplicación, se generan nuevas claves de encriptación
