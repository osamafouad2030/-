package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.MishkatViewModel
import com.example.ui.components.AdaptiveScaffold
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.MishkatTheme

class MainActivity : ComponentActivity() {

    // تهيئة الـ ViewModel الأساسية لإدارة جميع الحالات الأكاديمية والمزامنة والترجمة
    private val viewModel: MishkatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // تفعيل العرض الشامل Edge-to-Edge لدعم الشاشات الحديثة والتجويف العلوي notch
        enableEdgeToEdge()
        
        setContent {
            // استيراد السمة الإسلامية الذهبية الزمردية المخصصة
            MishkatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val isEn by viewModel.isEnglish.collectAsState()

                    when (currentScreen) {
                        "auth" -> {
                            // شاشة تسجيل الدخول الأكاديمي والمصادقة بفيسبوك
                            LoginScreen(viewModel = viewModel, isEn = isEn)
                        }
                        else -> {
                            // الهيكل التكيفي العام يدير الشاشات حسب رتب وفئات الأجهزة (هاتف / لوحي / ديسكتوب)
                            AdaptiveScaffold(viewModel = viewModel) { _ ->
                                // يدار المحتوى بالكامل داخل الـ AdaptiveScaffold لضمان الـ Responsive
                            }
                        }
                    }
                }
            }
        }
    }
}
