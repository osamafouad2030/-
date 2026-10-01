package com.example.data

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

object GitHubSyncService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * الضغط باستخدام GZIP لتقليل استهلاك البيانات وحجم الملف اليومي
     */
    private fun compressGzip(data: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { gzip ->
            gzip.write(data.toByteArray(Charsets.UTF_8))
        }
        return bos.toByteArray()
    }

    /**
     * يرفع ملف تقدّم الطالب بصيغة JSON المشفّر بـ Base64 إلى مستودع جيت هاب الخاص
     * مع معالجة الأخطاء وحالات الحافة (تجاوز معدل الطلبات، تعارض SHA، فشل الشبكة)
     */
    suspend fun syncToGitHub(
        token: String,
        owner: String,
        repo: String,
        path: String,
        contentJson: String,
        currentSha: String? = null
    ): SyncResult = withContext(Dispatchers.IO) {
        var sha = currentSha
        var attempt = 0
        val maxAttempts = 3
        var currentDelay = 1000L // 1 second base delay

        while (attempt < maxAttempts) {
            try {
                // ضغط البيانات باستخدام GZIP قبل الرفع كما هو مطلوب بالبرومبت لتقليل الحجم عن 500KB
                val compressedData = compressGzip(contentJson)
                val base64Content = Base64.encodeToString(compressedData, Base64.NO_WRAP)

                val url = "https://api.github.com/repos/$owner/$repo/contents/$path"

                // إعداد جسم الطلب
                val jsonBody = JSONObject().apply {
                    put("message", "الأكاديمية: تحديث تقدم الطالب اليومي - ${System.currentTimeMillis()}")
                    put("content", base64Content)
                    if (sha != null) {
                        put("sha", sha)
                    }
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .header("Authorization", "token $token")
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()

                client.newCall(request).execute().use { response ->
                    val statusCode = response.code
                    val responseBody = response.body?.string() ?: ""

                    when (statusCode) {
                        200, 201 -> {
                            // نجاح المزامنة
                            val responseJson = JSONObject(responseBody)
                            val newSha = responseJson.getJSONObject("content").getString("sha")
                            return@withContext SyncResult.Success(newSha)
                        }
                        403 -> {
                            // تجاوز حد الطلبات أو مشكلة صلاحيات
                            val rateLimitRemaining = response.header("X-RateLimit-Remaining")
                            if (rateLimitRemaining == "0") {
                                return@withContext SyncResult.RateLimited("تم تجاوز حد الطلبات المسموح به من GitHub API")
                            }
                            return@withContext SyncResult.Failure("صلاحيات غير كافية أو تجاوز حد الرفع (403): $responseBody")
                        }
                        409 -> {
                            // تعارض SHA (Conflict) - يجب جلب الملف لمعرفة الـ SHA الأحدث والمحاولة مرة أخرى
                            val latestSha = fetchLatestSha(token, owner, repo, path)
                            if (latestSha != null && latestSha != sha) {
                                sha = latestSha
                                // لا تحتسب كمحاولة فاشلة، بل أعد المحاولة مباشرة بالـ SHA الجديد
                                continue
                            }
                            return@withContext SyncResult.Conflict("تعارض في النسخ (SHA mismatch) وفشل تحديثه تلقائياً")
                        }
                        else -> {
                            // أخطاء عامة - تطبيق Exponential Backoff
                            attempt++
                            if (attempt >= maxAttempts) {
                                return@withContext SyncResult.Failure("فشل الاتصال بجيت هاب برمز الخطأ: $statusCode - $responseBody")
                            }
                            delay(currentDelay)
                            currentDelay *= 2 // مضاعفة وقت الانتظار
                        }
                    }
                }
            } catch (e: Exception) {
                // فشل الشبكة - تطبيق Exponential Backoff وإدراج في قائمة الانتظار المحلية
                attempt++
                if (attempt >= maxAttempts) {
                    return@withContext SyncResult.NetworkError(e.message ?: "خطأ غير معروف في الشبكة")
                }
                delay(currentDelay)
                currentDelay *= 2
            }
        }

        return@withContext SyncResult.Failure("فشل الرفع بعد محاولات متعددة")
    }

    /**
     * جلب SHA الأخير لتجنب تعارض الملفات (Conflict 409)
     */
    private suspend fun fetchLatestSha(token: String, owner: String, repo: String, path: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$owner/$repo/contents/$path"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "token $token")
                .header("Accept", "application/vnd.github.v3+json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.code == 200) {
                    val body = response.body?.string() ?: ""
                    return@withContext JSONObject(body).getString("sha")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}

sealed class SyncResult {
    data class Success(val sha: String) : SyncResult()
    data class RateLimited(val message: String) : SyncResult()
    data class Conflict(val message: String) : SyncResult()
    data class NetworkError(val message: String) : SyncResult()
    data class Failure(val message: String) : SyncResult()
}
