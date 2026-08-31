package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessType
import com.example.ui.theme.*
import com.example.ui.viewmodel.SahayakViewModel
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    viewModel: SahayakViewModel,
    onLoginSuccess: () -> Unit
) {
    var isOtpStep by remember { mutableStateOf(false) }
    var mobileNumber by remember { mutableStateOf("9876543210") }
    var userName by remember { mutableStateOf("Ramesh Kumar Sharma") }
    var selectedRole by remember { mutableStateOf(BusinessType.KIRANA) }
    var otpDigits by remember { mutableStateOf(listOf("7", "4", "9", "2", "0", "1")) }
    var resendCountdown by remember { mutableIntStateOf(30) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }

    // Resend timer countdown effect
    LaunchedEffect(isOtpStep) {
        if (isOtpStep) {
            resendCountdown = 30
            while (resendCountdown > 0) {
                delay(1000)
                resendCountdown--
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Brand Emblem
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Emerald800),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🇮🇳",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "सहायक एआई • SahayakAI",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Ministry of Social Justice & Empowerment • SIH 2026",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Emerald800
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "ग्रामीण व सूक्ष्म उद्यमियों का डिजिटल खाता और वित्तीय साथी",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Slate700
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = isOtpStep,
                label = "auth_step_anim"
            ) { otpStepActive ->
                if (!otpStepActive) {
                    // STEP 1: MOBILE NUMBER & BUSINESS SELECTION
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_phone_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "मोबाइल नंबर से लॉगिन करें",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "Enter your 10-digit mobile number to receive OTP",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate700),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Name input
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                label = { Text("उद्यमी / व्यापारी का नाम (Full Name)", color = Slate800) },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Emerald800)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900,
                                    focusedBorderColor = Emerald700,
                                    unfocusedBorderColor = Slate300
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Mobile input
                            OutlinedTextField(
                                value = mobileNumber,
                                onValueChange = { if (it.length <= 10) mobileNumber = it.filter { char -> char.isDigit() } },
                                label = { Text("मोबाइल नंबर (Mobile Number)", color = Slate800) },
                                singleLine = true,
                                prefix = {
                                    Text(
                                        text = "+91  ",
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Emerald800)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_phone_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Slate900,
                                    unfocusedTextColor = Slate900,
                                    focusedBorderColor = Emerald700,
                                    unfocusedBorderColor = Slate300
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "व्यवसाय का प्रकार चुनें (Select Business Type):",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val roles = listOf(
                                    BusinessType.KIRANA,
                                    BusinessType.AGRICULTURE,
                                    BusinessType.STREET_VENDOR,
                                    BusinessType.DAIRY_FARMING,
                                    BusinessType.HANDICRAFTS,
                                    BusinessType.TAILORING
                                )
                                roles.chunked(2).forEach { rowRoles ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowRoles.forEach { role ->
                                            val isSelected = selectedRole == role
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { selectedRole = role },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) Emerald100 else Slate50,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) Emerald700 else Slate300
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = role.icon, fontSize = 20.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Column {
                                                        Text(
                                                            text = role.titleHi,
                                                            style = MaterialTheme.typography.labelMedium.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSelected) Emerald900 else Slate900
                                                            ),
                                                            maxLines = 1
                                                        )
                                                        Text(
                                                            text = role.name.replace("_", " "),
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = if (isSelected) Emerald800 else Slate700
                                                            ),
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = UdhaarRed,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (mobileNumber.length == 10) {
                                        errorMessage = null
                                        isOtpStep = true
                                    } else {
                                        errorMessage = "कृपया 10 अंकों का वैध मोबाइल नंबर दर्ज करें।"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("send_otp_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Emerald800,
                                    contentColor = PureWhite
                                )
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ओटीपी भेजें • Send OTP",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // STEP 2: OTP VERIFICATION
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_otp_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ओटीपी सत्यापन (OTP Verification)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "Sent to +91 $mobileNumber",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Slate800,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "बदलें (Edit)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Emerald800,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { isOtpStep = false }
                                        .padding(4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Demo OTP Hint Banner
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Amber100,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Amber600),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "💡", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "डेमो ओटीपी (Demo OTP Code): 749201",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Amber900
                                            )
                                        )
                                        Text(
                                            text = "Direct 1-tap verification for SIH Hackathon Jury",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Amber800
                                            )
                                        )
                                    }
                                    TextButton(
                                        onClick = {
                                            otpDigits = listOf("7", "4", "9", "2", "0", "1")
                                        }
                                    ) {
                                        Text(
                                            text = "Auto-Fill",
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald900
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // 6-Digit OTP Boxes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (i in 0..5) {
                                    val digit = otpDigits.getOrElse(i) { "" }
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (digit.isNotEmpty()) Emerald50 else Slate100)
                                            .border(
                                                width = if (digit.isNotEmpty()) 2.dp else 1.dp,
                                                color = if (digit.isNotEmpty()) Emerald700 else Slate300,
                                                shape = RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = digit,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Resend Timer & Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (resendCountdown > 0) {
                                    Text(
                                        text = "पुनः भेजें: ${resendCountdown}s में",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                                    )
                                } else {
                                    TextButton(
                                        onClick = {
                                            resendCountdown = 30
                                            otpDigits = listOf("7", "4", "9", "2", "0", "1")
                                        }
                                    ) {
                                        Text("Resend SMS OTP", color = Emerald800, fontWeight = FontWeight.Bold)
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        otpDigits = listOf("7", "4", "9", "2", "0", "1")
                                    }
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Emerald800, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset OTP", color = Emerald800)
                                }
                            }

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = UdhaarRed,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val enteredOtp = otpDigits.joinToString("")
                                    if (enteredOtp.length == 6) {
                                        isVerifying = true
                                        val success = viewModel.loginWithOtp(
                                            phone = mobileNumber,
                                            otp = enteredOtp,
                                            role = selectedRole,
                                            name = userName
                                        )
                                        isVerifying = false
                                        if (success) {
                                            onLoginSuccess()
                                        } else {
                                            errorMessage = "अमान्य ओटीपी कोड। कृपया सही ओटीपी दर्ज करें।"
                                        }
                                    } else {
                                        errorMessage = "कृपया पूरा 6 अंकों का ओटीपी दर्ज करें।"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("verify_otp_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Emerald800,
                                    contentColor = PureWhite
                                ),
                                enabled = !isVerifying
                            ) {
                                if (isVerifying) {
                                    CircularProgressIndicator(
                                        color = PureWhite,
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("सत्यापित किया जा रहा है...", color = PureWhite)
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "सत्यापित करें व शुरू करें • Verify & Start",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Demo Persona Selection (For rapid evaluation)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate300)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚡ त्वरित डेमो प्रोफाइल (1-Tap Fast Switch):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    userName = "Ramesh Kumar"
                                    mobileNumber = "9876543210"
                                    selectedRole = BusinessType.KIRANA
                                    otpDigits = listOf("7", "4", "9", "2", "0", "1")
                                    viewModel.loginWithOtp("9876543210", "749201", BusinessType.KIRANA, "Ramesh Kumar")
                                    onLoginSuccess()
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = PureWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate300)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🛒", fontSize = 20.sp)
                                Text("रमेश (किराना)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate900))
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    userName = "Sunita Devi"
                                    mobileNumber = "9415088231"
                                    selectedRole = BusinessType.TAILORING
                                    otpDigits = listOf("7", "4", "9", "2", "0", "1")
                                    viewModel.loginWithOtp("9415088231", "749201", BusinessType.TAILORING, "Sunita Devi (SHG)")
                                    onLoginSuccess()
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = PureWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate300)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🧵", fontSize = 20.sp)
                                Text("सुनीता (SHG)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate900))
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    userName = "Rajesh Patel"
                                    mobileNumber = "9839012489"
                                    selectedRole = BusinessType.AGRICULTURE
                                    otpDigits = listOf("7", "4", "9", "2", "0", "1")
                                    viewModel.loginWithOtp("9839012489", "749201", BusinessType.AGRICULTURE, "Rajesh Patel (Farmer)")
                                    onLoginSuccess()
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = PureWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate300)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌾", fontSize = 20.sp)
                                Text("राजेश (किसान)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate900))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
