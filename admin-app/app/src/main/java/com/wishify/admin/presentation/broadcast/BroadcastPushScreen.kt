package com.wishify.admin.presentation.broadcast

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wishify.admin.data.repository.WishifyAdminRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastPushScreen(
    repository: WishifyAdminRepository,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("Festive Surprise! 🎉") }
    var message by remember { mutableStateOf("Enjoy 20% OFF on all gourmet cakes & roses today only with code FESTIVE20.") }
    var isSending by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Broadcast Push Notification 📢", fontWeight = FontWeight.Bold) })
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Compose Push Message", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Notification Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Notification Body / Message") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    statusMessage?.let {
                        Text(
                            text = it,
                            color = if (it.contains("Success", ignoreCase = true)) Color(0xFF81C784) else Color.Red,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isSending = true
                                statusMessage = null
                                val res = repository.broadcastPush(title, message)
                                isSending = false
                                if (res.isSuccess) {
                                    statusMessage = "Success: Broadcast dispatched to all active customer devices!"
                                } else {
                                    statusMessage = "Error: ${res.exceptionOrNull()?.message ?: "Broadcast failed"}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSending && title.isNotBlank() && message.isNotBlank()
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dispatch Broadcast to All Customers", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
