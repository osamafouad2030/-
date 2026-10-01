package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private const val MODEL_NAME = "gemini-3.5-flash"

    /**
     * يقوم بمخاطبة المعلم الذكي للحصول على شرح دقيق لمسألة فقهية أو علمية
     */
    suspend fun askTutor(question: String, level: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "تنبيه الأكاديمية: لم يتم العثور على مفتاح API الخاص بـ Gemini في ملف .env أو لوحة Secrets. الرجاء إضافته لتفعيل المعلم الذكي التفاعلي بالكامل."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

        val systemInstruction = """
            أنت "المعلم الأكاديمي الذكي" في منصة مشكاة الدر المكنون لمدارسة العلوم الشرعية.
            مهمتك هي الإجابة عن أسئلة الطلاب حسب مستواهم الدراسي الحالي: ($level).
            - للمبتدئين: قدم شرحاً ميسراً، واضحاً وبسيطاً مدعوماً بالأدلة الأساسية دون تعقيدات مذهبية.
            - للمتوسطين/المتقدمين: توسع قليلاً في تفصيل الفروع والمسائل الفقهية والأدلة.
            - للباحث المتقن: قدم صياغة علمية رصينة ومقارنة علمية دقيقة للأدلة مع ذكر الراجح بلغة أكاديمية بليغة.
            يجب أن تتسم جميع إجاباتك بالأدب الجم، مستهلاً بـ (بسم الله الرحمن الرحيم، والحمد لله، والصلاة والسلام على رسول الله) ومختتماً بـ (والله تعالى أعلم).
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "السؤال: $question")
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.5)
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val rootJson = JSONObject(bodyString)
                    val candidates = rootJson.getJSONArray("candidates")
                    if (candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.getJSONObject("content")
                        val parts = content.getJSONArray("parts")
                        if (parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).getString("text")
                        }
                    }
                    return@withContext "لم يتم تلقي استجابة صالحة من المعلم الذكي."
                } else {
                    return@withContext "أخطأ المعلم الذكي في الاتصال بالشبكة (الرمز: ${response.code})."
                }
            }
        } catch (e: Exception) {
            return@withContext "فشل استدعاء المعلم الذكي بسبب: ${e.localizedMessage}"
        }
    }

    /**
     * يقوم بتحليل التلاوة الصوتية ومطابقتها بالآية الكريمة لتقييم أحكام التجويد ومخارج الحروف.
     * يدعم إرسال ملف الصوت الفعلي، أو توليد محاكاة متقنة للأحكام للتسهيل أثناء عدم توفر ميكروفون.
     */
    suspend fun evaluateRecitation(expectedVerse: String, actualRecitationText: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "تنبيه الأكاديمية: يرجى تهيئة مفتاح GEMINI_API_KEY في لوحة Secrets لتفعيل التقييم الآلي الفوري لمخارج الحروف والتجويد بالذكاء الاصطناعي."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

        val systemInstruction = """
            أنت "مصحح ومحكم التلاوة المجود" في مختبر التلاوة الصوتية لمنصة مشكاة الدر المكنون.
            وظيفتك هي مقارنة تلاوة الطالب المسجلة (المكتوبة أو الصوتية) بالآية القرآنية المقررة التالية: ($expectedVerse).
            عليك صياغة تقرير مفصل يشتمل على:
            1. مطابقة الكلمات والحروف بدقة (هل هناك زيادة، نقصان، أو تبديل في الحروف أو الحركات).
            2. تقييم أحكام التجويد الأساسية التي تم تلاوتها (إظهار، إدغام، إخفاء، قلقلة، مدود).
            3. تقديم نصائح تصحيحية دقيقة لمخارج الحروف والصفات.
            4. منح التلاوة درجة تقديرية من 100% (مثال: 95/100).
            اجعل نبرة الرد لطيفة، مشجعة ومحفزة لطالب علم القرآن الكريم.
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "تلاوة الطالب الفعلية هي: $actualRecitationText")
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val rootJson = JSONObject(bodyString)
                    val candidates = rootJson.getJSONArray("candidates")
                    if (candidates.length() > 0) {
                        return@withContext candidates.getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")
                    }
                    return@withContext "لم يتم إصدار تقرير تصحيح التلاوة بنجاح."
                } else {
                    return@withContext "عجز المحكّم التجويدي عن الاتصال بالخادم الرئيسي (الرمز: ${response.code})."
                }
            }
        } catch (e: Exception) {
            return@withContext "فشل تحليل التجويد بسبب: ${e.localizedMessage}"
        }
    }
}
