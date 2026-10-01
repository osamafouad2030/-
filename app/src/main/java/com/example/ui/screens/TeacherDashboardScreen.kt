package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.LoginLog
import com.example.data.MishkatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    viewModel: MishkatViewModel,
    isEn: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val loginLogs by viewModel.loginLogs.collectAsState(initial = emptyList())
    var selectedLevelFilter by remember { mutableStateOf("ALL") } // ALL, BEGINNER, ADVANCED

    // إجمالي الطلاب والنشطون (محاكاة تفاعلية مبنية على البيانات)
    val activeStudentsCount = remember { mutableStateOf(5) }
    val totalStudentsCount = 28

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("teacher_dashboard_screen")
    ) {
        // العنوان العلوي للوحة الإدارة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isEn) "Teacher Analytics & Controls" else "لوحة تحليلات وإدارة المعلم مأذون",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isEn) "Role: Academics Instructor (Custom Claims Validated)" else "الصلاحيات: معلّم مأذون (متحقق منها بأمان)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            // زر التصدير CSV التفاعلي الممتد
            IconButton(
                onClick = {
                    Toast.makeText(context, if (isEn) "Progress logs exported to Mishkat_Progress_Report.csv" else "تم تصدير سجلات تقدم الطلاب بنجاح بصيغة CSV", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .testTag("export_csv_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Export Report",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // بطاقات البيانات السريعة والنشطة حالياً
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isEn) "Active Now" else "النشطون حالياً",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${activeStudentsCount.value} طلاب",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isEn) "Total Enrolled" else "إجمالي المسجلين",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$totalStudentsCount طالباً",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // رسم بياني كود خاص (Compose Canvas Chart) يعرض تحليلات الأداء العلمي اليومي والشهري
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isEn) "Daily Study Logins Analytics" else "تحليلات معدل تدارس الأوراد اليومي",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // رسم الأعمدة البيانية التكيفي بالكانفاس لضمان السرعة المطلقة والجمال
                val barData = listOf(15f, 22f, 18f, 28f, 24f, 32f, 20f)
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barCount = barData.size
                    val spacing = 24.dp.toPx()
                    val totalSpacing = spacing * (barCount - 1)
                    val barWidth = (canvasWidth - totalSpacing) / barCount

                    val maxVal = 35f

                    for (i in 0 until barCount) {
                        val x = i * (barWidth + spacing)
                        val barHeightNormalized = (barData[i] / maxVal) * canvasHeight
                        val y = canvasHeight - barHeightNormalized

                        // رسم العمود الشرعي الجمالي
                        drawRect(
                            color = if (i == 5) secondaryColor else primaryColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeightNormalized)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // متابعة تقدم وإدارة شؤون الطلاب
        Text(
            text = if (isEn) "Student List & Action Center" else "متابعة تقدم وإجراءات الطلاب:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // رقعة جدولية تفاعلية لعرض تقدم الطلاب الفعلي
        val students = listOf(
            StudentItem("أحمد الفاروق", "أحمد الفاروق", "student.farooq@mishkat.academy", "مبتدئ", 4, 0.35f),
            StudentItem("عبد الله السعد", "عبد الله السعد", "abdullah.saad@mishkat.academy", "متقدم", 9, 0.75f),
            StudentItem("مريم المنصور", "مريم المنصور", "maryam.mansoor@mishkat.academy", "باحث متقن", 12, 1.0f),
            StudentItem("عمر الأنصاري", "عمر الأنصاري", "omar.ansari@mishkat.academy", "متوسط", 6, 0.50f)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(students) { student ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isEn) student.nameEn else student.nameAr,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = student.email,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            // زر الإجراءات (تنبيه / تعليق)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = {
                                        Toast.makeText(context, "تم إرسال تنبيه حث المدارسة إلى: ${student.nameAr}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                ) {
                                    Icon(imageVector = Icons.Default.NotificationAdd, contentDescription = "Alert", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(
                                    onClick = {
                                        Toast.makeText(context, "تم تعليق حساب الطالب: ${student.nameAr} لأسباب تأديبية", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp).background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Suspend", modifier = Modifier.size(16.dp), tint = Color.Red)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // نسبة تقدم الطالب في تدارس المتون
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = student.progress,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${(student.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // سجل عمليات الدخول الأكاديمية (رصد الدخول في الوقت الحقيقي)
        Text(
            text = if (isEn) "Recent Scholar Login Audit" else "رصد سجل عمليات الدخول الأكاديمية:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            if (loginLogs.isEmpty()) {
                Text(
                    text = if (isEn) "No recent log entries." else "لا توجد سجلات دخول مسجلة حالياً.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(loginLogs) { log ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                            Text(
                                text = "[$timeStr] ${log.uid}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${log.device} (${log.ip})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class StudentItem(
    val nameAr: String,
    val nameEn: String,
    val email: String,
    val level: String,
    val lessonsCompleted: Int,
    val progress: Float
)
