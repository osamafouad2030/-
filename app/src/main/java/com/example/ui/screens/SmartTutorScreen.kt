package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
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
fun SmartTutorScreen(
    viewModel: MishkatViewModel,
    isEn: Boolean,
    modifier: Modifier = Modifier
) {
    var question by remember { mutableStateOf("") }
    val tutorResponse by viewModel.tutorResponse.collectAsState()
    val isLoading by viewModel.isTutorLoading.collectAsState()
    val progressState by viewModel.studentProgress.collectAsState(initial = null)
    val currentLevel = progressState?.level ?: "مبتدئ"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("smart_tutor_screen")
    ) {
        // شريط المعلم الذكي العلوي
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isEn) "Intelligent AI Tutor" else "المعلم الأكاديمي الذكي المساعد",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isEn) "Configured for: $currentLevel Level" else "مهيأ حسب مقررك: مستوى $currentLevel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // نافذة محادثة أو عرض استجابة المعلم الذكي الشرعي
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    // مؤشر تحميل تفاعلي مع نبرة لطيفة
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isEn) "Consulting classical scholarly sources..." else "جاري مدارسة المسألة وتخريج الأدلة علمياً...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else if (tutorResponse.isEmpty()) {
                    // الحالة الفارغة الجذابة مع تلميحات
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isEn) "How can I assist you today?" else "كيف يمكنني مساعدتك في شرح كتبك اليوم؟",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isEn) {
                                "Ask about any branch of Fiqh, Hadith, Aqeedah, or Arabic linguistics to receive custom-tailored explanations."
                            } else {
                                "اطرح سؤالاً عن تفسير آية، حكم فقهي، أو إعراب متن لتلقى رداً مفصلاً بالأدلة والمقارنات المذهبية الرصينة."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    // نص الرد الشرعي بالكامل مع دعم التمرير
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = tutorResponse,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // حقل طرح الأسئلة
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                placeholder = { Text(if (isEn) "Ask about the study plan..." else "اطرح سؤالاً بليغاً في المقررات...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("tutor_input_field")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (question.isNotBlank()) {
                        viewModel.askTutorQuestion(question)
                        question = ""
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .size(48.dp)
                    .testTag("tutor_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
