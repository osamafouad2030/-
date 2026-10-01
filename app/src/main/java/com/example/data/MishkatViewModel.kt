package com.example.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MishkatViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = StudentRepository(database.studentDao())

    // 1. واجهة المستخدم والتوجيه
    private val _currentScreen = MutableStateFlow("auth") // auth, home, teacher_dashboard
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _isEnglish = MutableStateFlow(false)
    val isEnglish: StateFlow<Boolean> = _isEnglish.asStateFlow()

    private val _displayMode = MutableStateFlow("AUTO") // AUTO, MOBILE, TABLET, DESKTOP
    val displayMode: StateFlow<String> = _displayMode.asStateFlow()

    // 2. حالة المصادقة والبيانات المفتوحة
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentUserRole = MutableStateFlow("student") // student, teacher
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    // 3. التدفق المباشر للبيانات من قاعدة بيانات Room
    val studentProgress = repository.studentProgress
    val badges = repository.badges
    val loginLogs = repository.loginLogs

    // 4. حالات حوارات المنبثقات (Dialog States)
    val showBadgesDialog = MutableStateFlow(false)
    val showNewBadgeDialog = MutableStateFlow(false)
    val newUnlockedBadge = MutableStateFlow<BadgeEntity?>(null)
    val showDisplayModeDialog = MutableStateFlow(false)
    val showDailyReminderDialog = MutableStateFlow(false)
    val showRecitationLabDialog = MutableStateFlow(false)

    // 5. حالات المعلم الذكي والقرآني
    private val _tutorResponse = MutableStateFlow("")
    val tutorResponse: StateFlow<String> = _tutorResponse.asStateFlow()

    private val _isTutorLoading = MutableStateFlow(false)
    val isTutorLoading: StateFlow<Boolean> = _isTutorLoading.asStateFlow()

    private val _recitationResponse = MutableStateFlow("")
    val recitationResponse: StateFlow<String> = _recitationResponse.asStateFlow()

    private val _isRecitationLoading = MutableStateFlow(false)
    val isRecitationLoading: StateFlow<Boolean> = _isRecitationLoading.asStateFlow()

    // 6. حالة مزامنة جيت هاب
    private val _syncStatusMessage = MutableStateFlow("")
    val syncStatusMessage: StateFlow<String> = _syncStatusMessage.asStateFlow()

    init {
        // تهيئة البيانات الافتراضية
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
            // استرجاع تفضيلات وضع العرض واللغة من SharedPreferences
            val prefs = getApplication<Application>().getSharedPreferences("mishkat_prefs", Context.MODE_PRIVATE)
            _displayMode.value = prefs.getString("display_mode", "AUTO") ?: "AUTO"
            _isEnglish.value = prefs.getBoolean("is_english", false)
        }
    }

    fun setLanguage(isEn: Boolean) {
        _isEnglish.value = isEn
        val prefs = getApplication<Application>().getSharedPreferences("mishkat_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_english", isEn).apply()
    }

    fun setDisplayMode(mode: String) {
        _displayMode.value = mode
        val prefs = getApplication<Application>().getSharedPreferences("mishkat_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("display_mode", mode).apply()
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // --- المصادقة والفيسبوك ---
    fun login(email: String, role: String) {
        viewModelScope.launch {
            _currentUserEmail.value = email
            _currentUserRole.value = role
            _isLoggedIn.value = true
            _currentScreen.value = if (role == "teacher") "teacher_dashboard" else "home"

            // رصد عملية الدخول في قاعدة البيانات المحلية (Room)
            val deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
            repository.insertLoginLog(
                LoginLog(
                    timestamp = System.currentTimeMillis(),
                    uid = email,
                    device = deviceModel,
                    ip = "192.168.1.104", // محاكاة لعنوان IP الأكاديمي
                    status = "ناجح"
                )
            )

            // إطلاق التنبيه اليومي كترحيب تفاعلي
            if (role == "student") {
                showDailyReminderDialog.value = true
            }
        }
    }

    fun loginWithFacebook() {
        viewModelScope.launch {
            // محاكاة مصادقة فيسبوك وتكاملها مع Firebase Credential
            _currentUserEmail.value = "fb.student.99@facebook.com"
            _currentUserRole.value = "student"
            _isLoggedIn.value = true
            _currentScreen.value = "home"
            showDailyReminderDialog.value = true

            val deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (FB Connect)"
            repository.insertLoginLog(
                LoginLog(
                    timestamp = System.currentTimeMillis(),
                    uid = "fb.student.99@facebook.com",
                    device = deviceModel,
                    ip = "172.56.21.90",
                    status = "ناجح (فيسبوك)"
                )
            )
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUserEmail.value = ""
        _currentUserRole.value = "student"
        _currentScreen.value = "auth"
    }

    // --- عمليات التقدّم والمدارسة ---
    fun completeLesson() {
        viewModelScope.launch {
            val progress = repository.getProgress() ?: return@launch
            val updatedLessons = progress.completedLessonsCount + 1
            
            // رفع المستوى بناءً على عدد الدروس المكتملة تلقائياً ليعكس مستويات الدراسة الأربعة
            val updatedLevel = when {
                updatedLessons >= 12 -> "باحث متقن"
                updatedLessons >= 8 -> "متقدم"
                updatedLessons >= 5 -> "متوسط"
                else -> "مبتدئ"
            }

            // تحديث الرتبة الأكاديمية
            val updatedRank = when {
                updatedLessons >= 12 -> "العلامة الأثري"
                updatedLessons >= 8 -> "المحقق المجتهد"
                updatedLessons >= 5 -> "طالب علم مأذون"
                else -> "قارئ مبادر"
            }

            val updatedProgress = progress.copy(
                completedLessonsCount = updatedLessons,
                level = updatedLevel,
                academicRank = updatedRank
            )

            repository.saveProgress(updatedProgress)

            // فتح وسام "حفظ المتون" (b2) إذا بلغ 5 دروس
            if (updatedLessons == 5) {
                unlockBadgeLocally("b2")
            }
            // فتح وسام "الباحث الأثري" (b4) إذا بلغ 8 دروس
            if (updatedLessons == 8) {
                unlockBadgeLocally("b4")
            }

            // جدولة المزامنة التلقائية مع GitHub
            syncProgressToGitHub()
        }
    }

    fun addPerfectScore() {
        viewModelScope.launch {
            val progress = repository.getProgress() ?: return@launch
            val updatedPerfects = progress.perfectScoresCount + 1
            repository.saveProgress(progress.copy(perfectScoresCount = updatedPerfects))
        }
    }

    private suspend fun unlockBadgeLocally(badgeId: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        repository.unlockBadge(badgeId, today)
        
        // جلب تفاصيل الوسام لعرض نافذة التهنئة المنبثقة التفاعلية NewBadgeUnlockedDialog
        val allBadges = repository.getAllBadges()
        val unlocked = allBadges.find { it.id == badgeId }
        if (unlocked != null) {
            newUnlockedBadge.value = unlocked
            showNewBadgeDialog.value = true
        }
    }

    // --- استدعاءات الذكاء الاصطناعي Gemini ---
    fun askTutorQuestion(question: String) {
        viewModelScope.launch {
            if (question.isBlank()) return@launch
            _isTutorLoading.value = true
            _tutorResponse.value = ""

            val progress = repository.getProgress()
            val currentLevel = progress?.level ?: "مبتدئ"

            val result = GeminiService.askTutor(question, currentLevel)
            _tutorResponse.value = result
            _isTutorLoading.value = false
        }
    }

    fun evaluateRecitationVoice(expectedVerse: String, voiceInputText: String) {
        viewModelScope.launch {
            if (voiceInputText.isBlank()) return@launch
            _isRecitationLoading.value = true
            _recitationResponse.value = ""

            val result = GeminiService.evaluateRecitation(expectedVerse, voiceInputText)
            _recitationResponse.value = result
            _isRecitationLoading.value = false

            // إذا تضمن التقييم مدحاً قوياً أو درجة كاملة، نفتح وسام "الترتيل المتقن" أو "تجويد متواصل"
            if (result.contains("100") || result.contains("9") || result.contains("ممتاز")) {
                unlockBadgeLocally("b3")
                addPerfectScore()
            }
        }
    }

    // --- المزامنة الخارجية مع GitHub ---
    fun syncProgressToGitHub() {
        viewModelScope.launch {
            _syncStatusMessage.value = "جاري إعداد حزمة التقدّم وضغطها..."
            val progress = repository.getProgress()
            if (progress == null) {
                _syncStatusMessage.value = "لا يوجد تقدّم دراسي مسجل لحفظه"
                return@launch
            }

            // تشفير محاكاة لـ PAT المخزن في Secret Manager
            val token = "ghp_MockPersonalAccessTokenMishkatAcademic992817"
            val owner = "MishkatAcademy"
            val repo = "students-data"
            val path = "students/student_01/progress.json"

            // تحويل تقدم الطالب إلى JSON لرفعه
            val payloadObj = JSONObject().apply {
                put("email", progress.email)
                put("level", progress.level)
                put("completed_lessons", progress.completedLessonsCount)
                put("perfect_scores", progress.perfectScoresCount)
                put("rank", progress.academicRank)
                put("timestamp", System.currentTimeMillis())
            }
            val payloadString = payloadObj.toString()

            // استدعاء خدمة المزامنة
            val result = GitHubSyncService.syncToGitHub(
                token = token,
                owner = owner,
                repo = repo,
                path = path,
                contentJson = payloadString
            )

            when (result) {
                is SyncResult.Success -> {
                    _syncStatusMessage.value = "تمت المزامنة وحفظ نسخة احتياطية بنجاح في GitHub!"
                    repository.saveProgress(progress.copy(lastSyncedTimestamp = System.currentTimeMillis()))
                }
                is SyncResult.RateLimited -> {
                    _syncStatusMessage.value = "مؤجل: تجاوز حد الطلبات من GitHub. سيتم الحفظ في طابور الانتظار."
                    repository.addToSyncQueue(payloadString)
                }
                is SyncResult.Conflict -> {
                    _syncStatusMessage.value = "تعارض في النسخ، جاري تحديث المستند المحلي وإعادة المحاولة..."
                    repository.addToSyncQueue(payloadString)
                }
                is SyncResult.NetworkError -> {
                    // حالات الحافة الإلزامية: انقطاع الشبكة أثناء المزامنة -> قائمة انتظار محلية (Room) وإعادة المحاولة
                    _syncStatusMessage.value = "شبكة غير متاحة. تم الإدراج في طابور المزامنة التلقائي المحلي لحفظ تقدمك دون اتصال."
                    repository.addToSyncQueue(payloadString)
                }
                is SyncResult.Failure -> {
                    _syncStatusMessage.value = "فشلت المزامنة: ${result.message}. تم الإدراج في طابور الانتظار."
                    repository.addToSyncQueue(payloadString)
                }
            }
        }
    }

    // محاكاة إفراغ طابور المزامنة في قاعدة البيانات عند عودة الشبكة
    fun retryPendingSyncs() {
        viewModelScope.launch {
            val pending = repository.getPendingSyncs()
            if (pending.isEmpty()) return@launch

            _syncStatusMessage.value = "جاري إعادة محاولة مزامنة ${pending.size} ملفات معلقة من طابور الانتظار..."
            val token = "ghp_MockPersonalAccessTokenMishkatAcademic992817"
            val owner = "MishkatAcademy"
            val repo = "students-data"
            val path = "students/student_01/progress.json"

            var successCount = 0
            for (item in pending) {
                val result = GitHubSyncService.syncToGitHub(
                    token = token,
                    owner = owner,
                    repo = repo,
                    path = path,
                    contentJson = item.payload
                )
                if (result is SyncResult.Success) {
                    repository.deleteFromSyncQueue(item.id)
                    successCount++
                }
            }
            _syncStatusMessage.value = "اكتملت المعالجة. تم مزامنة $successCount ملفات من قائمة الانتظار بنجاح."
        }
    }
}
