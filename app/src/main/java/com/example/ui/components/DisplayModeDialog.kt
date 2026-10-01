package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.MishkatViewModel

@Composable
fun DisplayModeDialog(
    viewModel: MishkatViewModel,
    isEn: Boolean,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val currentWidthDp = configuration.screenWidthDp
    val currentMode = viewModel.displayMode.value

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("display_mode_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // عنوان النافذة
                Text(
                    text = if (isEn) "Adaptive Display Settings" else "إعدادات العرض التكيفي",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // معلومات عرض الشاشة الحالي بالـ dp كما هو مطلوب بالشرط الفني
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isEn) "Current Width: ${currentWidthDp}dp" else "عرض الشاشة الحالي: ${currentWidthDp}dp",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // خيارات أنماط العرض
                val modes = listOf(
                    Triple("AUTO", if (isEn) "Auto (System Responsive)" else "تلقائي (حسب الشاشة)", Icons.Default.Devices),
                    Triple("MOBILE", if (isEn) "Mobile View (< 600dp)" else "هاتف محمول (Mobile)", Icons.Default.Smartphone),
                    Triple("TABLET", if (isEn) "Tablet View (600-1024dp)" else "جهاز لوحي (Tablet)", Icons.Default.TabletAndroid),
                    Triple("DESKTOP", if (isEn) "Desktop View (> 1024dp)" else "مكتب (Desktop)", Icons.Default.Computer)
                )

                modes.forEach { (modeKey, label, icon) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setDisplayMode(modeKey) }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        RadioButton(
                            selected = (currentMode == modeKey),
                            onClick = { viewModel.setDisplayMode(modeKey) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.testTag("radio_${modeKey.lowercase()}")
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (currentMode == modeKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (currentMode == modeKey) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentMode == modeKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // زر الإغلاق والإنقاذ
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_display_button")
                ) {
                    Text(
                        text = if (isEn) "Save & Apply" else "حفظ وتطبيق المظهر",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
