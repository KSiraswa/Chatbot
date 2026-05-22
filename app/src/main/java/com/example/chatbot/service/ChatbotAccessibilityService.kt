package com.example.chatbot.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class ChatbotAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ChatbotAccessibility"
        var instance: ChatbotAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We can track app transitions here if needed
    }

    override fun onInterrupt() {
        instance = null
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun extractScreenText(): String {
        val rootNode = rootInActiveWindow ?: return ""
        val sb = StringBuilder()
        extractTextRecursive(rootNode, sb)
        return sb.toString()
    }

    private fun extractTextRecursive(node: AccessibilityNodeInfo?, sb: StringBuilder) {
        if (node == null) return

        if (node.text != null) {
            sb.append(node.text).append("\n")
        } else if (node.contentDescription != null) {
            sb.append(node.contentDescription).append("\n")
        }

        for (i in 0 until node.childCount) {
            extractTextRecursive(node.getChild(i), sb)
        }
    }

    fun getForegroundApp(): String {
        return rootInActiveWindow?.packageName?.toString() ?: "Unknown"
    }
}
