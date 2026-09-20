# Budget Calculator

A production-quality Android application for managing personal finances, built with modern Android development practices.

## Architecture
The project strictly follows **Clean Architecture** with three main layers:
- **Presentation Layer**: Jetpack Compose, ViewModels (MVVM), StateFlow, Navigation Compose.
- **Domain Layer**: Pure Kotlin business logic, Use Cases, Repository interfaces.
- **Data Layer**: Room Database (Local Source of Truth), Firebase Auth, Firestore (Cloud Sync), Mappers.

## Tech Stack
- **Kotlin** & **Coroutines** (Flow)
- **Jetpack Compose** (UI)
- **Material 3** (Design System)
- **Hilt** (Dependency Injection)
- **Room** (Local Database)
- **Firebase Auth** (Authentication)
- **Firebase Firestore** (Cloud Backup)
- **WorkManager** (Background Synchronization)
- **DataStore** (App Preferences)
- **Navigation Compose** (Type-safe Navigation)

## Project Structure
- `core/`: Common utilities and design system components.
- `data/`: Room entities, DAOs, Firestore services, and repository implementations.
- `domain/`: Business models, repository interfaces, and use cases.
- `presentation/`: Compose screens, ViewModels, and navigation logic.

## Setup Instructions
1.  **Firebase Setup**:
    - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
    - Add an Android App with package name `com.vegam.budgetcalculator`.
    - Download `google-services.json` and place it in the `app/` directory (replacing the dummy file).
    - Enable **Email/Password** authentication in the Firebase Auth settings.
    - Enable **Cloud Firestore** in test or production mode (apply the recommended security rules).
2.  **Build**:
    - Open the project in Android Studio.
    - Sync Gradle and run the `:app:assembleDebug` task.

## Key Features
- **Offline-First**: All data is stored locally in Room and synced to the cloud when online.
- **Monthly Budgeting**: Set budgets for categories and track spending progress.
- **Analytics**: Donut charts for category spending and Bar charts for daily trends.
- **Calendar View**: Interactive calendar to view transactions by date.
- **Modern UI**: Polished Material 3 design with dark mode support.

## Firestore Security Rules
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      match /expenses/{expenseId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```
