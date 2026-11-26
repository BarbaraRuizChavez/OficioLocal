package com.example.oficiolocal.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oficiolocal.R

private val Navy = Color(0xFF101A33)
private val Yellow = Color(0xFFFFC21A)
private val TextGray = Color(0xFF60636B)
private val FieldBorder = Color(0xFF9B9BA2)

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, modifier: Modifier = Modifier, viewModel: LoginViewModel = viewModel()) {
    var passwordVisible by remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).imePadding()) {
        Image(
            painter = painterResource(R.drawable.login_illustration),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.fillMaxWidth().height(310.dp),
            contentScale = ContentScale.Crop
        )
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 6.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = if (viewModel.isRegister) stringResource(R.string.login_title_register) else stringResource(R.string.login_title_login),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Navy
            )
            Text(
                text = if (viewModel.isRegister) stringResource(R.string.login_intro_register) else stringResource(R.string.login_intro_login),
                fontSize = 16.sp,
                color = TextGray
            )
            if (viewModel.isRegister) {
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = viewModel::onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.auth_name)) },
                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = TextGray) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = authFieldColors()
                )
            }
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = viewModel::onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.auth_email)) },
                leadingIcon = { Icon(Icons.Outlined.Email, null, tint = TextGray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = authFieldColors()
            )
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = viewModel::onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.auth_password)) },
                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = TextGray) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            stringResource(R.string.login_password_toggle),
                            tint = TextGray
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = authFieldColors()
            )
            if (viewModel.isRegister) {
                Text(stringResource(R.string.login_role_prompt), color = Navy, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = !viewModel.isProvider,
                        onClick = { viewModel.onRoleChange(false) },
                        label = { Text(stringResource(R.string.role_client)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = viewModel.isProvider,
                        onClick = { viewModel.onRoleChange(true) },
                        label = { Text(stringResource(R.string.role_provider)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            viewModel.errorRes?.let {
                Text(stringResource(it), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            Button(
                onClick = { viewModel.submit(onLoginSuccess) },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Navy)
            ) {
                Text(
                    text = if (viewModel.isRegister) stringResource(R.string.auth_register) else stringResource(R.string.auth_login),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(color = Color(0xFFF5F4F6), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (viewModel.isRegister) stringResource(R.string.auth_to_login) else stringResource(R.string.auth_to_register),
                        color = Navy,
                        fontSize = 16.sp
                    )
                    TextButton(onClick = { passwordVisible = false; viewModel.toggleMode() }) {
                        Text(
                            text = if (viewModel.isRegister) stringResource(R.string.auth_login) else stringResource(R.string.auth_register),
                            color = Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Navy, unfocusedTextColor = Navy,
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
    focusedBorderColor = Yellow, unfocusedBorderColor = FieldBorder,
    focusedLabelColor = Navy, unfocusedLabelColor = TextGray, cursorColor = Yellow
)
