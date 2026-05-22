package com.example.chatbot.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chatbot.data.ChatMessage

@Composable
fun FloatingChatUI(
    messages: List<ChatMessage>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSendMessage: (String) -> Unit,
    onClose: () -> Unit,
    onCaptureScreen: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandIn(),
            exit = shrinkOut()
        ) {
            ChatScreen(
                messages = messages,
                onSendMessage = onSendMessage,
                onClose = onToggleExpand
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = CircleShape,
            color = Color(0xFF00E5FF).copy(alpha = 0.8f),
            modifier = Modifier
                .size(60.dp)
                .clickable { onToggleExpand() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chat",
                    tint = Color.Black
                )
            }
        }
    }
}
