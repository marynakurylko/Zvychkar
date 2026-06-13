# Zvychkar 🚀

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Modern%20UI-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Backend-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Material 3](https://img.shields.io/badge/Material%203-Design-757575?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io/)

**Zvychkar** is a modern, sleek habit tracker designed to help you build better routines with a premium user experience. Built using the latest Android technologies, it combines high-performance functionality with a stunning **Glassmorphism** aesthetic.

---

## ✨ Key Features

- 📶 **Offline Support** – Keep tracking your progress even without an internet connection; data syncs seamlessly when you're back online.
- 🎨 **Dynamic Glassmorphism UI** – A beautiful, modern interface with frosted glass effects and fluid animations.
- 🌓 **Dark & Light Mode** – Fully optimized for both themes, respecting your system preferences.
- 🔐 **Firebase Authentication** – Secure login and cloud synchronization across multiple devices.
- 📊 **Crash Monitoring** – Integrated with Firebase Crashlytics to ensure a stable and bug-free experience.
- 🔔 **Smart Notifications** – Customizable reminders to help you stay consistent with your habits.
- 📱 **Home Screen Widgets** – Quick-access widgets built with Jetpack Glance to track habits at a glance.

---

## 🛠 Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/) (Coroutines, Flow, Serialization)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture:** MVVM + Clean Architecture
- **Dependency Injection:** Hilt (Dagger)
- **Backend:** Firebase Auth & Firestore
- **Local Storage:** DataStore & Room (Offline-first approach)
- **Monitoring:** Firebase Crashlytics
- **UI Extras:** Material 3, Konfetti (Celebrations), Compose Navigation

---

## 🏗 Architecture Overview

Zvychkar follows **Clean Architecture** principles combined with the **MVVM (Model-View-ViewModel)** pattern to ensure a scalable, maintainable, and testable codebase.

- **Data Layer:** Handles API communication, Firebase integration, and local persistence.
- **Domain Layer:** Contains business logic and Use Cases.
- **Presentation Layer:** State-driven UI using Jetpack Compose and ViewModels.

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Koala | 2024.1.1 or newer
- JDK 17+
- A Firebase Project

### Installation & Build

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/Zvychkar.git
   ```

2. **Setup Firebase:**
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Create a new project and add an Android App.
   - Use the package name: `com.example.vibehabit`.
   - Download the `google-services.json` file.
   - Place `google-services.json` in the `/app` directory.

3. **Build the project:**
   - Open the project in Android Studio.
   - Let Gradle sync finish.
   - Press **Run** to deploy the app to your emulator or physical device.

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Developed with ❤️ for better habits.
</p>
