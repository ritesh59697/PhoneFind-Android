package com.ritesh.phonefind.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.phonefind.R

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Signup
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onAuthSuccess()
        }
    }

    // Neo-Brutalist Off-White Canvas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4EE))
            .imePadding()
            .navigationBarsPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Logo Box with Neo-Brutalist Hard Shadow
            Box(modifier = Modifier.size(72.dp)) {
                // Hard Black Shadow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(Color.Black, shape = RoundedCornerShape(10.dp))
                )
                // Main Logo Box
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White, shape = RoundedCornerShape(10.dp))
                        .border(2.5.dp, Color.Black, shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_phonefind_logo),
                        contentDescription = "PhoneFind Logo",
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PhoneFind",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 30.sp,
                    color = Color.Black
                )
            )

            // High Contrast Neo-Brutalist Pill Badge
            Box(
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 24.dp)
                    .background(Color(0xFFFFE600), shape = RoundedCornerShape(6.dp))
                    .border(2.dp, Color.Black, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "HARDWARE SECURITY CONSOLE",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }

            // Neo-Brutalist Card Container with Hard Black Offset Shadow
            Box(modifier = Modifier.fillMaxWidth()) {
                // 1. Solid Hard Black Offset Shadow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .background(Color.Black, shape = RoundedCornerShape(12.dp))
                )

                // 2. Main Card Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .border(2.5.dp, Color.Black, shape = RoundedCornerShape(12.dp))
                        .padding(20.dp)
                ) {
                    // Neo-Brutalist Tab Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(Color(0xFFE5E5E0), shape = RoundedCornerShape(8.dp))
                            .border(2.dp, Color.Black, shape = RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .then(
                                    if (selectedTab == 0) {
                                        Modifier
                                            .background(Color.Black, shape = RoundedCornerShape(6.dp))
                                            .border(1.5.dp, Color.Black, shape = RoundedCornerShape(6.dp))
                                    } else Modifier
                                )
                                .clickable {
                                    selectedTab = 0
                                    viewModel.resetState()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "LOG IN",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = if (selectedTab == 0) Color.White else Color.Black
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .then(
                                    if (selectedTab == 1) {
                                        Modifier
                                            .background(Color.Black, shape = RoundedCornerShape(6.dp))
                                            .border(1.5.dp, Color.Black, shape = RoundedCornerShape(6.dp))
                                    } else Modifier
                                )
                                .clickable {
                                    selectedTab = 1
                                    viewModel.resetState()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "SIGN UP",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = if (selectedTab == 1) Color.White else Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email Input Field
                    Text(
                        text = "EMAIL ADDRESS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("name@domain.com", color = Color(0xFFAAAAAA), fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9F7),
                            unfocusedContainerColor = Color(0xFFF9F9F7),
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Input Field
                    Text(
                        text = "PASSWORD",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", color = Color(0xFFAAAAAA), fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9F7),
                            unfocusedContainerColor = Color(0xFFF9F9F7),
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                    )

                    if (selectedTab == 1) {
                        Text(
                            text = "MINIMUM 8 CHARACTERS REQUIRED",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF555555),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    if (uiState is AuthUiState.Error) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFD1D1), shape = RoundedCornerShape(6.dp))
                                .border(2.dp, Color(0xFFDC2626), shape = RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = (uiState as AuthUiState.Error).message,
                                color = Color(0xFFDC2626),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Neo-Brutalist Action Button with Hard Offset Shadow
                    val isFormValid = email.isNotBlank() && password.isNotBlank()

                    Box(modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        if (isFormValid) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 4.dp, y = 4.dp)
                                    .background(Color.Black, shape = RoundedCornerShape(8.dp))
                            )
                        }

                        Button(
                            onClick = {
                                if (selectedTab == 0) {
                                    viewModel.login(email, password)
                                } else {
                                    viewModel.signup(email, password)
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 2.5.dp,
                                    color = if (isFormValid) Color.Black else Color(0xFF888888),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFE5E5E0),
                                disabledContentColor = Color(0xFF777777)
                            ),
                            enabled = uiState !is AuthUiState.Loading && isFormValid
                        ) {
                            if (uiState is AuthUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = if (selectedTab == 0) "AUTHENTICATE CONSOLE" else "INITIALIZE ACCOUNT",
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Neo-Brutalist Security Badge Footer
            Box(
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(6.dp))
                    .border(2.dp, Color.Black, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HARDWARE-BACKED HARDWARE SECURITY",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
