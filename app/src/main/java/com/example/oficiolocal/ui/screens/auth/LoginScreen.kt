
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oficiolocal.R

private val Navy = Color(0xFF071A46)
private val Blue = Color(0xFF0757C8)
private val Orange = Color(0xFFFF9418)
private val LightBackground = Color(0xFFF4F7FC)
private val TextGray = Color(0xFF667085)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel()
) {
    var passwordVisible by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
    ) {
        // Guardamos la altura para utilizarla dentro del Column.
        val alturaPantalla = this.maxHeight

        // IMAGEN COMPLETA DE FONDO
        Image(
            painter = painterResource(id = R.drawable.logo_login),
            contentDescription = stringResource(R.string.login_background_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // CONTENIDO ENCIMA DE LA IMAGEN
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ESPACIO SUPERIOR PARA EL DISEÑO DEL FONDO
            Spacer(
                modifier = Modifier.height(alturaPantalla * 0.43f)
            )

            // TARJETA BLANCA DEL FORMULARIO
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // TÍTULO
                    Text(
                        text = stringResource(
                            if (viewModel.isRegister) R.string.login_title_register
                            else R.string.login_title_welcome
                        ),
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )

                    // DESCRIPCIÓN
                    Text(
                        text = stringResource(
                            if (viewModel.isRegister) R.string.login_intro_register
                            else R.string.login_intro_signin
                        ),
                        fontSize = 15.sp,
                        color = TextGray
                    )

                    // NOMBRE (SOLO REGISTRO)
                    if (viewModel.isRegister) {
                        OutlinedTextField(
                            value = viewModel.name,
                            onValueChange = viewModel::onNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(stringResource(R.string.auth_name))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = authFieldColors()
                        )
                    }

                    // CORREO ELECTRÓNICO
                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = viewModel::onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(stringResource(R.string.auth_email))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = authFieldColors()
                    )

                    // CONTRASEÑA
                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = viewModel::onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(stringResource(R.string.auth_password))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    passwordVisible = !passwordVisible
                                }
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Outlined.Visibility
                                    else
                                        Icons.Outlined.VisibilityOff,
                                    contentDescription = stringResource(R.string.login_password_toggle)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = authFieldColors()
                    )

                    // TIPO DE USUARIO (SOLO REGISTRO)
                    if (viewModel.isRegister) {
                        Text(
                            text = stringResource(R.string.login_role_prompt),
                            color = Navy,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = !viewModel.isProvider,
                                onClick = {
                                    viewModel.onRoleChange(false)
                                },
                                label = {
                                    Text(stringResource(R.string.role_client))
                                },
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = viewModel.isProvider,
                                onClick = {
                                    viewModel.onRoleChange(true)
                                },
                                label = {
                                    Text(stringResource(R.string.role_provider))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // MENSAJES DE ERROR
                    viewModel.errorRes?.let { error ->
                        Text(
                            text = stringResource(error),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }

                    // BOTÓN INICIAR SESIÓN / REGISTRARSE
                    Button(
                        onClick = {
                            viewModel.submit(onLoginSuccess)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Orange,
                            contentColor = Navy
                        )
                    ) {
                        Text(
                            text = stringResource(
                                if (viewModel.isRegister)
                                    R.string.auth_register
                                else
                                    R.string.auth_login
                            ),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // SEPARADOR
                    HorizontalDivider(
                        color = LightBackground
                    )

                    // CAMBIAR ENTRE LOGIN Y REGISTRO
                    TextButton(
                        onClick = {
                            passwordVisible = false
                            viewModel.toggleMode()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(
                                if (viewModel.isRegister)
                                    R.string.auth_to_login
                                else
                                    R.string.auth_to_register
                            ),
                            color = Navy,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // PIE DE PANTALLA
            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = "© OficioLocal",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
}

// COLORES DE LOS CAMPOS
@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Navy,
    unfocusedTextColor = Navy,

    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,

    focusedBorderColor = Blue,
    unfocusedBorderColor = Color(0xFFD9E0EB),

    focusedLabelColor = Blue,
    unfocusedLabelColor = TextGray,

    cursorColor = Orange
)
