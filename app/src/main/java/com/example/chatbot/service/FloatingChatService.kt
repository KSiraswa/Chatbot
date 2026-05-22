package com.example.chatbot.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.MotionEvent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.chatbot.R
import com.example.chatbot.ai.GeminiClient
import com.example.chatbot.ai.ScreenAnalyzer
import com.example.chatbot.data.ChatMessage
import com.example.chatbot.ui.FloatingChatUI
import kotlinx.coroutines.launch

class FloatingChatService : LifecycleService(), ViewModelStoreOwner, SavedStateRegistryOwner, SensorEventListener {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var params: WindowManager.LayoutParams? = null

    private val chatMessages = mutableStateListOf<ChatMessage>().apply {
        add(ChatMessage("Hello! I am your AI screen assistant. Click me or SHAKE your phone to analyze this screen.", false))
    }
    
    private var isExpanded by mutableStateOf(false)
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var proximitySensor: Sensor? = null
    private var lastShakeTime: Long = 0

    private lateinit var screenAnalyzer: ScreenAnalyzer
    private lateinit var geminiClient: GeminiClient

    // Implementing ViewModelStoreOwner
    private val mViewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore
        get() = mViewModelStore

    // Implementing SavedStateRegistryOwner
    private val mSavedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = mSavedStateRegistryController.savedStateRegistry

    // Placeholder API Key - User should replace this
    private val API_KEY = "AIzaSyAyu5nlFkC0oSpsTssGmGvXagR7Qul8cgY"

    override fun onCreate() {
        super.onCreate()
        mSavedStateRegistryController.performRestore(null)
        screenAnalyzer = ScreenAnalyzer(this)
        geminiClient = GeminiClient(API_KEY)
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        
        startForegroundService()
        initFloatingWindow()
    }

    private fun startForegroundService() {
        val channelId = "chatbot_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Chatbot Service",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Chatbot is active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(1, notification)
    }

    private fun initFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingChatService)
            setViewTreeViewModelStoreOwner(this@FloatingChatService)
            setViewTreeSavedStateRegistryOwner(this@FloatingChatService)

            setContent {
                FloatingChatUI(
                    messages = chatMessages,
                    isExpanded = isExpanded,
                    onToggleExpand = { toggleChat() },
                    onSendMessage = { text -> sendMessageToAI(text) },
                    onClose = { stopSelf() },
                    onCaptureScreen = { captureAndSummarize() }
                )
            }
        }

        floatingView = composeView

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        windowManager.addView(floatingView, params)

        floatingView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.0f
            private var initialTouchY: Float = 0.0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params!!.x
                        initialY = params!!.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params!!.x = initialX + (event.rawX - initialTouchX).toInt()
                        params!!.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun updateWindowFocusable(focusable: Boolean) {
        params?.let { p ->
            if (focusable) {
                p.flags = p.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            } else {
                p.flags = p.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
            if (::floatingView.isInitialized) {
                windowManager.updateViewLayout(floatingView, p)
            }
        }
    }

    private fun sendMessageToAI(userText: String) {
        chatMessages.add(ChatMessage(userText, true))
        
        lifecycleScope.launch {
            val prompt = "User asked: $userText\nPrevious messages context: ${chatMessages.takeLast(5).joinToString { it.text }}"
            val response = geminiClient.generateResponse(prompt)
            chatMessages.add(ChatMessage(response, false))
        }
    }

    private fun captureAndSummarize() {
        if (ChatbotAccessibilityService.instance == null) {
            chatMessages.add(ChatMessage("Please enable Accessibility Service for this app in Settings to read the screen.", false))
            return
        }

        chatMessages.add(ChatMessage("Analyzing screen...", false))
        
        lifecycleScope.launch {
            // Note: MediaProjection for screenshot is complex, using Accessibility text only for now
            val contextPrompt = screenAnalyzer.analyzeScreen(null) 
            val summaryPrompt = "Please summarize this screen content:\n$contextPrompt"
            val response = geminiClient.generateResponse(summaryPrompt)
            
            // Replace the "Analyzing..." message
            if (chatMessages.isNotEmpty()) chatMessages.removeAt(chatMessages.size - 1)
            chatMessages.add(ChatMessage(response, false))
        }
    }

    private fun toggleChat() {
        isExpanded = !isExpanded
        updateWindowFocusable(isExpanded)
        if (isExpanded) {
            captureAndSummarize()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
                if (acceleration > 12) { // Shake sensitivity threshold
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastShakeTime > 2000) { // 2 second cooldown
                        lastShakeTime = currentTime
                        if (!isExpanded) {
                            toggleChat()
                        }
                    }
                }
            }
            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                // distance < proximitySensor.maximumRange usually means "near"
                // Most sensors return 0 for near and a larger value for far.
                if (distance < (proximitySensor?.maximumRange ?: 5f) && isExpanded) {
                    toggleChat() // Close it
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        mViewModelStore.clear()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }
}
