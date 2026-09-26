# Couple (Days in Love) ❤️

**Couple (Days in Love)** es una aplicación móvil nativa para Android diseñada para parejas que desean llevar el registro del tiempo que llevan juntas, recordar fechas especiales y aniversarios, guardar metas compartidas en una *Bucket List*, personalizar sus perfiles y proteger su privacidad con seguridad biométrica y PIN.

---

## 📌 Guía Rápida para Desarrolladores

Si vas a continuar el desarrollo de este proyecto en una nueva PC o perfil de Android Studio, consulta nuestra guía detallada paso a paso:

👉 **[Guía de Instalación y Configuración del Proyecto (SETUP_GUIDE.md)](SETUP_GUIDE.md)**

---

## ✨ Características Principales

* ⏳ **Contador de Días en Enamorados:** Muestra de manera clara y atractiva el tiempo exacto (días, meses, años) que la pareja lleva junta.
* 📅 **Eventos Especiales y Aniversarios:** Registro de fechas importantes con recordatorios y cuenta regresiva.
* 🎯 **Lista de Deseos (Bucket List):** Espacio interactivo para guardar metas, viajes y actividades pendientes por realizar en pareja.
* 🎨 **Perfiles Personalizables:** Edición de nombres, fotos de avatar con herramienta de recorte personalizada (`CropImageDialog`).
* 💌 **Tarjetas de Aniversario:** Creador de tarjetas personalizadas listas para compartir en redes sociales o guardar como recuerdo.
* 🔒 **Seguridad y Privacidad:** Bloqueo de la aplicación mediante código PIN y autenticación biométrica (huella dactilar o reconocimiento facial) utilizando `androidx.biometric`.
* 📱 **Widget para Pantalla de Inicio:** Widget flotante para visualizar el contador de días directamente desde el escritorio de Android.

---

## 🛠️ Tecnologías y Requisitos

* **Lenguaje principal:** Java (Android Native)
* **Arquitectura:** Patrón basado en componentes Android (Activities, Fragments, Dialogs, Helpers)
* **Min SDK:** API 29 (Android 10.0)
* **Target SDK:** API 37
* **Librerías clave:**
  * Material Design Components (`com.google.android.material`)
  * Jetpack Biometric (`androidx.biometric`)
  * AndroidX ConstraintLayout & AppCompat
  * AppWidgets para pantalla de inicio

---

## 📂 Estructura del Proyecto

```text
app/src/main/
├── java/com/example/coupledaysinlove/
│   ├── MainActivity.java            # Actividad principal con navegación
│   ├── SecurityLockActivity.java    # Pantalla de bloqueo PIN/Biométrico
│   ├── adapters/                    # Adaptadores para RecyclerViews (Bucket, Eventos)
│   ├── dialogs/                     # Diálogos (Recorte de fotos, agregar evento, PIN, etc.)
│   ├── fragments/                   # Fragmentos principales (Counter, Events, Profile, Settings)
│   ├── helpers/                     # Utilidades (Fechas, Imagen, Notificaciones, Preferencias)
│   ├── models/                      # Modelos de datos (BucketItem, SpecialEvent)
│   ├── receivers/                   # BroadcastReceivers (BootReceiver, NotificationReceiver)
│   └── widgets/                     # Provider del Widget de pantalla de inicio
└── res/                             # Layouts, drawables, colores y recursos XML
```

---

## 🚀 Cómo Empezar

Para clonar el repositorio e instalar el entorno en tu equipo:

1. Clona el repositorio:
   ```bash
   git clone https://github.com/TU_USUARIO/TU_REPOSITORIO.git
   ```
2. Lee la **[Guía de Instalación y Configuración](SETUP_GUIDE.md)** para los pasos exactos en Android Studio.

---

## 📄 Licencia

Este proyecto está desarrollado para fines educativos y personales.
