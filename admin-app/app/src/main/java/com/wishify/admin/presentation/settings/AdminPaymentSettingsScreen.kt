package com.wishify.admin.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wishify.admin.data.model.PaymentSetting
import com.wishify.admin.data.model.UpdatePaymentSettingRequest
import com.wishify.admin.data.repository.WishifyAdminRepository
import com.wishify.admin.ui.theme.GoldAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPaymentSettingsScreen(
    repository: WishifyAdminRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var settingsState by remember { mutableStateOf<PaymentSetting?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isUploadingQr by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    // Editable form state
    var upiId by remember { mutableStateOf("wishify@upi") }
    var qrImageUrl by remember { mutableStateOf("") }
    var accountHolderName by remember { mutableStateOf("Wishify Gifts") }
    var codMaxAmount by remember { mutableStateOf("100.0") }

    // Image Picker for QR Code upload
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isUploadingQr = true
                    statusMessage = null
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null && bytes.isNotEmpty()) {
                        val result = repository.uploadQrImage(bytes, "image/jpeg")
                        if (result.isSuccess) {
                            val updated = result.getOrNull()!!
                            settingsState = updated
                            qrImageUrl = updated.qrImageUrl
                            statusMessage = "QR Code image uploaded successfully!"
                            isError = false
                        } else {
                            statusMessage = result.exceptionOrNull()?.message ?: "Failed to upload QR image"
                            isError = true
                        }
                    }
                } catch (e: Exception) {
                    statusMessage = e.message ?: "Failed to read image"
                    isError = true
                } finally {
                    isUploadingQr = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        val res = repository.getPaymentSettings()
        if (res.isSuccess) {
            val data = res.getOrNull()!!
            settingsState = data
            upiId = data.upiId
            qrImageUrl = data.qrImageUrl
            accountHolderName = data.accountHolderName
            codMaxAmount = data.codMaxAmount.toString()
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UPI QR & Payment Settings 💳", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            isLoading = true
                            repository.getPaymentSettings().onSuccess {
                                settingsState = it
                                upiId = it.upiId
                                qrImageUrl = it.qrImageUrl
                                accountHolderName = it.accountHolderName
                                codMaxAmount = it.codMaxAmount.toString()
                            }
                            isLoading = false
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GoldAccent)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    statusMessage?.let {
                        Text(
                            text = it,
                            color = if (isError) Color.Red else Color(0xFF2E7D32),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isSaving = true
                                statusMessage = null
                                val req = UpdatePaymentSettingRequest(
                                    upiId = upiId.trim(),
                                    qrImageUrl = qrImageUrl.trim().ifBlank { null },
                                    accountHolderName = accountHolderName.trim(),
                                    codMaxAmount = codMaxAmount.toDoubleOrNull() ?: 100.0
                                )
                                val res = repository.updatePaymentSettings(req)
                                if (res.isSuccess) {
                                    val updated = res.getOrNull()!!
                                    settingsState = updated
                                    statusMessage = "Payment settings saved successfully!"
                                    isError = false
                                } else {
                                    statusMessage = res.exceptionOrNull()?.message ?: "Failed to save settings"
                                    isError = true
                                }
                                isSaving = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        enabled = !isSaving && !isUploadingQr
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Payment Settings", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. UPI QR Image Upload & Preview Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Admin UPI QR Code",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GoldAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Active for Customers",
                                        color = GoldAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // QR Preview Box
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(2.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploadingQr) {
                                    CircularProgressIndicator(color = GoldAccent)
                                } else if (qrImageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = qrImageUrl,
                                        contentDescription = "UPI QR Code",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.QrCode2,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "No QR Code Uploaded",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Upload Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    enabled = !isUploadingQr
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Upload QR Image")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Direct Image URL Field
                            OutlinedTextField(
                                value = qrImageUrl,
                                onValueChange = { qrImageUrl = it },
                                label = { Text("Or Paste QR Image URL") },
                                placeholder = { Text("https://example.com/upi-qr.png") },
                                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                // 2. UPI & Merchant Details
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Merchant UPI Details",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )

                            OutlinedTextField(
                                value = upiId,
                                onValueChange = { upiId = it },
                                label = { Text("UPI ID (VPA)") },
                                placeholder = { Text("e.g. wishify@okaxis") },
                                leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = accountHolderName,
                                onValueChange = { accountHolderName = it },
                                label = { Text("Beneficiary / Merchant Name") },
                                placeholder = { Text("e.g. Wishify Gifts") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                // 3. Cash on Delivery (COD) Rules Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Cash on Delivery (COD) Threshold",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )

                            OutlinedTextField(
                                value = codMaxAmount,
                                onValueChange = { codMaxAmount = it },
                                label = { Text("Maximum COD Order Value (₹)") },
                                placeholder = { Text("100.0") },
                                leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Any order with a total amount greater than ₹$codMaxAmount will automatically disable COD for the customer and require Online Payment via the UPI QR code above.",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
