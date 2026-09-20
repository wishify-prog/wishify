package com.wishify.customer.data.remote

import android.util.Log
import com.wishify.customer.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object RealtimeSyncManager {
    private const val TAG = "RealtimeSyncManager"

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var isRunning = false

    suspend fun startListening() = withContext(Dispatchers.IO) {
        if (isRunning) return@withContext
        isRunning = true

        val sseUrl = "${BuildConfig.BASE_URL.trimEnd('/')}/api/v1/events"
        val request = Request.Builder()
            .url(sseUrl)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        while (isRunning) {
            try {
                Log.d(TAG, "Connecting to SSE stream: $sseUrl")
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e(TAG, "SSE connection failed with code ${response.code}")
                        delay(5000)
                        return@use
                    }

                    val source = response.body?.byteStream() ?: return@use
                    val reader = BufferedReader(InputStreamReader(source))

                    var line: String? = null
                    while (isRunning && reader.readLine().also { line = it } != null) {
                        val currentLine = line ?: break
                        if (currentLine.startsWith("data:")) {
                            val data = currentLine.removePrefix("data:").trim()
                            if (data.isNotEmpty()) {
                                Log.d(TAG, "Received SSE event: $data")
                                _events.tryEmit(data)
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                isRunning = false
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "SSE connection interrupted, retrying in 3s: ${e.message}")
                delay(3000)
            }
        }
    }

    fun stop() {
        isRunning = false
    }
}
