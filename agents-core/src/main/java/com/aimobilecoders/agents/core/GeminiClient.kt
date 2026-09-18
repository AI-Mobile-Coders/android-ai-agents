package com.aimobilecoders.agents.core

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * [LlmClient] backed by the Gemini API (Google Generative Language API).
 *
 * Dev mode: pass a raw Gemini API key. This works from the device, but is NOT safe
 * for a published app because the key can be extracted from the APK. For production,
 * route requests through your own backend or Firebase AI Logic instead.
 */
class GeminiClient(
    private val apiKey: String,
    private val model: String = DEFAULT_MODEL,
    private val http: OkHttpClient = OkHttpClient(),
) : LlmClient {

    init {
        require(apiKey.isNotBlank()) { "Gemini API key must not be blank." }
        Log.w(
            TAG,
            "Using a raw Gemini API key on-device. Fine for prototypes, but do NOT ship this " +
                "in a public app - route through a backend or Firebase AI Logic for production.",
        )
    }

    override suspend fun generate(prompt: String, systemInstruction: String?): String =
        withContext(Dispatchers.IO) {
            val url = "$BASE_URL/$model:generateContent?key=$apiKey"
            val payload = buildRequestJson(prompt, systemInstruction).toString()
            val request = Request.Builder()
                .url(url)
                .post(payload.toRequestBody(JSON))
                .build()

            http.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IOException("Gemini request failed (${response.code}): $raw")
                }
                parseText(raw)
            }
        }

    override fun stream(prompt: String, systemInstruction: String?): Flow<String> = flow {
        // v0.1: emit the full reply as a single chunk. True token streaming
        // (streamGenerateContent + SSE) is planned for a later version.
        emit(generate(prompt, systemInstruction))
    }.flowOn(Dispatchers.IO)

    private fun buildRequestJson(prompt: String, systemInstruction: String?): JSONObject {
        val userContent = JSONObject()
            .put("role", "user")
            .put("parts", JSONArray().put(JSONObject().put("text", prompt)))
        val root = JSONObject().put("contents", JSONArray().put(userContent))
        if (!systemInstruction.isNullOrBlank()) {
            root.put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", systemInstruction)),
                ),
            )
        }
        return root
    }

    private fun parseText(raw: String): String {
        val candidates = JSONObject(raw).optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""
        val parts = candidates.getJSONObject(0)
            .optJSONObject("content")
            ?.optJSONArray("parts") ?: return ""
        val builder = StringBuilder()
        for (i in 0 until parts.length()) {
            builder.append(parts.getJSONObject(i).optString("text"))
        }
        return builder.toString().trim()
    }

    companion object {
        /** Default model. Change this if you want a different Gemini variant. */
        const val DEFAULT_MODEL = "gemini-flash-latest"

        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private const val TAG = "GeminiClient"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
