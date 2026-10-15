package com.bioscanner.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.bioscanner.domain.auth.MIN_PASSWORD_LENGTH
import com.bioscanner.presentation.platform.AppConfig
import com.bioscanner.ui.theme.ForestGreen
import com.bioscanner.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthScreen(
    onClose: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
    config: AppConfig = koinInject(),
) {
    val state by viewModel.uiState.collectAsState()
    val googleSignIn = rememberGoogleSignIn(config.googleWebClientId)
    val scope = rememberCoroutineScope()
    var googleInProgress by remember { mutableStateOf(false) }
    val busy = state.loading || googleInProgress

    LaunchedEffect(Unit) {
        viewModel.done.collect { onClose() }
    }
    // "Voltar" do sistema percorre as etapas antes de fechar a tela.
    BackHandler { if (!viewModel.back()) onClose() }

    val googleEnabled = googleSignIn != null
    val onGoogleClick = {
        googleInProgress = true
        viewModel.onGoogleStarted()
        scope.launch {
            val result = checkNotNull(googleSignIn)()
            googleInProgress = false
            viewModel.onGoogleResult(result)
        }
        Unit
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IconButton(onClick = { if (!viewModel.back()) onClose() }) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
            }

            val (title, subtitle) = state.step.texts(config.emailCodesEnabled)
            Text(text = title, style = MaterialTheme.typography.headlineMedium, color = ForestGreen)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

            state.info?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }

            when (state.step) {
                AuthStep.SIGN_IN -> {
                    EmailField(state, viewModel)
                    PasswordField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        error = state.passwordError,
                        label = "Senha",
                        isNew = false,
                        onDone = viewModel::submit,
                    )
                }
                AuthStep.SIGN_UP -> {
                    AuthTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Nome",
                        error = state.nameError,
                        contentType = ContentType.PersonFullName,
                    )
                    EmailField(state, viewModel)
                    PasswordField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        error = state.passwordError,
                        label = "Senha",
                        isNew = true,
                        onDone = viewModel::submit,
                    )
                }
                AuthStep.CONFIRM_SIGN_UP -> CodeField(state, viewModel, imeAction = ImeAction.Done)
                AuthStep.FORGOT_PASSWORD -> EmailField(state, viewModel, imeAction = ImeAction.Done)
                AuthStep.RESET_PASSWORD -> {
                    CodeField(state, viewModel, imeAction = ImeAction.Next)
                    PasswordField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        error = state.passwordError,
                        label = "Nova senha",
                        isNew = true,
                        onDone = viewModel::submit,
                    )
                }
            }

            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = viewModel::submit,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(state.step.submitLabel(), fontWeight = FontWeight.Bold)
                }
            }

            when (state.step) {
                AuthStep.SIGN_IN -> {
                    if (config.emailCodesEnabled) {
                        TextButton(onClick = { viewModel.goTo(AuthStep.FORGOT_PASSWORD) }, enabled = !busy) {
                            Text("Esqueci minha senha")
                        }
                    }
                    if (googleEnabled) GoogleSection(busy = busy, googleInProgress = googleInProgress, onClick = onGoogleClick)
                    SwitchStep("Não tem conta?", "Criar conta", enabled = !busy) { viewModel.goTo(AuthStep.SIGN_UP) }
                }
                AuthStep.SIGN_UP -> {
                    if (googleEnabled) GoogleSection(busy = busy, googleInProgress = googleInProgress, onClick = onGoogleClick)
                    SwitchStep("Já tem conta?", "Entrar", enabled = !busy) { viewModel.goTo(AuthStep.SIGN_IN) }
                }
                AuthStep.CONFIRM_SIGN_UP -> TextButton(onClick = viewModel::resendSignUpCode, enabled = !busy) {
                    Text("Reenviar código")
                }
                AuthStep.FORGOT_PASSWORD, AuthStep.RESET_PASSWORD -> Unit
            }
        }
    }
}

private fun AuthStep.texts(emailCodesEnabled: Boolean): Pair<String, String> = when (this) {
    AuthStep.SIGN_IN -> "Entrar" to "Sua conta é opcional. Os registros continuam salvos neste aparelho."
    AuthStep.SIGN_UP -> "Criar conta" to if (emailCodesEnabled) {
        "Leva menos de um minuto. Vamos enviar um código para confirmar seu email."
    } else {
        "Leva menos de um minuto."
    }
    AuthStep.CONFIRM_SIGN_UP -> "Confirme seu email" to "Digite o código que enviamos por email. Confira também a caixa de spam."
    AuthStep.FORGOT_PASSWORD -> "Recuperar senha" to "Informe o email da conta para receber um código."
    AuthStep.RESET_PASSWORD -> "Nova senha" to "Digite o código recebido por email e escolha uma nova senha."
}

private fun AuthStep.submitLabel(): String = when (this) {
    AuthStep.SIGN_IN -> "Entrar"
    AuthStep.SIGN_UP -> "Criar conta"
    AuthStep.CONFIRM_SIGN_UP -> "Confirmar"
    AuthStep.FORGOT_PASSWORD -> "Enviar código"
    AuthStep.RESET_PASSWORD -> "Salvar nova senha"
}

@Composable
private fun EmailField(state: AuthUiState, viewModel: AuthViewModel, imeAction: ImeAction = ImeAction.Next) {
    AuthTextField(
        value = state.email,
        onValueChange = viewModel::onEmailChange,
        label = "Email",
        error = state.emailError,
        contentType = ContentType.EmailAddress,
        keyboardType = KeyboardType.Email,
        imeAction = imeAction,
        onDone = viewModel::submit,
    )
}

@Composable
private fun CodeField(state: AuthUiState, viewModel: AuthViewModel, imeAction: ImeAction) {
    AuthTextField(
        value = state.code,
        onValueChange = viewModel::onCodeChange,
        label = "Código",
        error = state.codeError,
        contentType = ContentType.SmsOtpCode,
        keyboardType = KeyboardType.NumberPassword,
        imeAction = imeAction,
        onDone = viewModel::submit,
    )
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    contentType: ContentType,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        // Permite que gerenciadores de senha preencham os campos.
        modifier = Modifier.fillMaxWidth().semantics { this.contentType = contentType },
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    label: String,
    isNew: Boolean,
    onDone: () -> Unit,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = (error ?: if (isNew) "Mínimo de $MIN_PASSWORD_LENGTH caracteres, com letras e números." else null)?.let { { Text(it) } },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Ocultar senha" else "Mostrar senha",
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = Modifier.fillMaxWidth().semantics {
            contentType = if (isNew) ContentType.NewPassword else ContentType.Password
        },
    )
}

@Composable
private fun GoogleSection(busy: Boolean, googleInProgress: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text("ou", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
    OutlinedButton(
        onClick = onClick,
        enabled = !busy,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        if (googleInProgress) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text("Continuar com Google")
        }
    }
}

@Composable
private fun SwitchStep(question: String, action: String, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(question, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        TextButton(onClick = onClick, enabled = enabled) { Text(action, fontWeight = FontWeight.Bold) }
    }
}
