# 🤖 AI Screen Assistant Chatbot

An advanced, futuristic Android chatbot that "sees" what is on your screen and provides AI-powered assistance using **Google Gemini 1.5 Flash**. Built with a modern Jetpack Compose UI and deep Android system integration.

## ✨ Features

*   **🔍 Screen Awareness**: Uses Android Accessibility Services and ML Kit OCR to extract text and context from any app you are currently using.
*   **🛸 Floating Overlay**: Stays active on top of other apps as a draggable bubble, allowing you to get help without switching tasks.
*   **🫨 Shake to Open**: Uses the phone's Accelerometer to detect a shake gesture to instantly expand the chatbot.
*   **🖐️ Proximity Hide**: Cover the top of your phone (Proximity Sensor) to quickly minimize the chat window for privacy.
*   **💎 Glassmorphic UI**: A futuristic, semi-transparent design built entirely with **Jetpack Compose**.
*   **⚡ Gemini 1.5 Flash**: Powered by Google's fastest free-tier multimodal model for lightning-fast responses.

## 🛠️ Tech Stack

- **UI**: Jetpack Compose (Material 3)
- **AI Engine**: Google Generative AI SDK (Gemini)
- **Background Logic**: Android Foreground Services & Lifecycle Services
- **Context Extraction**: Android Accessibility Service API
- **OCR**: Google ML Kit Text Recognition
- **Sensors**: Hardware Accelerometer & Proximity Sensors
- **Asynchronous**: Kotlin Coroutines & Flow

## 🚀 Getting Started

### Prerequisites
1.  Android Studio Ladybug or newer.
2.  An API Key from [Google AI Studio](https://aistudio.google.com/app/apikey).
3.  An Android device running API 26 (Oreo) or higher.

### Setup
1.  Clone the repository:
    ```bash
    git clone https://github.com/your-username/Chatbot.git
    ```
2.  Open the project in Android Studio.
3.  Open `FloatingChatService.kt` and replace the `API_KEY` placeholder with your own key:
    ```kotlin
    private val API_KEY = "YOUR_API_KEY_HERE"
    ```
4.  Build and Run the app.

### 🔑 Permissions Needed
To function correctly, this app requires:
- **Display over other apps**: To show the floating bubble.
- **Accessibility Service**: To read the content of other apps (this data is sent only to the Gemini API and is not stored).

## 📖 How to Use
1.  Open the app and click **Enable Accessibility**.
2.  Find "Chatbot" in the list and turn it **ON**.
3.  Click **Start Chatbot Service**.
4.  **To Open**: Either tap the floating cyan bubble or **Shake your phone**.
5.  **To Close**: Tap the "X" button or **Cover the proximity sensor** at the top of your phone.

## ⚖️ License
Distributed under the MIT License. See `LICENSE` for more information.

---
*Developed as a futuristic AI experiment.*
