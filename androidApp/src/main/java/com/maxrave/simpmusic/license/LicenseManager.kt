package com.maxrave.simpmusic.license

import android.content.Context
import android.provider.Settings
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

object LicenseManager {
    const val ENDPOINT =
        "https://tdlicnosntmlojmwnita.supabase.co/functions/v1/validate-pg-music-license"

    private const val PREFERENCES = "pg_music_license"
    private const val KEY_LICENSE = "license_key"
    private const val KEY_FALLBACK_DEVICE_ID = "fallback_device_id"
    private const val PRODUCT = "pg_music"

    @Volatile
    private var sessionValidated = false

    private val client =
        OkHttpClient
            .Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .build()

    data class Result(
        val valid: Boolean,
        val status: String,
        val expiresAt: String? = null,
        val message: String? = null,
    )

    fun hasValidatedSession(): Boolean = sessionValidated

    fun markSessionValidated() {
        sessionValidated = true
    }

    fun savedKey(context: Context): String? =
        context
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(KEY_LICENSE, null)
            ?.takeIf { it.isNotBlank() }

    fun saveKey(
        context: Context,
        key: String,
    ) {
        context
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LICENSE, normalizeKey(key))
            .apply()
    }

    fun clearKey(context: Context) {
        sessionValidated = false
        context
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LICENSE)
            .apply()
    }

    fun deviceId(context: Context): String {
        val androidId =
            Settings.Secure
                .getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }

        if (androidId != null) return androidId

        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        preferences.getString(KEY_FALLBACK_DEVICE_ID, null)?.let { return it }

        return UUID
            .randomUUID()
            .toString()
            .also { generated ->
                preferences.edit().putString(KEY_FALLBACK_DEVICE_ID, generated).apply()
            }
    }

    fun validate(
        context: Context,
        key: String,
        callback: (Result) -> Unit,
    ) {
        val cleanKey = normalizeKey(key)
        if (cleanKey.isBlank()) {
            callback(Result(valid = false, status = "invalid_request"))
            return
        }

        val payload =
            JSONObject()
                .put("key_code", cleanKey)
                .put("device_id", deviceId(context))
                .toString()

        val request =
            Request
                .Builder()
                .url(ENDPOINT)
                .post(payload.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    error: IOException,
                ) {
                    callback(
                        Result(
                            valid = false,
                            status = "network_error",
                            message = error.localizedMessage,
                        ),
                    )
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    response.use {
                        try {
                            val json = JSONObject(it.body?.string().orEmpty())
                            val serverValid = json.optBoolean("valid", false)
                            val product = json.optString("product").takeIf { value -> value.isNotBlank() }
                            val valid = serverValid && product == PRODUCT
                            val status =
                                if (serverValid && product != PRODUCT) {
                                    "invalid"
                                } else {
                                    json.optString("status", "error")
                                }

                            callback(
                                Result(
                                    valid = valid,
                                    status = status,
                                    expiresAt =
                                        json
                                            .optString("expires_at")
                                            .takeIf { value -> value.isNotBlank() },
                                    message =
                                        json
                                            .optString("error")
                                            .takeIf { value -> value.isNotBlank() }
                                            ?: json
                                                .optString("message")
                                                .takeIf { value -> value.isNotBlank() },
                                ),
                            )
                        } catch (error: Exception) {
                            callback(
                                Result(
                                    valid = false,
                                    status = "error",
                                    message = error.localizedMessage,
                                ),
                            )
                        }
                    }
                }
            },
        )
    }

    private fun normalizeKey(key: String): String = key.trim().uppercase(Locale.ROOT)
}
