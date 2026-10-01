package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Entities

@Entity(tableName = "student_progress")
data class StudentProgress(
    @PrimaryKey val id: Int = 1,
    val email: String,
    val level: String, // Beginner, Intermediate, Advanced, Scholar
    val completedLessonsCount: Int,
    val perfectScoresCount: Int,
    val academicRank: String,
    val lastSyncedTimestamp: Long = 0L
)

@Entity(tableName = "earned_badges")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val type: String, // ACADEMIC or QURANIC
    val isUnlocked: Boolean,
    val unlockedDate: String = ""
)

@Entity(tableName = "login_logs")
data class LoginLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val uid: String,
    val device: String,
    val ip: String,
    val status: String
)

@Entity(tableName = "sync_queue")
data class SyncQueue(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val payload: String, // JSON payload representing progress
    val timestamp: Long
)

// 2. DAOs

@Dao
interface StudentDao {
    @Query("SELECT * FROM student_progress WHERE id = 1")
    fun getProgressFlow(): Flow<StudentProgress?>

    @Query("SELECT * FROM student_progress WHERE id = 1")
    suspend fun getProgress(): StudentProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: StudentProgress)

    @Query("SELECT * FROM earned_badges ORDER BY type DESC")
    fun getBadgesFlow(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM earned_badges")
    suspend fun getAllBadges(): List<BadgeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<BadgeEntity>)

    @Query("UPDATE earned_badges SET isUnlocked = 1, unlockedDate = :date WHERE id = :badgeId")
    suspend fun unlockBadge(badgeId: String, date: String)

    @Query("SELECT * FROM login_logs ORDER BY timestamp DESC")
    fun getLoginLogsFlow(): Flow<List<LoginLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoginLog(log: LoginLog)

    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    suspend fun getPendingSyncs(): List<SyncQueue>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToSyncQueue(item: SyncQueue)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteFromSyncQueue(id: Int)
}

// 3. Database

@Database(
    entities = [StudentProgress::class, BadgeEntity::class, LoginLog::class, SyncQueue::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mishkat_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 4. Repository

class StudentRepository(private val studentDao: StudentDao) {
    val studentProgress: Flow<StudentProgress?> = studentDao.getProgressFlow()
    val badges: Flow<List<BadgeEntity>> = studentDao.getBadgesFlow()
    val loginLogs: Flow<List<LoginLog>> = studentDao.getLoginLogsFlow()

    suspend fun getProgress(): StudentProgress? = studentDao.getProgress()

    suspend fun saveProgress(progress: StudentProgress) {
        studentDao.saveProgress(progress)
    }

    suspend fun insertBadges(badges: List<BadgeEntity>) {
        studentDao.insertBadges(badges)
    }

    suspend fun getAllBadges(): List<BadgeEntity> = studentDao.getAllBadges()

    suspend fun unlockBadge(badgeId: String, date: String) {
        studentDao.unlockBadge(badgeId, date)
    }

    suspend fun insertLoginLog(log: LoginLog) {
        studentDao.insertLoginLog(log)
    }

    suspend fun addToSyncQueue(payload: String) {
        studentDao.addToSyncQueue(SyncQueue(payload = payload, timestamp = System.currentTimeMillis()))
    }

    suspend fun getPendingSyncs(): List<SyncQueue> = studentDao.getPendingSyncs()

    suspend fun deleteFromSyncQueue(id: Int) = studentDao.deleteFromSyncQueue(id)

    suspend fun initializeDefaultDataIfEmpty() {
        val currentProgress = studentDao.getProgress()
        if (currentProgress == null) {
            studentDao.saveProgress(
                StudentProgress(
                    email = "student@mishkat.academy",
                    level = "مبتدئ",
                    completedLessonsCount = 3,
                    perfectScoresCount = 1,
                    academicRank = "قارئ مبادر"
                )
            )
        }

        val currentBadges = studentDao.getAllBadges()
        if (currentBadges.isEmpty()) {
            val defaultBadges = listOf(
                BadgeEntity(
                    id = "b1",
                    titleAr = "طالب مجتهد",
                    titleEn = "Diligent Student",
                    descriptionAr = "أتم تدارس الدرس الأول بالمقرر الأكاديمي بنجاح واقتدار.",
                    descriptionEn = "Successfully completed the first lesson in the academic syllabus.",
                    type = "ACADEMIC",
                    isUnlocked = true,
                    unlockedDate = "2026-09-28"
                ),
                BadgeEntity(
                    id = "b2",
                    titleAr = "حفظ المتون",
                    titleEn = "Text Memorizer",
                    descriptionAr = "أتم الحفظ والمذاكرة لثلاثة متون لغوية وشرعية هامة.",
                    descriptionEn = "Memorized and studied three essential linguistic and religious texts.",
                    type = "ACADEMIC",
                    isUnlocked = false
                ),
                BadgeEntity(
                    id = "b3",
                    titleAr = "الترتيل المتقن",
                    titleEn = "Masterful Reciter",
                    descriptionAr = "حصل على تقييم امتياز 100% في مختبر تلاوة الآيات الكريمة.",
                    descriptionEn = "Earned an Excellent 100% score in the Quranic Recitation Lab.",
                    type = "QURANIC",
                    isUnlocked = true,
                    unlockedDate = "2026-09-30"
                ),
                BadgeEntity(
                    id = "b4",
                    titleAr = "الباحث الأثري",
                    titleEn = "Textual Explorer",
                    descriptionAr = "طرح خمسة أسئلة عميقة وتمت إجابته من قبل المعلم الذكي.",
                    descriptionEn = "Asked five deep scholarly questions answered by the Intelligent AI Tutor.",
                    type = "ACADEMIC",
                    isUnlocked = false
                ),
                BadgeEntity(
                    id = "b5",
                    titleAr = "تجويد متواصل",
                    titleEn = "Consistent Tajweed",
                    descriptionAr = "حافظ على ورد التلاوة والتجويد الصوتي اليومي لثلاثة أيام متتالية.",
                    descriptionEn = "Maintained daily voice-evaluated Tajweed practice for three consecutive days.",
                    type = "QURANIC",
                    isUnlocked = false
                )
            )
            studentDao.insertBadges(defaultBadges)
        }
    }
}
