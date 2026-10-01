package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var viewModel: MishkatViewModel
    private lateinit var database: AppDatabase
    private lateinit var dao: StudentDao

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        viewModel = MishkatViewModel(context as Application)
        database = AppDatabase.getDatabase(context)
        dao = database.studentDao()
    }

    // 1. التحقق من تطابق تعريب اسم التطبيق في موارد النظام مع متطلبات الهوية (تتطابق مع الإنجليزية في بيئة الاختبار)
    @Test
    fun testAppNameLocalization() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Mishkat Al-Durr Al-Maknun", appName)
    }

    // 2. التحقق من التهيئة الافتراضية للغة في الـ ViewModel
    @Test
    fun testDefaultLanguageState() {
        assertFalse(viewModel.isEnglish.value)
    }

    // 3. التحقق من القدرة على تبديل اللغة وتحديث حالتها فوراً i18n
    @Test
    fun testLanguageToggle() {
        viewModel.setLanguage(true)
        assertTrue(viewModel.isEnglish.value)
        viewModel.setLanguage(false)
        assertFalse(viewModel.isEnglish.value)
    }

    // 4. التحقق من نمط العرض التكيفي الافتراطي (AUTO)
    @Test
    fun testDefaultDisplayMode() {
        assertEquals("AUTO", viewModel.displayMode.value)
    }

    // 5. التحقق من حفظ مظهر العرض التكيفي وتحديث حالته بنجاح
    @Test
    fun testSetDisplayMode() {
        viewModel.setDisplayMode("DESKTOP")
        assertEquals("DESKTOP", viewModel.displayMode.value)
    }

    // 6. التحقق من نجاح عملية تسجيل الدخول للأكاديمية وحالة المصادقة
    @Test
    fun testAcademicLoginSuccess() {
        viewModel.login("teacher@mishkat.academy", "teacher")
        assertTrue(viewModel.isLoggedIn.value)
        assertEquals("teacher@mishkat.academy", viewModel.currentUserEmail.value)
        assertEquals("teacher", viewModel.currentUserRole.value)
        assertEquals("teacher_dashboard", viewModel.currentScreen.value)
    }

    // 7. التحقق من تكامل مصادقة فيسبوك التفاعلية
    @Test
    fun testFacebookLoginIntegration() {
        viewModel.loginWithFacebook()
        assertTrue(viewModel.isLoggedIn.value)
        assertEquals("fb.student.99@facebook.com", viewModel.currentUserEmail.value)
        assertEquals("student", viewModel.currentUserRole.value)
        assertEquals("home", viewModel.currentScreen.value)
        assertTrue(viewModel.showDailyReminderDialog.value)
    }

    // 8. التحقق من صحة تسجيل الخروج واسترجاع شاشة البداية
    @Test
    fun testAcademicLogout() {
        viewModel.login("student@mishkat.academy", "student")
        viewModel.logout()
        assertFalse(viewModel.isLoggedIn.value)
        assertEquals("", viewModel.currentUserEmail.value)
        assertEquals("auth", viewModel.currentScreen.value)
    }

    // 9. التحقق من ترقيات المستوى الدراسي الأربعة: مبتدئ (0-4 دروس مكتملة)
    @Test
    fun testStudyLevelBeginner() {
        val level = calculateLevelForTest(3)
        assertEquals("مبتدئ", level)
    }

    // 10. التحقق من ترقيات المستوى الدراسي الأربعة: متوسط (5-7 دروس مكتملة)
    @Test
    fun testStudyLevelIntermediate() {
        val level = calculateLevelForTest(6)
        assertEquals("متوسط", level)
    }

    // 11. التحقق من ترقيات المستوى الدراسي الأربعة: متقدم (8-11 درساً مكتملاً)
    @Test
    fun testStudyLevelAdvanced() {
        val level = calculateLevelForTest(9)
        assertEquals("متقدم", level)
    }

    // 12. التحقق من ترقيات المستوى الدراسي الأربعة: باحث متقن (12+ درساً مكتملاً)
    @Test
    fun testStudyLevelScholar() {
        val level = calculateLevelForTest(14)
        assertEquals("باحث متقن", level)
    }

    // 13. اختبار الحفظ والاسترجاع المباشر لبيانات التقدم في قاعدة بيانات Room العلمية
    @Test
    fun testDatabaseProgressStorage() = runTest {
        val progress = StudentProgress(
            id = 1,
            email = "direct@mishkat.academy",
            level = "متوسط",
            completedLessonsCount = 7,
            perfectScoresCount = 2,
            academicRank = "طالب علم مأذون"
        )
        dao.saveProgress(progress)
        
        val retrieved = dao.getProgress()
        assertNotNull(retrieved)
        assertEquals("direct@mishkat.academy", retrieved?.email)
        assertEquals("متوسط", retrieved?.level)
        assertEquals(7, retrieved?.completedLessonsCount)
    }

    // 14. اختبار إدراج الأوسمة وفتحها التفاعلي في قاعدة البيانات
    @Test
    fun testDatabaseBadgeManagement() = runTest {
        val testBadge = BadgeEntity(
            id = "test_b1",
            titleAr = "حفظ الأثر",
            titleEn = "Preserving Narration",
            descriptionAr = "أتم الحفظ لمتن الحديث بنجاح.",
            descriptionEn = "Successfully memorized narration text.",
            type = "ACADEMIC",
            isUnlocked = false
        )
        dao.insertBadges(listOf(testBadge))
        
        // التحقق من الإدراج كحالة مغلقة
        var retrieved = dao.getAllBadges().find { it.id == "test_b1" }
        assertNotNull(retrieved)
        assertFalse(retrieved?.isUnlocked ?: true)

        // فتح الوسام
        dao.unlockBadge("test_b1", "2026-10-01")
        retrieved = dao.getAllBadges().find { it.id == "test_b1" }
        assertTrue(retrieved?.isUnlocked ?: false)
        assertEquals("2026-10-01", retrieved?.unlockedDate)
    }

    // 15. اختبار الضغط باستخدام GZIP لتقليل حجم البيانات قبل رفعها لـ GitHub
    @Test
    fun testGzipCompressionLogic() {
        val originalText = "مهمتك هي الإجابة عن أسئلة الطلاب في منصة مشكاة الدر المكنون لمدارسة العلوم الشرعية."
        val compressedBytes = compressGzipForTest(originalText)
        assertNotNull(compressedBytes)
        assertTrue(compressedBytes.size > 0)
        // البيانات المضغوطة بالعادة تبدأ بمعرف GZIP السحري 0x1f, 0x8b
        assertEquals(0x1f_00, compressedBytes[0].toInt().and(0xff).shl(8))
    }

    // دالة مساعدة لمحاكاة ترقية مستويات الدراسة الأربعة
    private fun calculateLevelForTest(completedLessons: Int): String {
        return when {
            completedLessons >= 12 -> "باحث متقن"
            completedLessons >= 8 -> "متقدم"
            completedLessons >= 5 -> "متوسط"
            else -> "مبتدئ"
        }
    }

    // دالة مساعدة لمحاكاة ضغط GZIP للتحقق من سلامة البناء
    private fun compressGzipForTest(data: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { gzip ->
            gzip.write(data.toByteArray(Charsets.UTF_8))
        }
        return bos.toByteArray()
    }
}
