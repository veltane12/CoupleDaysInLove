# 🚀 Guía de Instalación y Configuración en Android Studio

Esta guía describe los pasos necesarios para descargar, configurar y ejecutar el proyecto **Couple (Days in Love)** en un nuevo perfil de **Android Studio** o en una computadora diferente.

---

## 📋 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado lo siguiente en la nueva PC:

1. **Android Studio:**
   * Versión recomendada: **Android Studio Ladybug (2024.2.1)**, **Jellyfish**, **Iguana** o superior.
2. **Java Development Kit (JDK):**
   * **JDK 17** o **JDK 11** (Android Studio incluye su propio JDK integrado que se puede usar directamente).
3. **Android SDK:**
   * Android SDK Platform instalado para **API 29 (Android 10)** hasta **API 34/37**.
4. **Git:**
   * Git instalado en el sistema operativo para clonar y gestionar el repositorio.

---

## 🛠️ Paso a Paso para la Instalación

### Paso 1: Clonar el Repositorio desde Git

Abre tu terminal (PowerShell, CMD o Git Bash) y ejecuta:

```bash
git clone https://github.com/TU_USUARIO/TU_REPOSITORIO.git
```

Navega a la carpeta del proyecto:
```bash
cd CoupleDaysInLove
```

---

### Paso 2: Abrir el Proyecto en Android Studio

1. Inicia **Android Studio**.
2. En la pantalla de bienvenida (*Welcome to Android Studio*), selecciona **Open** (o ve a `File` -> `Open...` si ya tienes otro proyecto abierto).
3. Selecciona la carpeta raíz del proyecto (`CoupleDaysInLove`).
4. Espera a que Android Studio reconozca la estructura del proyecto y los archivos de configuración Gradle.

---

### Paso 3: Configurar el JDK y Sincronizar Gradle

1. Ve a **Settings / Preferences**:
   * En Windows/Linux: `File` -> `Settings` -> `Build, Execution, Deployment` -> `Build Tools` -> `Gradle`.
   * En macOS: `Android Studio` -> `Settings` -> `Build, Execution, Deployment` -> `Build Tools` -> `Gradle`.
2. En la opción **Gradle JDK**, asegúrate de seleccionar **Embedded JDK** (JDK 17 o JDK 11).
3. Presiona **Apply** y **OK**.
4. Sincroniza el proyecto haciendo clic en el icono del elefante **Sync Project with Gradle Files** en la barra superior o ve a `File` -> `Sync Project with Gradle Files`.

> 💡 **Nota:** Durante la primera sincronización, Android Studio generará automáticamente el archivo `local.properties` local con la ruta del SDK de Android de tu nueva PC.

---

### Paso 4: Configurar un Emulador o Dispositivo Físico

#### Opción A: Crear un Emulador (AVD)
1. Abre el **Device Manager** en Android Studio (`Tools` -> `Device Manager`).
2. Haz clic en **Create Virtual Device**.
3. Selecciona un dispositivo (por ejemplo, *Pixel 7* o *Pixel 8*).
4. Elige una imagen del sistema con nivel de API 29 o superior (ejemplo: API 34 o API 35).
5. Finaliza la creación y enciende el emulador.

#### Opción B: Dispositivo Físico
1. En tu teléfono Android, activa las **Opciones de Desarrollador** (presionando 7 veces en *Número de compilación* en los Ajustes).
2. Activa la **Depuración USB** (*USB Debugging*).
3. Conecta el teléfono a la PC mediante cable USB y acepta el permiso de depuración en la pantalla del teléfono.

---

### Paso 5: Compilar y Ejecutar la Aplicación

1. En la barra superior de Android Studio, asegúrate de que esté seleccionada la configuración de ejecución `app`.
2. Selecciona tu emulador o dispositivo físico en la lista desplegable de dispositivos.
3. Haz clic en el botón verde de **Run 'app'** (o presiona `Shift + F10`).
4. La app se compilará y se instalará automáticamente en tu dispositivo.

---

## ❓ Solución de Problemas Comunes (Troubleshooting)

### 1. Error de ruta de SDK (`SDK location not found`)
Si por alguna razón `local.properties` no se genera automáticamente:
* Crea un archivo llamado `local.properties` en la raíz del proyecto.
* Añade la ruta de tu SDK local:
  * **Windows:**
    ```properties
    sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
    ```
  * **macOS:**
    ```properties
    sdk.dir=/Users/TU_USUARIO/Library/Android/sdk
    ```

### 2. Permisos del ejecutable Gradle (`gradlew`)
En macOS o Linux, si recibes un error de permiso al ejecutar Gradle desde la terminal:
```bash
chmod +x gradlew
```

### 3. Limpieza y Recompilación (Caché de Gradle)
Si experimentas errores extraños de compilación o recursos no encontrados al cambiar de equipo:
1. En Android Studio ve a `Build` -> `Clean Project`.
2. Luego haz clic en `Build` -> `Rebuild Project`.
3. Si el problema persiste, ve a `File` -> `Invalidate Caches...` -> marca todas las casillas y presiona **Invalidate and Restart**.
