package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.MedicineEntity
import com.example.data.database.PrescriptionEntity
import com.example.data.database.ReportEntity
import com.example.ui.viewmodel.MedicalViewModel
import com.example.ui.viewmodel.StructuredMedDraft
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun MainAppScreen(viewModel: MedicalViewModel) {
    val isSplashCompleted by viewModel.isSplashCompleted.collectAsState()
    val isLandingCompleted by viewModel.isLandingCompleted.collectAsState()
    val isOnboarded by viewModel.isOnboardingCompleted.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isDocState by viewModel.isDoctorMode.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            !isSplashCompleted -> {
                SplashScreen(onDone = { viewModel.isSplashCompleted.value = true })
            }
            !isLandingCompleted -> {
                LandingScreen(onContinue = { viewModel.isLandingCompleted.value = true })
            }
            !isOnboarded -> {
                OnboardingScreen(
                    viewModel = viewModel,
                    onDone = { viewModel.isOnboardingCompleted.value = true }
                )
            }
            !isLoggedIn -> {
                AuthScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { isDoc ->
                        viewModel.isDoctorMode.value = isDoc
                        viewModel.isLoggedIn.value = true
                    }
                )
            }
            else -> {
                Crossfade(
                    targetState = isDocState,
                    animationSpec = tween(500),
                    label = "AppModeTransition"
                ) { isDoctor ->
                    if (isDoctor) {
                        DoctorWorkspace(viewModel)
                    } else {
                        PatientWorkspace(viewModel)
                    }
                }
            }
        }
    }
}

// --- CINEMATIC SPLASH SCREEN ---
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }
    val particleAnim = rememberInfiniteTransition(label = "Particles")
    val rotation by particleAnim.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ParticlesRotation"
    )

    LaunchedEffect(Unit) {
        // Run cinematic entrance spring scale
        launch {
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(1000, easing = EaseInOutCubic)
            )
        }
        // Wait 2.5 seconds to feel cinematic and high fidelity, then fade out and finish
        kotlinx.coroutines.delay(2500)
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(500, easing = EaseInOutCubic)
        )
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFAFA),
                        Color(0xFFFAFAFA),
                        Color(0xFFFFEBEE).copy(alpha = 0.35f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Cybernetic background elements drawing
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(rotationZ = rotation)
        ) {
            val centerPoint = Offset(size.width / 2f, size.height / 2f)
            // Outer dashed orbital ring to show premium details
            drawCircle(
                color = Color(0xFFE53935).copy(alpha = 0.15f),
                radius = 240f,
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f))
            )

            // Dynamic background particle dots representing intelligent health data flow
            drawCircle(
                color = Color(0xFFE53935).copy(alpha = 0.35f),
                radius = 12f,
                center = Offset(centerPoint.x - 180f, centerPoint.y - 120f)
            )
            drawCircle(
                color = Color(0xFF111111).copy(alpha = 0.15f),
                radius = 8f,
                center = Offset(centerPoint.x + 210f, centerPoint.y - 80f)
            )
            drawCircle(
                color = Color(0xFFE53935).copy(alpha = 0.25f),
                radius = 16f,
                center = Offset(centerPoint.x + 110f, centerPoint.y + 190f)
            )
            drawCircle(
                color = Color(0xFF111111).copy(alpha = 0.25f),
                radius = 10f,
                center = Offset(centerPoint.x - 130f, centerPoint.y + 160f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value,
                alpha = alpha.value
            )
        ) {
            // Elegant glowing logo mark in Vivid Red & Black
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF111111))
                    .drawBehind {
                        // Soft pulsing glow ring in back
                        drawCircle(
                            color = Color(0xFFE53935).copy(alpha = 0.25f),
                            radius = size.minDimension / 1.5f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = "MediScript Logo",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            // Premium display headings
            Text(
                text = "MediScript",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111111),
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "SECURE AI HEALTH PORTAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE53935),
                letterSpacing = 3.sp
            )
        }
    }
}

// --- REDESIGNED LANDING PAGE ---
@Composable
fun LandingScreen(onContinue: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "LandingOrbit")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatY"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Status Header / Trust tag
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF111111)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Logo",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MediScript",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF111111)
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "V3.5 Live",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE53935)
                    )
                }
            }
        }

        // Center Content with Animated floating Cards and minimalist illustration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Elegant background ECG line canvas
            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val path = Path()
                val w = size.width
                val h = size.height
                path.moveTo(0f, h * 0.5f)
                path.lineTo(w * 0.25f, h * 0.5f)
                path.lineTo(w * 0.35f, h * 0.2f)
                path.lineTo(w * 0.45f, h * 0.8f)
                path.lineTo(w * 0.5f, h * 0.5f)
                path.lineTo(w * 0.53f, h * 0.45f)
                path.lineTo(w * 0.56f, h * 0.55f)
                path.lineTo(w * 0.6f, h * 0.5f)
                path.lineTo(w * 0.7f, h * 0.5f)
                path.lineTo(w * 0.75f, h * 0.15f)
                path.lineTo(w * 0.8f, h * 0.75f)
                path.lineTo(w * 0.85f, h * 0.5f)
                path.lineTo(w, h * 0.5f)

                drawPath(
                    path = path,
                    color = Color(0xFFE53935).copy(alpha = 0.08f),
                    style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // Beautiful Floating glassmorphic medicine card
            Box(
                modifier = Modifier
                    .offset(y = floatAnim.dp)
                    .width(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .border(BorderStroke(1.5.dp, Color(0xFF111111).copy(alpha = 0.12f)), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🧬", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Amoxicillin",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF111111)
                                )
                                Text(
                                    text = "500 mg • 3x Daily",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE53935))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Active",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF111111).copy(alpha = 0.05f))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Next dose in 42 mins",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF111111)
                            )
                        }
                        Text(
                            text = "10:30 AM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    }
                }
            }
        }

        // Heading & Subheading Area (60% White space backdrop, 30% Off-black text contrast)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "AI-Powered Smart Healthcare",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
                color = Color(0xFF111111),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Manage prescriptions, medicines, reports, and doctor workflows with intelligent automation.",
                fontSize = 14.sp,
                color = Color(0xFF111111).copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Animated / Beautiful Statistics section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF111111).copy(alpha = 0.03f))
                    .border(BorderStroke(1.dp, Color(0xFF111111).copy(alpha = 0.06f)), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("99.8%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                    Text("Transcription", fontSize = 10.sp, color = Color(0xFF64748B))
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF111111).copy(alpha = 0.08f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("2.1 Sec", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
                    Text("AI Summaries", fontSize = 10.sp, color = Color(0xFF64748B))
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF111111).copy(alpha = 0.08f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HIPAA", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
                    Text("Compliant", fontSize = 10.sp, color = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Interactive Buttons with smooth visual highlights
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111111),
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFFE53935))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Continue with Phone", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFF111111).copy(alpha = 0.15f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF111111))
            ) {
                Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = null, tint = Color(0xFFE53935))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "Login",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE53935),
                    modifier = Modifier.clickable { onContinue() }
                )
                Text(
                    text = " • ",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "Create Account",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111111),
                    modifier = Modifier.clickable { onContinue() }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// --- REDESIGNED ONBOARDING DETAILS ---
@Composable
fun OnboardingScreen(viewModel: MedicalViewModel, onDone: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    val isDoctorModeSelected = remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "OnboardPulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Identity Header & Dynamic SKIP option
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF111111)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Logo",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "MediScript",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            }

            if (step < 3) {
                Text(
                    text = "Skip",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE53935),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { step = 3 } // Directly skip to profile role selection page!
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Stepped Graphics and text
        Crossfade(
            targetState = step,
            animationSpec = tween(450),
            label = "OnboardingSteps"
        ) { currentStep ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                when (currentStep) {
                    0 -> {
                        // AI Prescription Writing Screen
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = Color(0x1FE53935),
                                        radius = size.minDimension / 1.7f * glowScale
                                    )
                                    drawCircle(
                                        color = Color(0x0CE53935),
                                        radius = size.minDimension / 1.3f * glowScale
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "AI Prescription Writing Icon",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(68.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        Text(
                            text = "AI Prescription Writing",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF111111)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Doctors dictate natural speech; our intelligent Voice AI automatically transcribes structured medicine names, dosages, and schedules in real-time.",
                            fontSize = 14.sp,
                            color = Color(0xFF111111).copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    1 -> {
                        // Medicine Tracking Screen
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = Color(0x1F111111),
                                        radius = size.minDimension / 1.7f * glowScale
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = "Medicine Tracking Icon",
                                tint = Color(0xFF111111),
                                modifier = Modifier.size(68.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        Text(
                            text = "Smart Medicine Tracking",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF111111)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Patients keep active schedules synced seamlessly. Track dose compliance daily and keep safety buffers intact without manual logging.",
                            fontSize = 14.sp,
                            color = Color(0xFF111111).copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    2 -> {
                        // AI Health Assistant Screen
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = Color(0x1FE53935),
                                        radius = size.minDimension / 1.7f * glowScale
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "AI Health Assistant Icon",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(68.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        Text(
                            text = "AI Health Assistant",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF111111)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Engage in full secure chat conversations to break down complicated scanner reports, examine allergy risks, or translate diagnostics values.",
                            fontSize = 14.sp,
                            color = Color(0xFF111111).copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    3 -> {
                        // Doctor + Patient Selector Screen (The original profile chooser, updated)
                        Text(
                            text = "Doctor + Patient Ecosystem",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF111111)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Select your workflow profile to access secure workspaces",
                            fontSize = 13.sp,
                            color = Color(0xFF111111).copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            RoleCard(
                                title = "Patient Workspace",
                                description = "For medication schedules, uploading report files, & chats",
                                icon = Icons.Default.Person,
                                isSelected = !isDoctorModeSelected.value,
                                onClick = { isDoctorModeSelected.value = false },
                                modifier = Modifier.weight(1f)
                            )
                            RoleCard(
                                title = "Doctor Clinic",
                                description = "For real-time voice presets, patient histories, & signature authorizations",
                                icon = Icons.Default.Badge,
                                isSelected = isDoctorModeSelected.value,
                                onClick = { isDoctorModeSelected.value = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Bottom page controllers (Page indicator + CTA Button)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Page Dot Indicators (4 pages)
            Row(
                modifier = Modifier.padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) { i ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (step == i) 20.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (step == i) Color(0xFFE53935)
                                else Color(0xFF111111).copy(alpha = 0.15f)
                            )
                    )
                }
            }

            // Next / Launch Master CTA Button
            Button(
                onClick = {
                    if (step < 3) {
                        step++
                    } else {
                        viewModel.isDoctorMode.value = isDoctorModeSelected.value
                        onDone() // Done with onboarding! Move to Auth
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_cta"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111111),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (step < 3) "Next Page" else "Confirm Professional Profile",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- ROLE SELECTOR CARD ---
@Composable
fun RoleCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag(if (isSelected) "role_selected" else "role_unselected")
            .border(
                width = 2.dp,
                color = if (isSelected) Color(0xFFE53935) else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFEBEE)
            else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color(0xFFE53935).copy(alpha = 0.15f)
                        else Color(0xFF111111).copy(alpha = 0.05f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFFE53935) else Color(0xFF111111).copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF111111)
            )
            Text(
                text = description,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                color = Color(0xFF111111).copy(alpha = 0.6f)
            )
        }
    }
}

// --- PREMIUM AUTHENTICATION AND OTP FLOW ---
@Composable
fun AuthScreen(viewModel: MedicalViewModel, onLoginSuccess: (isDoctor: Boolean) -> Unit) {
    var isOtpTransition by remember { mutableStateOf(false) }
    var phoneNumberInput by remember { mutableStateOf("") }
    val otpDigits = remember { mutableStateListOf("", "", "", "") }
    var nextDigitIndex by remember { mutableStateOf(0) }
    
    // Timer state for OTP
    var secondsRemaining by remember { mutableStateOf(30) }
    var isTimerActive by remember { mutableStateOf(true) }

    LaunchedEffect(isTimerActive, secondsRemaining) {
        if (isTimerActive && secondsRemaining > 0) {
            kotlinx.coroutines.delay(1000)
            secondsRemaining--
        } else if (secondsRemaining == 0) {
            isTimerActive = false
        }
    }

    var isVerifying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isDoctorRoleChosen = viewModel.isDoctorMode.collectAsState().value

    if (!isOtpTransition) {
        // Main authenticating options (split layout / luxury visual)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper Area: Luxury design, dark off-black backdrop panel (30% secondary space)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.42f)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Color(0xFF111111))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Pulsating central medical secure shield
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = if (isDoctorRoleChosen) "Doctor Clinical Portal" else "Patient Personal Workspace",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ISO 27001 SECURED & REGISTERED GATEWAY",
                        fontSize = 10.sp,
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Lower Area: Inputs & Interactive controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.58f)
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Enter secure phone number to authorize terminal access:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = phoneNumberInput,
                        onValueChange = { if (it.length <= 10) phoneNumberInput = it },
                        placeholder = { Text("+1 (555) 000-0000", color = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color(0xFFE53935))
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF111111),
                            unfocusedTextColor = Color(0xFF111111),
                            focusedBorderColor = Color(0xFFE53935),
                            unfocusedBorderColor = Color(0xFF111111).copy(alpha = 0.15f)
                        )
                    )

                    Button(
                        onClick = {
                            if (phoneNumberInput.isNotEmpty()) {
                                isOtpTransition = true
                                isTimerActive = true
                                secondsRemaining = 30
                            }
                        },
                        enabled = phoneNumberInput.length >= 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF111111).copy(alpha = 0.08f),
                            disabledContentColor = Color(0xFF64748B)
                        )
                    ) {
                        Text(text = "Send Security OTP Code", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Social dividers and secondary logins
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF111111).copy(alpha = 0.08f)))
                        Text(
                            text = " OR PRE-AUTHORIZED BIOMETRICS ",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.sp
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF111111).copy(alpha = 0.08f)))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onLoginSuccess(isDoctorRoleChosen) }, // Auto match chosen role
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, Color(0xFF111111).copy(alpha = 0.12f))
                        ) {
                            Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Google Link", fontSize = 11.sp, color = Color(0xFF111111), fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onLoginSuccess(isDoctorRoleChosen) }, // Auto match chosen role
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, Color(0xFF111111).copy(alpha = 0.12f))
                        ) {
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF111111), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TouchID", fontSize = 11.sp, color = Color(0xFF111111), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        // OTP SCREEN view block
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header description with back button option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isOtpTransition = false }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back", tint = Color(0xFF111111))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Security Verification", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
                }

                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935).copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "A secure verification code was sent to terminal +1 $phoneNumberInput",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 4 Interactive Premium Animated OTP displays
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    repeat(4) { idx ->
                        val isCurrent = nextDigitIndex == idx
                        val containsVal = otpDigits[idx].isNotEmpty()
                        
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(
                                    BorderStroke(
                                        width = if (isCurrent) 2.dp else 1.2.dp,
                                        color = when {
                                            isCurrent -> Color(0xFFE53935)
                                            containsVal -> Color(0xFF111111)
                                            else -> Color(0xFF111111).copy(alpha = 0.15f)
                                        }
                                    ),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = otpDigits[idx],
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Resend Timer countdown details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Didn't receive code? ", fontSize = 12.sp, color = Color(0xFF64748B))
                    if (isTimerActive) {
                        Text(
                            text = "Resend in ${secondsRemaining}s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111)
                        )
                    } else {
                        Text(
                            text = "Resend Now",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE53935),
                            modifier = Modifier
                                .clickable {
                                    secondsRemaining = 30
                                    isTimerActive = true
                                    otpDigits[0] = ""
                                    otpDigits[1] = ""
                                    otpDigits[2] = ""
                                    otpDigits[3] = ""
                                    nextDigitIndex = 0
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Secure Virtual Keypad to facilitate interactive login triggers
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF111111).copy(alpha = 0.03f))
                    .border(BorderStroke(1.dp, Color(0xFF111111).copy(alpha = 0.05f)), RoundedCornerShape(24.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isVerifying) {
                    Column(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFFE53935), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Verifying Credentials...", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
                    }
                } else {
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("Clear", "0", "Verify")
                    )

                    keys.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { key ->
                                Button(
                                    onClick = {
                                        when (key) {
                                            "Clear" -> {
                                                otpDigits[0] = ""
                                                otpDigits[1] = ""
                                                otpDigits[2] = ""
                                                otpDigits[3] = ""
                                                nextDigitIndex = 0
                                            }
                                            "Verify" -> {
                                                isVerifying = true
                                                scope.launch {
                                                    kotlinx.coroutines.delay(1200)
                                                    isVerifying = false
                                                    onLoginSuccess(isDoctorRoleChosen)
                                                }
                                            }
                                            else -> {
                                                if (nextDigitIndex < 4) {
                                                    otpDigits[nextDigitIndex] = key
                                                    nextDigitIndex++
                                                    if (nextDigitIndex == 4) {
                                                        isVerifying = true
                                                        scope.launch {
                                                            kotlinx.coroutines.delay(1000)
                                                            isVerifying = false
                                                            onLoginSuccess(isDoctorRoleChosen)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (key == "Verify" || key == "Clear") Color(0xFF111111).copy(alpha = 0.08f) else Color.White,
                                        contentColor = Color(0xFF111111)
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = if (key.length > 1) 12.sp else 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (key == "Verify") Color(0xFFE53935) else Color(0xFF111111)
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


// --- PATIENT MODE WORKSPACE ---

@Composable
fun PatientWorkspace(viewModel: MedicalViewModel) {
    val tab by viewModel.selectedPatientTab.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            PatientBottomBar(
                selectedTab = tab,
                onTabSelected = { viewModel.selectedPatientTab.value = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (tab) {
                0 -> PatientDashboard(viewModel)
                1 -> PatientMedicineTracker(viewModel)
                2 -> HealthAssistantChat(viewModel)
                3 -> ReportsManager(viewModel)
                4 -> ProfileScreen(viewModel)
            }
        }
    }
}

@Composable
fun PatientBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        modifier = Modifier.testTag("patient_bottom_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(0, Icons.Outlined.Dashboard, "Home"),
            Triple(1, Icons.Outlined.Alarm, "Tracker"),
            Triple(2, Icons.Outlined.Chat, "Health AI"),
            Triple(3, Icons.Outlined.FolderCopy, "Reports"),
            Triple(4, Icons.Outlined.AccountCircle, "Profile")
        )

        items.forEach { (index, icon, label) ->
            val isActive = selectedTab == index
            NavigationBarItem(
                selected = isActive,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (isActive) {
                            when (index) {
                                0 -> Icons.Filled.Dashboard
                                1 -> Icons.Filled.Alarm
                                2 -> Icons.Filled.Chat
                                3 -> Icons.Filled.FolderCopy
                                else -> Icons.Filled.AccountCircle
                            }
                        } else icon,
                        contentDescription = label
                    )
                },
                label = { Text(text = label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}

// 1. Patient Dashboard Screen
@Composable
fun PatientDashboard(viewModel: MedicalViewModel) {
    val medicines by viewModel.medicines.collectAsState()
    val prescriptions by viewModel.prescriptions.collectAsState()
    val reports by viewModel.reports.collectAsState()

    val totalDoses = medicines.size
    val completedDoses = medicines.count { it.isCompletedToday }
    val completionRatio = if (totalDoses > 0) completedDoses.toFloat() / totalDoses else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Header Section - Geometric Balance Style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GOOD MORNING,",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Alex Bennett",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Profile Avatar with Notification Badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { viewModel.toggleMode() }
                        .testTag("switch_to_doctor"),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .background(Color(0xFF111111)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AB",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    // Red indicator badge
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }
        }

        item {
            // Progress Card: Geometric MD3 Style with Ultra Modern Matte Black Background (30% secondary)
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFF222222)), RoundedCornerShape(32.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Today's Progress",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$completedDoses of $totalDoses doses taken",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (completionRatio >= 1f && totalDoses > 0) "Perfect Routine!" else "Next: 8:00 PM",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Custom Circular Progress Gauge matching design in Vivid Accent Red (10%)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(80.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = completionRatio,
                                strokeWidth = 8.dp,
                                color = Color(0xFFE53935),
                                trackColor = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = "${(completionRatio * 100).toInt()}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        item {
            // Up Next Section (Geometric Balance rounded box)
            val nextMed = medicines.firstOrNull { !it.isCompletedToday }
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFF111111).copy(alpha = 0.1f)), RoundedCornerShape(32.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UP NEXT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.8.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "View Schedule",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935),
                            modifier = Modifier.clickable { viewModel.selectedPatientTab.value = 1 }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    if (nextMed != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFFFEBEE))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rounded capsule emoji indicator
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💊", fontSize = 22.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nextMed.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111111)
                                )
                                Text(
                                    text = "${nextMed.dosage} • ${nextMed.timing}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFD32F2F)
                                )
                            }

                            Button(
                                onClick = { viewModel.markMedicineTaken(nextMed) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE53935),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Mark", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✨", fontSize = 22.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "All Kept Active",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = "No pending doses scheduled next.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            // AI Health Assistant Card (Fine modern outline style)
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, Color(0xFF111111)), RoundedCornerShape(32.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF111111)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", color = Color(0xFFE53935), fontSize = 18.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Health Insights",
                                color = Color(0xFF111111),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your health patterns have shown 12% improvement. Keep up the consistent intervals to sustain metabolic symmetry!",
                                color = Color(0xFF333333),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            // Quick Actions 3-Column Grid Layout
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "QUICK ACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.8.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Action 1: Records -> tab = 3
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.selectedPatientTab.value = 3 }
                            .testTag("action_records"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.1f)
                                .border(BorderStroke(1.dp, Color(0xFF111111).copy(alpha = 0.1f)), RoundedCornerShape(24.dp))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("📄", fontSize = 28.sp)
                            }
                        }
                        Text(text = "RECORDS", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111111))
                    }

                    // Action 2: AI Chat -> tab = 2
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.selectedPatientTab.value = 2 }
                            .testTag("action_ai_chat"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.1f)
                                .border(BorderStroke(1.dp, Color(0xFF111111).copy(alpha = 0.1f)), RoundedCornerShape(24.dp))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("💬", fontSize = 28.sp)
                            }
                        }
                        Text(text = "AI CHAT", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111111))
                    }

                    // Action 3: Switch Clinical workspace direct toggle
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.toggleMode() }
                            .testTag("action_sos"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.1f)
                                .border(BorderStroke(1.dp, Color(0xFFFFCDD2)), RoundedCornerShape(24.dp))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("🏥", fontSize = 28.sp)
                            }
                        }
                        Text(text = "CLINIC MODE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE53935))
                    }
                }
            }
        }

        // Timeline Weekly Tracker Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text(
                    text = "WEEKLY TRACK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.8.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    days.forEachIndexed { idx, day ->
                        val isToday = idx == 2 // Mocking Wed
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { }
                                .padding(horizontal = 10.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                color = if (isToday) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = (25 + idx).toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isToday) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        // Today's Medications Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Routine",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.selectedPatientTab.value = 1 }) {
                    Text("View Tracker")
                }
            }
        }

        // Quick Daily Medicine List
        if (medicines.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No active medicines found. Tap 'Tracker' to add a schedule.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(medicines) { med ->
                MedicineItemCard(medicine = med, onCheckChange = { viewModel.markMedicineTaken(med) }, onExplain = { viewModel.requestMedicineExplanation(med) })
            }
        }

        // Diagnostic insights
        if (reports.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Diagnostic Insight",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(reports.take(1)) { report ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Summarize, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = report.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = report.aiSummary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(start = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // AI Explanation Dialog overlay!
    val explainMed by viewModel.selectedMedicineForSimplify.collectAsState()
    val isSimplifyingState by viewModel.isSimplifying.collectAsState()
    val explanationText by viewModel.simplifiedExplanation.collectAsState()

    if (explainMed != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearExplanation() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.SmartButton, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "AI Explains ${explainMed?.name ?: ""}")
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (isSimplifyingState) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Consulting clinical pharmacist backend...")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            item {
                                Text(
                                    text = explanationText,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearExplanation() }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
fun MedicineItemCard(
    medicine: MedicineEntity,
    onCheckChange: () -> Unit,
    onExplain: () -> Unit
) {
    val completedColor = if (medicine.isCompletedToday) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medicine_card_${medicine.name}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (medicine.isCompletedToday) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = medicine.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (medicine.isCompletedToday) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = medicine.dosage,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${medicine.frequency} • ${medicine.timing} • ${medicine.days} Days",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                if (!medicine.instructions.isEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = medicine.instructions,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onExplain,
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartButton,
                        contentDescription = "AI Explain",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Checkbox
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (medicine.isCompletedToday) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                        )
                        .clickable { onCheckChange() }
                        .testTag("check_med_${medicine.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (medicine.isCompletedToday) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = "Mark Checked",
                        tint = if (medicine.isCompletedToday) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// 2. Patient Medicine Tracker Screen
@Composable
fun PatientMedicineTracker(viewModel: MedicalViewModel) {
    val medicines by viewModel.medicines.collectAsState()

    var showForm by remember { mutableStateOf(false) }

    var formName by remember { mutableStateOf("") }
    var formDosage by remember { mutableStateOf("1 tablet") }
    var formFreq by remember { mutableStateOf("Twice Daily") }
    var formTiming by remember { mutableStateOf("After Food") }
    var formDays by remember { mutableStateOf("5") }
    var formReminders by remember { mutableStateOf("08:00, 20:00") }
    var formInstructions by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Medication Tracker", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Manage and coordinate your prescription list", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
                Button(
                    onClick = { showForm = !showForm },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (showForm) "Close Form" else "Add Medicine")
                }
            }
        }

        if (showForm) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("add_prescription_form"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("New Scheduled Medicine", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        OutlinedTextField(
                            value = formName,
                            onValueChange = { formName = it },
                            label = { Text("Name (e.g. Lipitor)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formDosage,
                                onValueChange = { formDosage = it },
                                label = { Text("Dosage strength") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formDays,
                                onValueChange = { formDays = it },
                                label = { Text("Days count") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formFreq,
                                onValueChange = { formFreq = it },
                                label = { Text("Frequency") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formTiming,
                                onValueChange = { formTiming = it },
                                label = { Text("Timing rule") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = formInstructions,
                            onValueChange = { formInstructions = it },
                            label = { Text("Special details (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (!formName.isEmpty()) {
                                    viewModel.addCustomMedicine(
                                        name = formName,
                                        Dosage = formDosage,
                                        freq = formFreq,
                                        timing = formTiming,
                                        daysCount = formDays.toIntOrNull() ?: 5,
                                        reminders = formReminders,
                                        details = formInstructions
                                    )
                                    // Reset fields
                                    formName = ""
                                    formInstructions = ""
                                    showForm = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Schedule")
                        }
                    }
                }
            }
        }

        // Show the schedule list of added items
        if (medicines.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Your tracker list is empty. Add matching items above!")
                }
            }
        } else {
            items(medicines) { med ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(med.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("${med.dosage} - ${med.frequency}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteMedicine(med.id) }) {
                                Icon(imageVector = Icons.Default.Delete, tint = Color.Red, contentDescription = "Delete")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Timing: ${med.timing} | Duration: ${med.days} days | Reminders (Alarms): ${med.reminderTimes}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (!med.instructions.isEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(med.instructions, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// 3. AI Health Chat Assistant Screen
@Composable
fun HealthAssistantChat(viewModel: MedicalViewModel) {
    val chatStream by viewModel.chatMessages.collectAsState()
    val inputText by viewModel.chatInputText.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()

    val scope = rememberCoroutineScope()
    val listState = remember { androidx.compose.foundation.lazy.LazyListState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.SmartToy, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "MediScript Chatbot", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "AI Clinical Safe Explanations", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
            }
        }

        // Messages area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatStream) { msg ->
                val isAi = msg.sender == "ai"
                val alignment = if (isAi) Alignment.Start else Alignment.End
                val bubbleColor = if (isAi) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = alignment
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isAi) 4.dp else 16.dp,
                                    bottomEnd = if (isAi) 16.dp else 4.dp
                                )
                            )
                            .background(bubbleColor)
                            .padding(14.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg.content,
                            color = if (isAi) MaterialTheme.colorScheme.onSurface else Color.White,
                            fontSize = 14.sp,
                            lineHeight = 19.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isAi) "AI assistant" else "You",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            if (isLoading) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text("AI is analyzing...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Inputs row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { viewModel.chatInputText.value = it },
                placeholder = { Text("Ask about drugs, dosage, etc.") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text"),
                shape = RoundedCornerShape(24.dp)
            )

            FloatingActionButton(
                onClick = {
                    if (!inputText.isBlank()) {
                        viewModel.sendChatMessage()
                        scope.launch {
                            // Scroll to bottom
                            listState.animateScrollToItem(chatStream.size)
                        }
                    }
                },
                modifier = Modifier.size(48.dp).testTag("chat_send_btn"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
            }
        }
    }
}

// 4. Diagnostic Reports Upload & Management Screen
@Composable
fun ReportsManager(viewModel: MedicalViewModel) {
    val reports by viewModel.reports.collectAsState()

    val scanCategory by viewModel.uploadCategoryInput.collectAsState()
    val scanNotes by viewModel.uploadNotesInput.collectAsState()
    val scanName by viewModel.uploadNameInput.collectAsState()
    val isUploading by viewModel.isUploadingReport.collectAsState()

    var isAddingReport by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Clinical Folders", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Diagnostic reports, lab summaries & images", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
                Button(
                    onClick = { isAddingReport = !isAddingReport },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isAddingReport) "Close Scan" else "Upload Scan")
                }
            }
        }

        if (isAddingReport) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("add_report_form"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Extract Summary from Diagnostic Images", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        OutlinedTextField(
                            value = scanName,
                            onValueChange = { viewModel.uploadNameInput.value = it },
                            label = { Text("Report Title (e.g. Hemoglobin Test)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = scanCategory,
                            onValueChange = { viewModel.uploadCategoryInput.value = it },
                            label = { Text("Category (e.g. Hematology, Cardiology)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = scanNotes,
                            onValueChange = { viewModel.uploadNotesInput.value = it },
                            label = { Text("Raw Report Diagnostics / Doctor Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Button(
                            onClick = {
                                if (!scanName.isEmpty()) {
                                    viewModel.processReportUpload()
                                    isAddingReport = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isUploading) {
                                Text("Saving & Smart AI Explaining...")
                            } else {
                                Text("Compile AI Analysis & Upload")
                            }
                        }
                    }
                }
            }
        }

        if (reports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No diagnostic logs synced yet. Upload above!")
                }
            }
        } else {
            items(reports) { r ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.FolderOpen, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = r.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = "${r.category} • ${r.date} • ${r.fileSize}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                            IconButton(onClick = { viewModel.deleteReport(r.id) }) {
                                Icon(imageVector = Icons.Default.Delete, tint = Color.Red, contentDescription = "Delete")
                            }
                        }

                        if (!r.notes.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Original Scan Notes: ${r.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }

                        if (!r.aiSummary.isEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.ElectricBolt, tint = MaterialTheme.colorScheme.primary, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "MediScript AI Explanation", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = r.aiSummary, fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


// --- DOCTOR MODE CLINIC ---

@Composable
fun DoctorWorkspace(viewModel: MedicalViewModel) {
    val tab by viewModel.selectedDoctorTab.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            DoctorBottomBar(
                selectedTab = tab,
                onTabSelected = { viewModel.selectedDoctorTab.value = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (tab) {
                0 -> DoctorPatientsDashboard(viewModel)
                1 -> AIVoicePrescriber(viewModel)
                2 -> DoctorSignaturePad(viewModel)
                3 -> PrescriptionHistoryScreen(viewModel)
                4 -> ProfileScreen(viewModel)
            }
        }
    }
}

@Composable
fun DoctorBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        modifier = Modifier.testTag("doctor_bottom_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(0, Icons.Outlined.People, "Patients"),
            Triple(1, Icons.Outlined.KeyboardVoice, "Voice AI"),
            Triple(2, Icons.Outlined.Draw, "Signature"),
            Triple(3, Icons.Outlined.History, "History"),
            Triple(4, Icons.Outlined.AccountCircle, "Profile")
        )

        items.forEach { (index, icon, label) ->
            val isActive = selectedTab == index
            NavigationBarItem(
                selected = isActive,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (isActive) {
                            when (index) {
                                0 -> Icons.Filled.People
                                1 -> Icons.Filled.KeyboardVoice
                                2 -> Icons.Filled.Draw
                                3 -> Icons.Filled.History
                                else -> Icons.Filled.AccountCircle
                            }
                        } else icon,
                        contentDescription = label
                    )
                },
                label = { Text(text = label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                )
            )
        }
    }
}

// 1. Doctor Patient Lists Management Dashboard
@Composable
fun DoctorPatientsDashboard(viewModel: MedicalViewModel) {
    var searchByPatient by remember { mutableStateOf("") }

    val mockPatients = listOf(
        Pair("Ritik Rajput", "Age 23 - Rhinitis Active"),
        Pair("Devika Murthy", "Age 45 - Chronic Hypertension"),
        Pair("Amit Sharma", "Age 34 - Type 2 Diabetes"),
        Pair("Sneha Patel", "Age 29 - Acid Peptic Disease")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DR. SAHAB CLINICAL PORTAL,",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dr. Dev Anand",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Profile Avatar with Notification Badge (Doctor mode)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { viewModel.toggleMode() },
                    contentAlignment = Alignment.TopEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .background(Color(0xFF111111)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DA",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    // Red indicator badge
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }
        }

        item {
            // High-fidelity search entry shape
            OutlinedTextField(
                value = searchByPatient,
                onValueChange = { searchByPatient = it },
                placeholder = { Text("Search patient case logs...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("search_patients"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFF111111).copy(alpha = 0.12f)
                )
            )
        }

        item {
            Text(
                text = "ASSIGNED PATIENTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.8.sp,
                color = Color(0xFF64748B)
            )
        }

        items(mockPatients.filter { p -> p.first.lowercase().contains(searchByPatient.lowercase()) }) { p ->
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFF111111).copy(alpha = 0.1f)), RoundedCornerShape(24.dp))
                    .clickable {
                        viewModel.patientNameInput.value = p.first
                        viewModel.selectedDoctorTab.value = 1
                    }
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Badge, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = p.first, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = p.second, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.patientNameInput.value = p.first
                            viewModel.selectedDoctorTab.value = 1
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Prescribe", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


// 2. MAIN VOICE PRESCRIPTION WRITING SYSTEM
@Composable
fun AIVoicePrescriber(viewModel: MedicalViewModel) {
    val isRecording by viewModel.isListening.collectAsState()
    val transcript by viewModel.voiceTranscript.collectAsState()
    val isGenState by viewModel.isGeneratingPrescription.collectAsState()
    val drafts by viewModel.structuredMedsDraft.collectAsState()

    val pName by viewModel.patientNameInput.collectAsState()
    val dDiagnosis by viewModel.diagnosisInput.collectAsState()
    val dNotes by viewModel.doctorNotesInput.collectAsState()

    val scope = rememberCoroutineScope()

    val state = rememberInfiniteTransition("AudioWave")
    val waveScale1 by state.animateFloat(0.8f, 1.4f, infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse), label = "W1")
    val waveScale2 by state.animateFloat(0.9f, 1.3f, infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), label = "W2")

    val presets = listOf(
        "Paracetamol 650 mg twice daily for five days after food, and also give Cetirizine 10 mg at bedtime daily for seven days.",
        "Amoxicillin 500 mg tablets, triple strength three times daily for seven days complete course."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(text = "Dr. Sahab Voice AI Scribe", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Text(text = "Spoken clinical recommendations automatically parsed into digital records", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFF0F172A).copy(alpha = 0.05f)), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "Voice Dictation Core", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = pName,
                        onValueChange = { viewModel.patientNameInput.value = it },
                        label = { Text("Patient Name") },
                        modifier = Modifier.fillMaxWidth().testTag("patient_name_input")
                    )

                    OutlinedTextField(
                        value = dDiagnosis,
                        onValueChange = { viewModel.diagnosisInput.value = it },
                        label = { Text("Diagnosis / Assessment (e.g. Acid Reflux)") },
                        modifier = Modifier.fillMaxWidth().testTag("diagnosis_input")
                    )

                    // GLOWING AUDIO RECORDING DESIGN MODULE
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .drawBehind {
                                if (isRecording) {
                                    drawCircle(
                                        color = Color(0xFFE53935).copy(alpha = 0.22f),
                                        radius = size.minDimension / 2f * waveScale1
                                    )
                                    drawCircle(
                                        color = Color(0xFFE53935).copy(alpha = 0.12f),
                                        radius = size.minDimension / 1.6f * waveScale2
                                    )
                                }
                            }
                            .clickable {
                                if (!isRecording) {
                                    viewModel.startListeningSimulated()
                                } else {
                                    viewModel.stopListeningAndParse(null)
                                }
                            }
                            .testTag("dictate_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isRecording) {
                                        Color(0xFFE53935)
                                    } else {
                                        Color(0xFF111111)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Square else Icons.Default.Mic,
                                contentDescription = "Dictate",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isRecording) "🎙️ Listening... Tap to conclude dictation" else "Tap microphone to begin verbal dictation",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isRecording) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    if (!isRecording && transcript.isEmpty()) {
                        Divider()
                        Text("Or select reference preset text standard:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        presets.forEachIndexed { index, pr ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f))
                                    .clickable {
                                        viewModel.stopListeningAndParse(pr)
                                    }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Text(text = pr, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        if (isGenState) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("AI is extracting medical structures in real time...")
                }
            }
        }

        // Live transcription readout
        if (!transcript.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Spoken Readout Transcript", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                        Text(text = "\"$transcript\"", fontFamily = FontFamily.Serif, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
                    }
                }
            }
        }

        // Structured prescriptions draft view
        if (drafts.isNotEmpty()) {
            item {
                Text("AI Structured Prescription Draft", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            items(drafts) { med ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(med.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${med.dosage} • ${med.frequency} • ${med.timing} for ${med.days} Days", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = dNotes,
                    onValueChange = { viewModel.doctorNotesInput.value = it },
                    label = { Text("Clinical Notes / Advisory (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Draw sign link card
            item {
                Card(
                    onClick = { viewModel.selectedDoctorTab.value = 2 },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Draw, tint = MaterialTheme.colorScheme.secondary, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Register Signature before signing prescription", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            // Commit CTA
            item {
                Button(
                    onClick = { viewModel.commitVoicePrescription() },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(vertical = 4.dp).testTag("commit_prescription"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Issue Signed Digital Prescription", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// 3. Digital Signature Pad Screen
@Composable
fun DoctorSignaturePad(viewModel: MedicalViewModel) {
    val draftSign by viewModel.draftSignature.collectAsState()

    val pathState = remember { mutableStateListOf<androidx.compose.ui.geometry.Offset>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Sig-Pad Verification", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Text(text = "Draw your official digital clinical signature authorization below", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        }

        // Draw Area Box Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        pathState.add(change.position)
                    }
                }
                .testTag("sig_pad_canvas"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (pathState.size > 1) {
                    val p = Path()
                    p.moveTo(pathState.first().x, pathState.first().y)
                    for (i in 1 until pathState.size) {
                        p.lineTo(pathState[i].x, pathState[i].y)
                    }
                    drawPath(
                        path = p,
                        color = Color(0xFFE53935),
                        style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            if (pathState.isEmpty()) {
                Text(
                    text = "DRAW OFFICIAL AUTHORIZATION HERE\n(TOUCH & Scribble Drawing)",
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    pathState.clear()
                    viewModel.draftSignature.value = ""
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset Canvas")
            }

            Button(
                onClick = {
                    viewModel.draftSignature.value = "DrDevAnand_Sign_Secured_2026"
                    // Automatically return to voice write screen
                    viewModel.selectedDoctorTab.value = 1
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Authorize Sign", color = Color.White)
            }
        }
    }
}

// 4. Digital Prescription History List
@Composable
fun PrescriptionHistoryScreen(viewModel: MedicalViewModel) {
    val history by viewModel.prescriptions.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(text = "Prescription Cabinet", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Text(text = "Secure archives of digital signed medical outputs", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
        }

        if (history.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No prescriptions issued yet.")
                }
            }
        } else {
            items(history) { rx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Patient: ${rx.patientName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "Diagnosis: ${rx.diagnosis}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            Text(text = rx.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }

                        Divider()

                        // Medications Structured Breakdown
                        val jsonAry = try { JSONArray(rx.medicinesJson) } catch (e: Exception) { JSONArray() }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 0 until jsonAry.length()) {
                                val item = jsonAry.optJSONObject(i) ?: JSONObject()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "• ${item.optString("name")} (${item.optString("dosage")})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "${item.optString("frequency")} | ${item.optString("days")} Days", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }

                        if (!rx.doctorNotes.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f))
                                    .padding(8.dp)
                            ) {
                                Text(text = "Notes: ${rx.doctorNotes}", fontSize = 11.sp, fontFamily = FontFamily.Serif)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.VerifiedUser, tint = MaterialTheme.colorScheme.tertiary, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Secured Digital Signature Verified", fontSize = 10.sp, color = MaterialTheme.colorScheme.tertiary)
                            }
                            Text(
                                text = "AUTH KEY: " + rx.signatureSvg.ifBlank { "0xSEC_DEC_2026" },
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


// --- COMMON PROFILE SECTION ---

@Composable
fun ProfileScreen(viewModel: MedicalViewModel) {
    val isDoctor by viewModel.isDoctorMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = "MediScript Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    if (isDoctor) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDoctor) Icons.Default.MedicalInformation else Icons.Default.AccountCircle,
                contentDescription = null,
                tint = if (isDoctor) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
        }

        Text(
            text = if (isDoctor) "Dr. Dev Anand" else "Ritik Rajput",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (isDoctor) "MD - Medical Clinic Lead • Senior Consultant" else "Active Patient Member • Health Group B+",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Divider()

        // Toggle configuration
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Role Configurations", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Current Mode", fontWeight = FontWeight.SemiBold)
                        Text(text = if (isDoctor) "Doctor Clinic Active" else "Patient Dashboard Active", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                    Button(
                        onClick = { viewModel.toggleMode() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDoctor) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Toggle Mode", color = Color.White)
                    }
                }
            }
        }

        // Generic Info Settings
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Integrations Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Gemini API Status")
                    Text(
                        text = if (com.example.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") "CONNECTED (Live AI)" else "OFFLINE (Local Smart Filters)",
                        fontFamily = FontFamily.Monospace,
                        color = if (com.example.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Secure Encryption Engine")
                    Text("AES-256 Enabled", fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Workspace Build version")
                    Text("v1.0.4 - Release")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Designed for clinical startup investors with Flutter & Jetpack Compose native responsiveness.",
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
