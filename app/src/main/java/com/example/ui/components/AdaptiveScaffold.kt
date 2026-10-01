package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.ui.screens.LessonsListScreen
import com.example.ui.screens.RecitationLabScreen
import com.example.ui.screens.SmartTutorScreen
import com.example.ui.screens.TeacherDashboardScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveScaffold(
    viewModel: MishkatViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    val isEn by viewModel.isEnglish.collectAsState()
    val displayMode by viewModel.displayMode.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    // الحوارات المنبثقة التفاعلية
    val showBadges by viewModel.showBadgesDialog.collectAsState()
    val showNewBadge by viewModel.showNewBadgeDialog.collectAsState()
    val newUnlockedBadge by viewModel.newUnlockedBadge.collectAsState()
    val showDisplayMode by viewModel.showDisplayModeDialog.collectAsState()
    val showDailyReminder by viewModel.showDailyReminderDialog.collectAsState()

    // التبويبات النشطة داخلياً في الشاشة الرئيسية لغير المعلم
    var activeStudentTab by remember { mutableStateOf("lessons") } // lessons, tutor, recitation

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthDp = constraints.maxWidth / (LocalDensity ?: 2.5f) // محاكاة dp أو استخدام قياسات العرض المباشرة
        val screenWidthDp = maxWidth // قياس العرض بالـ dp مباشرة من BoxWithConstraints

        // تحديد نمط التخطيط التكيفي (Mobile, Tablet, Desktop)
        val layoutType = when (displayMode) {
            "MOBILE" -> "MOBILE"
            "TABLET" -> "TABLET"
            "DESKTOP" -> "DESKTOP"
            else -> {
                // الوضع التلقائي AUTO بناءً على قياسات الشاشة الفعلية
                when {
                    screenWidthDp < 600.dp -> "MOBILE"
                    screenWidthDp <= 1024.dp -> "TABLET"
                    else -> "DESKTOP"
                }
            }
        }

        Scaffold(
            topBar = {
                // شريط علوي تكيفي موحد للمنصة
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isEn) "Mishkat Al-Durr" else "مشكاة الدر المكنون",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        // 1. تبديل اللغة الفوري i18n
                        TextButton(
                            onClick = { viewModel.setLanguage(!isEn) },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("language_toggle_button")
                        ) {
                            Text(
                                text = if (isEn) "العربية" else "English",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 2. منتقي المظهر ووضع العرض التكيفي
                        IconButton(
                            onClick = { viewModel.showDisplayModeDialog.value = true },
                            modifier = Modifier.testTag("app_display_mode_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Display Mode Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // 3. زر تسجيل الخروج الأكاديمي
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("logout_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.Red
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            bottomBar = {
                // يظهر شريط التنقل السفلي فقط في الهاتف المحمول وللطلاب
                if (layoutType == "MOBILE" && currentUserRole == "student") {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("mobile_bottom_navigation")
                    ) {
                        NavigationBarItem(
                            selected = (activeStudentTab == "lessons"),
                            onClick = { activeStudentTab = "lessons" },
                            icon = { Icon(imageVector = Icons.Default.Book, contentDescription = "Syllabus") },
                            label = { Text(if (isEn) "Syllabus" else "المقررات") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary)
                        )
                        NavigationBarItem(
                            selected = (activeStudentTab == "tutor"),
                            onClick = { activeStudentTab = "tutor" },
                            icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Tutor") },
                            label = { Text(if (isEn) "AI Tutor" else "المعلم الذكي") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary)
                        )
                        NavigationBarItem(
                            selected = (activeStudentTab == "recitation"),
                            onClick = { activeStudentTab = "recitation" },
                            icon = { Icon(imageVector = Icons.Default.Mic, contentDescription = "Recitation") },
                            label = { Text(if (isEn) "Recitation" else "المختبر") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // التنقل الجانبي للأجهزة اللوحية والمكتبية (Navigation Rail)
                if (layoutType != "MOBILE") {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("adaptive_navigation_rail")
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (currentUserRole == "student") {
                            NavigationRailItem(
                                selected = (activeStudentTab == "lessons"),
                                onClick = { activeStudentTab = "lessons" },
                                icon = { Icon(imageVector = Icons.Default.Book, contentDescription = "Syllabus") },
                                label = { Text(if (isEn) "Syllabus" else "المقررات") }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            NavigationRailItem(
                                selected = (activeStudentTab == "tutor"),
                                onClick = { activeStudentTab = "tutor" },
                                icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Tutor") },
                                label = { Text(if (isEn) "AI Tutor" else "المعلم الذكي") }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            NavigationRailItem(
                                selected = (activeStudentTab == "recitation"),
                                onClick = { activeStudentTab = "recitation" },
                                icon = { Icon(imageVector = Icons.Default.Mic, contentDescription = "Recitation") },
                                label = { Text(if (isEn) "Recitation" else "المختبر") }
                            )
                        } else {
                            // تبويبات المعلم
                            NavigationRailItem(
                                selected = true,
                                onClick = {},
                                icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text(if (isEn) "Dashboard" else "الإدارة") }
                            )
                        }
                    }
                }

                // عرض المحتوى الفعلي بناءً على نوع اللياقة والتخطيط والتبويبات
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (currentUserRole == "teacher") {
                        // لوحة إدارة المعلم مأخوذة بكامل العرض أو بحد أقصى مريح
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 1000.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            TeacherDashboardScreen(viewModel = viewModel, isEn = isEn)
                        }
                    } else {
                        // شاشة الطالب التكيفية
                        when (layoutType) {
                            "DESKTOP" -> {
                                // نمط المكتب (> 1024dp): SplitView (فهرس المقررات 44% / معلم ذكي وتلاوة 56%) لضمان توازن الأبعاد
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(0.44f)
                                            .fillMaxHeight()
                                    ) {
                                        LessonsListScreen(
                                            viewModel = viewModel,
                                            isEn = isEn,
                                            onOpenTutor = { activeStudentTab = "tutor" },
                                            onOpenRecitation = { activeStudentTab = "recitation" }
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(0.56f)
                                            .fillMaxHeight()
                                    ) {
                                        if (activeStudentTab == "recitation") {
                                            RecitationLabScreen(viewModel = viewModel, isEn = isEn)
                                        } else {
                                            SmartTutorScreen(viewModel = viewModel, isEn = isEn)
                                        }
                                    }
                                }
                            }
                            "TABLET" -> {
                                // نمط الجهاز اللوحي (600-1024dp): SplitView مع حد قراءة أقصى 760dp
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .widthIn(max = 760.dp)
                                        .align(Alignment.TopCenter)
                                ) {
                                    if (activeStudentTab == "lessons") {
                                        LessonsListScreen(
                                            viewModel = viewModel,
                                            isEn = isEn,
                                            onOpenTutor = { activeStudentTab = "tutor" },
                                            onOpenRecitation = { activeStudentTab = "recitation" }
                                        )
                                    } else if (activeStudentTab == "tutor") {
                                        SmartTutorScreen(viewModel = viewModel, isEn = isEn)
                                    } else {
                                        RecitationLabScreen(viewModel = viewModel, isEn = isEn)
                                    }
                                }
                            }
                            else -> {
                                // الهاتف المحمول: عرض أحادي بالتبويبات
                                when (activeStudentTab) {
                                    "lessons" -> LessonsListScreen(
                                        viewModel = viewModel,
                                        isEn = isEn,
                                        onOpenTutor = { activeStudentTab = "tutor" },
                                        onOpenRecitation = { activeStudentTab = "recitation" }
                                    )
                                    "tutor" -> SmartTutorScreen(viewModel = viewModel, isEn = isEn)
                                    "recitation" -> RecitationLabScreen(viewModel = viewModel, isEn = isEn)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- معالجة ظهور الحوارات المنبثقة التفاعلية والموطّنة بالكامل ---
        if (showBadges) {
            StudentBadgesDialog(
                viewModel = viewModel,
                isEn = isEn,
                onDismiss = { viewModel.showBadgesDialog.value = false }
            )
        }

        if (showNewBadge && newUnlockedBadge != null) {
            NewBadgeUnlockedDialog(
                badge = newUnlockedBadge!!,
                isEn = isEn,
                onReviewBadges = { viewModel.showBadgesDialog.value = true },
                onDismiss = { viewModel.showNewBadgeDialog.value = false }
            )
        }

        if (showDisplayMode) {
            DisplayModeDialog(
                viewModel = viewModel,
                isEn = isEn,
                onDismiss = { viewModel.showDisplayModeDialog.value = false }
            )
        }

        if (showDailyReminder) {
            DailyReminderDialog(
                isEn = isEn,
                onStartStudy = { activeStudentTab = "lessons" },
                onDismiss = { viewModel.showDailyReminderDialog.value = false }
            )
        }
    }
}

// مساعد جلب الكثافة التوضيحي للـ layoutBuilder
val LocalDensity: Float?
    @Composable
    get() = androidx.compose.ui.platform.LocalDensity.current.density
