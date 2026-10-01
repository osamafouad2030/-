package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.MishkatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsListScreen(
    viewModel: MishkatViewModel,
    isEn: Boolean,
    onOpenTutor: () -> Unit,
    onOpenRecitation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressState by viewModel.studentProgress.collectAsState(initial = null)
    val badgesState by viewModel.badges.collectAsState(initial = emptyList())

    val completedCount = progressState?.completedLessonsCount ?: 0
    val currentLevel = progressState?.level ?: "مبتدئ"
    val academicRank = progressState?.academicRank ?: "قارئ مبادر"
    val totalBadgesEarned = badgesState.count { it.isUnlocked }

    // تعريف الكتب والدروس الموزعة على مستويات الدراسة الأربعة
    val booksSyllabus = listOf(
        BookSyllabusItem(
            titleAr = "كتاب التوحيد",
            titleEn = "Kitab At-Tawheed",
            levelAr = "مبتدئ",
            levelEn = "Beginner",
            lessonsCount = 4,
            descriptionAr = "دراسة المتن العقدي الأساسي في تحقيق العبادة لله وحده وتفنيد الشركيات.",
            descriptionEn = "Essential study of the foundational text on Islamic monotheism."
        ),
        BookSyllabusItem(
            titleAr = "شرح الأجرومية",
            titleEn = "Al-Ajurrumiyyah Explanation",
            levelAr = "متوسط",
            levelEn = "Intermediate",
            lessonsCount = 6,
            descriptionAr = "تعلم قواعد لغة الضاد والنحو العربي لضبط اللسان وفهم معاني النصوص.",
            descriptionEn = "Intermediate Arabic grammar and linguistics study for classical texts."
        ),
        BookSyllabusItem(
            titleAr = "بلوغ المرام",
            titleEn = "Bulugh al-Maram",
            levelAr = "متقدم",
            levelEn = "Advanced",
            lessonsCount = 10,
            descriptionAr = "مذاكرة أدلة الأحكام الفقهية وتخريج الأحاديث النبوية الشريفة وتفصيلها.",
            descriptionEn = "Advanced study of Islamic jurisprudence and Hadith methodology."
        ),
        BookSyllabusItem(
            titleAr = "فتح الباري بشرح البخاري",
            titleEn = "Fath al-Bari (Al-Bukhari)",
            levelAr = "باحث متقن",
            levelEn = "Scholar Specialist",
            lessonsCount = 15,
            descriptionAr = "البحث المقارن والاجتهاد الفقهي المستفيض وتحليل دلالات السنن النبوية.",
            descriptionEn = "Extensive academic research of complex jurisprudence and narration chains."
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("lessons_list_screen")
    ) {
        // بطاقة ملخص تقدم الطالب الأكاديمية الذهبية المتميزة
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEn) "Scholar Progress Dashboard" else "لوحة المدارسة والتقدم العلمي",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEn) "Rank: $academicRank" else "رتبتك: $academicRank",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    // زر فتح الأوسمة
                    Button(
                        onClick = { viewModel.showBadgesDialog.value = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$totalBadgesEarned أوسمة",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // عرض المستوى الدراسي التكيفي
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Academic Level: $currentLevel" else "المستوى الدراسي: $currentLevel",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = if (isEn) "$completedCount Lessons Read" else "أتممت تدارس: $completedCount درساً",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // شريط التقدم العلمي التفاعلي
                val targetLessonsGoal = 15f
                val progressFraction = (completedCount / targetLessonsGoal).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = progressFraction,
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // شريط الأزرار السريعة للتنقل الذكي (المعلم الذكي + مختبر التلاوة)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpenTutor,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("shortcut_tutor_button")
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEn) "Ask AI Tutor" else "اسأل المعلم الذكي",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = onOpenRecitation,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("shortcut_recitation_button")
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEn) "Qur'an Recitation" else "مختبر تلاوة القرآن",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // عنوان مناهج المدارسة المقررة
        Text(
            text = if (isEn) "Prescribed Academic Syllabus:" else "مناهج وكتب المدارسة المقررة:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // قائمة الكتب الشرعية والدروس الممتدة
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(booksSyllabus) { book ->
                val isBookLevelMatched = (book.levelAr == currentLevel)

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBookLevelMatched) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = if (isBookLevelMatched) {
                        androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) book.titleEn else book.titleAr,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBookLevelMatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = if (isEn) "Level: ${book.levelEn}" else "المستوى: ${book.levelAr}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // شارة التوافق مع المستوى الحالي
                            if (isBookLevelMatched) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isEn) "Active" else "مقرر حالياً",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isEn) book.descriptionEn else book.descriptionAr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // تفاصيل الدرس والزر التفاعلي لإكمال الدرس والمذاكرة
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEn) "Total: ${book.lessonsCount} lessons" else "يحتوي على: ${book.lessonsCount} فصول مقررة",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )

                            Button(
                                onClick = { viewModel.completeLesson() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBookLevelMatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("complete_lesson_${book.titleEn.lowercase().replace(" ", "_")}")
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "Complete Lesson" else "تسجيل تدارس وإكمال",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBookLevelMatched) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class BookSyllabusItem(
    val titleAr: String,
    val titleEn: String,
    val levelAr: String,
    val levelEn: String,
    val lessonsCount: Int,
    val descriptionAr: String,
    val descriptionEn: String
)
