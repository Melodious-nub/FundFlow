# FundFlow 💸

Smart Budget. Smarter Life. A modern, privacy-focused, offline-first personal finance manager for Android.

[![Android Version](https://img.shields.io/badge/Android-8.0%2B-brightgreen.svg)](https://developer.android.com)
[![Privacy](https://img.shields.io/badge/Privacy-Offline%20First-blue.svg)](#-privacy--security)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 🌟 Overview

**FundFlow** is a premium expense tracking utility designed for users who demand both flexibility and privacy. Built with a **"User-First"** philosophy, FundFlow breaks away from the rigid monthly constraints of traditional finance apps, allowing you to manage your money according to your life’s actual rhythm.

It is a **100% Native Android** application built using the latest modern tech stack, ensuring a fluid, high-performance experience without the bloat.

---

## 📖 The Story Behind FundFlow

The inspiration for FundFlow came from a gap in the market. Most budget trackers force you into a strict 1st-to-30th calendar month cycle. But life doesn't always work that way. Whether you get paid bi-weekly, manage a project-based budget, or just want to track a specific vacation fund, traditional apps feel restrictive.

Furthermore, many popular finance apps require you to link your bank accounts or upload your sensitive financial data to their servers. As a developer, I wanted a tool that:
1. **Adapts to me:** Let me define my own start and end dates.
2. **Protects my data:** Keep everything on my device, with cloud backups being a *choice*, not a requirement.
3. **Looks Professional:** Provide high-end analytics that actually make sense.

So, I built FundFlow to be the professional financial companion I wanted for myself.

---

## ✨ Key Features

### 📅 Custom Budget Cycles
Forget rigid calendar months. FundFlow allows you to define your own budget periods—whether it's weekly, bi-weekly, or custom ranges. The app dynamically calculates your balance and progress based on your unique cycle dates.

### 📊 Professional Analytics & Insights
Powered by **Vico Charts**, visualize your financial health with high-end charts optimized for clarity:
- **Daily Spending:** High-contrast bar charts to track your day-to-day fluctuations.
- **Cumulative Trends:** Smooth line charts showing the "flow" of your total spending over time.
- **Category Breakdown:** Interactive progress bars that show exactly where your money is going.

### ☁️ Robust Backup & Restore System
Never lose your financial history with our comprehensive backup suite:
- **Local Backup:** Securely export your data as encrypted JSON files to the public `Downloads/FundFlow_Backups` folder for easy manual management.
- **Google Drive Integration:** Seamlessly sync your data to your private Google Drive for maximum security.
- **Automatic Backups:** Powered by **WorkManager**, FundFlow can perform silent background backups periodically, ensuring your data is always safe without you lifting a finger.

### 🌓 Advanced Personalization (Theme Engine)
A truly professional app should look great in any lighting. FundFlow features a dedicated **Theme Selection** menu:
- **System Default:** Automatically matches your phone’s global theme.
- **Dark Mode:** A battery-saving, eye-friendly high-contrast dark UI.
- **Light Mode:** A crisp, clean white UI with optimized system status bars (clock/icons correctly tinted for visibility).

### 🚀 Seamless In-App Updates
Stay ahead with the latest features. FundFlow includes a dedicated **App Update** screen that checks the **GitHub Releases API** and handles the download and installation process automatically via a background BroadcastReceiver.

### 🌐 100% Privacy & Offline-First
Your financial data belongs to you, not a server. FundFlow works entirely offline and collects **zero** personal data. Cloud features like Google Drive are strictly opt-in and controlled by you.

---

## 🛠️ Technical Details & Compatibility

- **Minimum Version:** Android 8.0 (API 26)
- **Target Version:** Android 15 (API 35)
- **Language:** Kotlin 2.1.0+ (100% Native)
- **UI Framework:** Jetpack Compose with Material 3
- **Architecture:** MVVM + Clean Architecture
- **Dependency Injection:** Hilt
- **Database:** Room (Offline-first persistence)
- **Background Tasks:** WorkManager (For auto-updates and auto-backups)
- **Charts:** Vico (High-performance charting)

---

## 🛡️ Privacy & Security

This app is designed to be a "Vault" for your financial data.
- **Data Collection:** Zero. We do not track your spending, your location, or your identity.
- **Permissions:** 
    - `INTERNET`: Only used for optional Google Drive backups and checking for app updates.
    - `POST_NOTIFICATIONS`: Used only to alert you if a scheduled background backup fails.
- **Encryption:** All local data is stored within the app's secure sandbox.

---

## 🚀 How to Use

1. **Install** the APK.
2. **Create a Cycle:** Set your budget amount and your preferred date range.
3. **Log Expenses:** Add your daily spending with categories and notes.
4. **Manage Cycles:** Access "Manage Cycles" in Settings to view your history, edit active budget ranges, or delete old periods to keep your dashboard clean.
5. **Track Progress:** Use the Dashboard to see your "Safe Spending" amount per day to stay on track.
6. **Analyze:** Visit the Analytics tab to visualize your financial flow.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Developer

**Shawon Talukder**  
Software Engineer at Green Delta Insurance PLC  
Email: [shawon.taluckder2@gmail.com](mailto:shawon.taluckder2@gmail.com)  
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Profile-blue?style=flat&logo=linkedin)](https://www.linkedin.com/in/shawon-taluckder)  
[![Fiverr](https://img.shields.io/badge/Fiverr-Hire_Me-green?style=flat&logo=fiverr)](https://www.fiverr.com/talukder_shawon/)

---

*Developed with ❤️ for the Android community.*
