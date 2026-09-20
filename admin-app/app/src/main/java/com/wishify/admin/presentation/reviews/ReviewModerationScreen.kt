package com.wishify.admin.presentation.reviews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wishify.admin.data.model.AdminReview
import com.wishify.admin.data.repository.WishifyAdminRepository
import com.wishify.admin.ui.theme.GoldAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewModerationScreen(
    repository: WishifyAdminRepository,
    modifier: Modifier = Modifier
) {
    var reviewsState by remember { mutableStateOf<Result<List<AdminReview>>?>(null) }
    val scope = rememberCoroutineScope()

    val fetchReviews = {
        scope.launch {
            reviewsState = null
            reviewsState = repository.getReviews()
        }
    }

    LaunchedEffect(Unit) {
        fetchReviews()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Review Moderation ⭐", fontWeight = FontWeight.Bold) })
        },
        modifier = modifier
    ) { padding ->
        val state = reviewsState
        when {
            state == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldAccent)
                }
            }
            state.isFailure -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Failed to load reviews", color = Color.Red)
                }
            }
            state.isSuccess -> {
                val reviews = state.getOrNull()!!
                if (reviews.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("No reviews awaiting moderation", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize().padding(padding)
                    ) {
                        items(reviews) { review ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(review.product?.title ?: "Product", fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                            Text("${review.rating}/5", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("By: ${review.user?.name ?: "Customer"}", fontSize = 12.sp, color = Color.Gray)
                                    review.comment?.let {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("\"$it\"", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                scope.launch {
                                                    repository.updateReview(review.id, "REJECTED")
                                                    fetchReviews()
                                                }
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reject")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    repository.updateReview(review.id, "APPROVED")
                                                    fetchReviews()
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
