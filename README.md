Pawgress - Pet Growth Tracker
A comprehensive Android app for tracking your pet's growth, health records, and memorable moments.
Features

📈 Growth Tracking - Monitor weight, height, and length with interactive charts
💉 Vaccine Management - Track vaccination history and upcoming due dates
🌡️ Heat Cycle Tracking - Monitor female pets' reproductive cycles (female pets only)
📸 Photo Gallery - Capture and organize your pet's photos with favorites
📝 Notes & Records - Add detailed notes for each entry
🐕 Multi-Pet Support - Manage multiple pets with individual profiles

Screenshots
| Home Screen | Growth Tracker | Vaccine Records |
|-------------|----------------|-----------------|
| ![Image](https://github.com/user-attachments/assets/2ce97292-98d5-4cec-91e8-d2c7f83b6d7b) | ![Image](https://github.com/user-attachments/assets/3dac0d1c-372b-48e6-a34b-96924ec61ee8) | ![image](https://github.com/user-attachments/assets/da9f386f-cd70-42ff-ae95-ce8562d81850)|

Language: Java
Architecture: MVVM (Model-View-ViewModel)
Database: Room (SQLite)
UI: Material Design Components
Charts: MPAndroidChart
Image Loading: Glide
Lifecycle: Android Architecture Components (LiveData, ViewModel)

Database Schema
├── pets (main pet profiles)
├── growth_entries (weight, height, length records)
├── vaccine_entries (vaccination records)
├── heat_cycles (reproductive cycle tracking)
├── photo_entries (pet photos with metadata)
└── notes (general notes)
Installation

Clone the repository:

bashgit clone https://github.com/joshuazara/Pawgress-PetGrowthTracker.git

Open in Android Studio
Build and run:

bash./gradlew assembleDebug
Requirements

Android Studio: Arctic Fox or newer
Min SDK: 24 (Android 7.0)
Target SDK: 35 (Android 15)
Java: 11+

Key Components
Database Layer

Room database with TypeConverters for Date handling
DAO interfaces for clean data access
Foreign key relationships with cascade delete

ViewModels

Direct DAO access (Repository pattern removed for simplicity)
Proper lifecycle management with ExecutorService
LiveData for reactive UI updates

UI Components

RecyclerView adapters with base adapter pattern
Material Design dialogs and forms
Custom chart styling and animations

Permissions
xml<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
Architecture Decisions

No Repository Layer: Direct ViewModel-to-DAO access for simplicity
Single Database: All pet data in one Room database
Material Design: Consistent UI following Android design guidelines
Lifecycle Awareness: Proper handling of configuration changes
