package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.MishkatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecitationLabScreen(
    viewModel: MishkatViewModel,
    isEn: Boolean,
    modifier: Modifier = Modifier
) {
    val evaluationResult by viewModel.recitationResponse.collectAsState()
    val isLoading by viewModel.isRecitationLoading.collectAsState()

    // قائمة الآيات المقررة للاختبار الصوتي
    val quranVerses = listOf(
        "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
        "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ",
        "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ"
    )

    var selectedVerseIndex by remember { mutableStateOf(0) }
    val expectedVerse = quranVerses[selectedVerseIndex]

    // حالة محاكاة التسجيل ميكروفون
    var isRecordingSimulated by remember { mutableStateOf(false) }
    var simulatedVoiceText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("recitation_lab_screen")
    ) {
        // العنوان الرئيسي
        Text(
            text = if (isEn) "AI Qur'anic Recitation Lab" else "مختبر التلاوة الصوتية التفاعلي",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (isEn) "Evaluate Tajweed, pronunciation & articulation using AI." else "تدقيق وتصحيح التلاوة وأحكام التجويد ومخارج الحروف عبر الذكاء الاصطناعي.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // اختيار الآية الكريمة للاختبار
        Text(
            text = if (isEn) "Select verse to recite:" else "اختر الآية الكريمة المقررة للتلاوة:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        quranVerses.forEachIndexed { index, verse ->
            val isSelected = (index == selectedVerseIndex)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedVerseIndex = index }
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedVerseIndex = index },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.secondary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "﴿ $verse ﴾",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // رقعة التلاوة الصوتية / المحاكاة
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEn) "Recitation Recording" else "تسجيل تلاوتك العطرة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // زر الميكروفون التفاعلي
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = if (isRecordingSimulated) Color.Red.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                        .clickable {
                            isRecordingSimulated = !isRecordingSimulated
                            if (isRecordingSimulated) {
                                // محاكاة تلاوة ممتازة أو عادية للتحقق من التقييم
                                simulatedVoiceText = expectedVerse
                            }
                        }
                        .testTag("recitation_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecordingSimulated) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Microphone",
                        tint = if (isRecordingSimulated) Color.Red else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isRecordingSimulated) {
                        if (isEn) "Recording your recitation... Speak now." else "جاري الاستماع لصوتك المبارك وتدقيق المقاطع... تحدث الآن."
                    } else {
                        if (isEn) "Tap mic to start reciting." else "اضغط على الميكروفون لبدء تلاوة الآية الكريمة."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isRecordingSimulated) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                if (isRecordingSimulated) {
                    Spacer(modifier = Modifier.height(16.dp))
                    // حقل يوضح ما التقطه الميكروفون (يدعم التعديل للتأكد من المرونة)
                    OutlinedTextField(
                        value = simulatedVoiceText,
                        onValueChange = { simulatedVoiceText = it },
                        label = { Text(if (isEn) "Voice Transcription" else "التفريغ الصوتي التلقائي (تلاوتك)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_text_input")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // زر التقييم بالذكاء الاصطناعي
                Button(
                    onClick = {
                        val textToSubmit = if (simulatedVoiceText.isNotBlank()) simulatedVoiceText else expectedVerse
                        viewModel.evaluateRecitationVoice(expectedVerse, textToSubmit)
                        isRecordingSimulated = false
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("evaluate_recitation_button")
                ) {
                    Text(
                        text = if (isEn) "Evaluate Recitation" else "تقييم التلاوة وإصدار التقرير",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // عرض نتائج التقييم التجويدي
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isEn) "Tutor is analyzing your articulation & Tajweed..." else "المصحح الآلي يزن الحروف ومخارجها الآن...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else if (evaluationResult.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recitation_result_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFC9A227)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEn) "AI Feedback:" else "تقرير المعلم التجويدي المساعد:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = evaluationResult,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.1
                    )
                }
            }
        }
    }
}
