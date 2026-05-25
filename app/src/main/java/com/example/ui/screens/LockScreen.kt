package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SurfaceCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LockScreen(
    onUnlockSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var codeEntered by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("Biometric fingerprint scan or enter PIN code") }
    var isAuthenticating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Core biometric simulation
    val triggerBiometrics: () -> Unit = {
        scope.launch {
            isAuthenticating = true
            statusText = "Scanning Fingerprint..."
            delay(1500) // Realistic scanning delay
            isAuthenticating = false
            statusText = "Authentication Successful!"
            delay(400)
            onUnlockSuccess()
        }
    }

    LaunchedEffect(Unit) {
        // Auto trigger biometrics on compile
        delay(500)
        triggerBiometrics()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepBackground, Color(0xFF030D16))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Cyber Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CyberCyan.copy(alpha = 0.1f))
                    .border(2.dp, CyberCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Secured Vault",
                    tint = CyberCyan,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ALGO TRADE AI SECURE",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "Device biometric verification is required",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Glowing Fingerprint button
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (isAuthenticating) EmeraldGreen.copy(alpha = 0.3f) else CyberCyan.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            listOf(CyberCyan, if (isAuthenticating) EmeraldGreen else Color.DarkGray)
                        ),
                        shape = CircleShape
                    )
                    .clickable(enabled = !isAuthenticating) { triggerBiometrics() }
                    .testTag("biometric_scanner"),
                contentAlignment = Alignment.Center
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(90.dp),
                        color = EmeraldGreen,
                        strokeWidth = 3.dp
                    )
                }
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Tap Fingerprint Scanner",
                    tint = if (isAuthenticating) EmeraldGreen else CyberCyan,
                    modifier = Modifier
                        .size(56.dp)
                        .scale(if (isAuthenticating) 1.1f else 1.0f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = statusText,
                color = if (statusText.contains("Successful")) EmeraldGreen else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN Dots view
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                for (i in 1..4) {
                    val active = i <= codeEntered.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) CyberCyan else Color.Transparent
                            )
                            .border(2.dp, if (active) CyberCyan else Color.Gray, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Pad UI
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(260.dp)
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("CLR", "0", "DEL")
                )

                for (row in rows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (key in row) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.5f)
                                    .clip(CircleShape)
                                    .background(SurfaceCard)
                                    .clickable {
                                        when (key) {
                                            "CLR" -> codeEntered = ""
                                            "DEL" -> {
                                                if (codeEntered.isNotEmpty()) {
                                                    codeEntered = codeEntered.dropLast(1)
                                                }
                                            }
                                            else -> {
                                                if (codeEntered.length < 4) {
                                                    codeEntered += key
                                                    if (codeEntered.length == 4) {
                                                        scope.launch {
                                                            isAuthenticating = true
                                                            statusText = "Verifying security PIN..."
                                                            delay(500)
                                                            isAuthenticating = false
                                                            // For security demo, pin can be any 4 digits, but let's accept "1234" or any PIN for simple usability
                                                            statusText = "Secure Access Verified!"
                                                            delay(300)
                                                            onUnlockSuccess()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .border(1.dp, Color.DarkGray.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (key == "DEL") {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Backspace",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        color = if (key == "CLR") Color.Red else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
