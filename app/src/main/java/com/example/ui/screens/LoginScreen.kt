package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.MishkatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MishkatViewModel,
    isEn: Boolean
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("student@mishkat.academy") }
    var password by remember { mutableStateOf("mishkat123") }
    var selectedRole by remember { mutableStateOf("student") } // student, teacher

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // بطاقة تسجيل الدخول المقيدة بحد أقصى للـ Tablet & Desktop (max width 450dp)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 450.dp)
                .padding(16.dp)
                .testTag("login_card")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // شعار المنصة الإسلامية
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Mishkat Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // اسم التطبيق بالخط العربي الأنيق
                Text(
                    text = if (isEn) "Mishkat Al-Durr Al-Maknun" else "مشكاة الدر المكنون",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = if (isEn) "Interactive Academy Portal" else "البوابة الأكاديمية الشرعية التفاعلية",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // منتقي الأدوار (طالب علم / معلّم مأذون)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val roles = listOf(
                        "student" to (if (isEn) "Student" else "طالب علم"),
                        "teacher" to (if (isEn) "Teacher / Admin" else "معلّم مأذون")
                    )

                    roles.forEach { (roleKey, label) ->
                        val isSelected = (selectedRole == roleKey)
                        Button(
                            onClick = { selectedRole = roleKey },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            elevation = null,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("role_button_$roleKey")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // حقل البريد الإلكتروني
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(if (isEn) "Academy Email" else "البريد الإلكتروني للأكاديمية") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_text_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // حقل كلمة المرور
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isEn) "Password" else "كلمة المرور") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_text_field")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // زر تسجيل الدخول الرئيسي للأكاديمية
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, if (isEn) "Please enter credentials" else "يرجى كتابة البيانات للدخول", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.login(email, selectedRole)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_action_button")
                ) {
                    Text(
                        text = if (isEn) "Login Academic Gateway" else "تسجيل الدخول الأكاديمي",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // دمج الـ Facebook Login التفاعلي المرتبط بالمصادقة
                Button(
                    onClick = {
                        viewModel.loginWithFacebook()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)), // فيسبوك الأزرق الأصلي
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("facebook_login_button")
                ) {
                    Icon(imageVector = Icons.Default.Facebook, contentDescription = "Facebook", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEn) "Link & Sign In with Facebook" else "ربط وتسجيل الدخول بفيسبوك",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
