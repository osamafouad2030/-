package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.BadgeEntity
import com.example.data.MishkatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentBadgesDialog(
    viewModel: MishkatViewModel,
    isEn: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val progressState by viewModel.studentProgress.collectAsState(initial = null)
    val badgesState by viewModel.badges.collectAsState(initial = emptyList())

    // تصفية الأوسمة
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, ACADEMIC, QURANIC

    val filteredBadges = when (selectedFilter) {
        "ACADEMIC" -> badgesState.filter { it.type == "ACADEMIC" }
        "QURANIC" -> badgesState.filter { it.type == "QURANIC" }
        else -> badgesState
    }

    val totalEarned = badgesState.count { it.isUnlocked }
    val lessonsCompleted = progressState?.completedLessonsCount ?: 0
    val perfectScores = progressState?.perfectScoresCount ?: 0
    val academicRank = progressState?.academicRank ?: "طالب مبادر"

    // تحديد الاتجاه تلقائياً بناءً على اختيار اللغة i18n
    val layoutDirection = if (isEn) LayoutDirection.Ltr else LayoutDirection.Rtl

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .testTag("student_badges_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // شريط علوي للنافذة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Academic Accomplishments" else "الأوسمة والتقدم الأكاديمي",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // الرتبة والمستوى الأكاديمي
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.secondary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Rank",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (isEn) "Current Academic Rank:" else "الرتبة الأكاديمية والقرآنية:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isEn) {
                                        when (academicRank) {
                                            "العلامة الأثري" -> "Grand Scholarly Explorer"
                                            "المحقق المجتهد" -> "Diligent Researcher"
                                            "طالب علم مأذون" -> "Authorized Seeker of Knowledge"
                                            else -> "Initiator Reader"
                                        }
                                    } else academicRank,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // بطاقات الإحصائيات (الأوسمة المحققة / الدروس المكتملة / علامات 100%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            label = if (isEn) "Badges" else "الأوسمة",
                            value = totalEarned.toString(),
                            icon = Icons.Default.EmojiEvents,
                            backgroundColor = Color(0xFFC9A227).copy(alpha = 0.15f),
                            contentColor = Color(0xFFC9A227),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = if (isEn) "Lessons" else "الدروس",
                            value = lessonsCompleted.toString(),
                            icon = Icons.Default.Book,
                            backgroundColor = Color(0xFF0F5132).copy(alpha = 0.15f),
                            contentColor = Color(0xFF0F5132),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = if (isEn) "Perfect 100%" else "تلاوات 100%",
                            value = perfectScores.toString(),
                            icon = Icons.Default.Star,
                            backgroundColor = Color(0xFF1B2A4A).copy(alpha = 0.15f),
                            contentColor = Color(0xFF1B2A4A),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // أزرار الفلترة (تصفية الأوسمة)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = (selectedFilter == "ALL"),
                            onClick = { selectedFilter = "ALL" },
                            label = { Text(if (isEn) "All" else "الكل") },
                            modifier = Modifier.testTag("filter_all")
                        )
                        FilterChip(
                            selected = (selectedFilter == "ACADEMIC"),
                            onClick = { selectedFilter = "ACADEMIC" },
                            label = { Text(if (isEn) "Academic" else "أكاديمي") },
                            modifier = Modifier.testTag("filter_academic")
                        )
                        FilterChip(
                            selected = (selectedFilter == "QURANIC"),
                            onClick = { selectedFilter = "QURANIC" },
                            label = { Text(if (isEn) "Qur'anic" else "قرآني") },
                            modifier = Modifier.testTag("filter_quranic")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // قائمة الأوسمة التفاعلية
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredBadges) { badge ->
                            BadgeListItem(badge = badge, isEn = isEn)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // مشاركة الإنجازات والأوسمة
                    Button(
                        onClick = {
                            val shareMessage = if (isEn) {
                                "By the Grace of Allah, I achieved the academic rank of ($academicRank) and unlocked $totalEarned scholastic badges on Mishkat Al-Durr Al-Maknun educational platform!"
                            } else {
                                "بفضل الله ومنّته، أحرزت الرتبة الأكاديمية ($academicRank) وحصلت على $totalEarned أوسمة علمية وقرآنية في منصة مشكاة الدر المكنون لمدارسة العلم الشرعي!"
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "إنجاز علمي مبارك")
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(Intent.createChooser(intent, "مشاركة الأوسمة"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("share_badges_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEn) "Share Academic Accomplishments" else "مشاركة سجل الإنجازات والأوسمة",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = contentColor)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BadgeListItem(badge: BadgeEntity, isEn: Boolean) {
    val title = if (isEn) badge.titleEn else badge.titleAr
    val desc = if (isEn) badge.descriptionEn else badge.descriptionAr

    val iconColor = if (badge.isUnlocked) {
        if (badge.type == "QURANIC") Color(0xFF0F5132) else Color(0xFFC9A227)
    } else {
        Color.Gray.copy(alpha = 0.5f)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (badge.isUnlocked) {
            androidx.compose.foundation.BorderStroke(1.dp, iconColor.copy(alpha = 0.3f))
        } else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (badge.isUnlocked) iconColor.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (badge.type == "QURANIC") Icons.Default.MenuBook else Icons.Default.MilitaryTech,
                    contentDescription = title,
                    tint = iconColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (badge.isUnlocked && badge.unlockedDate.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEn) "Unlocked: ${badge.unlockedDate}" else "نيل الشرف في: ${badge.unlockedDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0F5132),
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (!badge.isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEn) "Locked - complete syllabus" else "مغلق - لم يتحقق الشرط الأكاديمي",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
