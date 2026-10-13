package com.bioscanner.presentation.records

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bioscanner.presentation.common.BioScannerBottomNav
import com.bioscanner.presentation.common.BottomNavTab
import com.bioscanner.presentation.common.ObservationPhoto
import com.bioscanner.presentation.common.formatDateTime
import com.bioscanner.ui.theme.ForestGreen
import com.bioscanner.ui.theme.TextSecondary

@Composable
fun RecordsScreen(
    onTabClick: (BottomNavTab) -> Unit,
    onRecordClick: (observationId: Long) -> Unit,
    onSignInClick: () -> Unit,
    viewModel: RecordsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Excluir conta?") },
            text = {
                Text(
                    "Sua conta será apagada permanentemente, junto com todos os registros e fotos " +
                        "salvos neste aparelho. Essa ação não pode ser desfeita. É preciso estar conectado à internet.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDelete = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Excluir conta") }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text("Cancelar") }
            },
        )
    }

    Scaffold(
        bottomBar = { BioScannerBottomNav(current = BottomNavTab.RECORDS, onTabClick = onTabClick) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ÁREA DO USUÁRIO",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                    )
                    Text(text = "Meus registros", style = MaterialTheme.typography.headlineMedium, color = ForestGreen)
                }
            }
            item {
                UserCard(
                    state = state,
                    onSignInClick = onSignInClick,
                    onSignOutClick = viewModel::signOut,
                    onDeleteAccountClick = { confirmingDelete = true },
                    onMessageDismiss = viewModel::dismissAccountMessage,
                )
            }

            when {
                state.loading -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.records.isEmpty() -> item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Nenhum registro ainda. Fotografe uma serpente para enviar o primeiro.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                        Button(onClick = { onTabClick(BottomNavTab.REGISTER) }) { Text("Registrar") }
                    }
                }
                else -> items(state.records, key = { it.observation.id }) { item ->
                    RecordCard(item, onClick = { onRecordClick(item.observation.id) })
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    state: RecordsUiState,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onMessageDismiss: () -> Unit,
) {
    val account = state.account
    val (name, subtitle) = when (account) {
        AccountState.Loading -> "…" to ""
        AccountState.SignedOut -> "Sem conta" to "Seus registros ficam salvos neste aparelho."
        is AccountState.SignedIn -> account.user.displayName to (account.user.email ?: "")
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp),
                    )
                }
                Column {
                    Text(text = name, style = MaterialTheme.typography.titleMedium)
                    if (subtitle.isNotEmpty()) {
                        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Stat(value = state.records.size, label = "registros")
                Stat(value = state.distinctRegionsCount, label = "estados")
                Stat(value = state.withLocationCount, label = "com local")
            }

            state.accountMessage?.let { message ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = message, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = onMessageDismiss) { Text("OK") }
                }
            }

            when {
                state.accountBusy -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
                account is AccountState.SignedOut -> Button(onClick = onSignInClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Entrar ou criar conta")
                }
                account is AccountState.SignedIn -> Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = onSignOutClick) { Text("Sair") }
                    TextButton(
                        onClick = onDeleteAccountClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) { Text("Excluir conta") }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun Stat(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge, color = ForestGreen)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun RecordCard(item: RecordItem, onClick: () -> Unit) {
    val observation = item.observation
    // Miniatura de 72dp: decodificar em ~2x evita carregar a foto inteira por item.
    val thumbPx = with(LocalDensity.current) { 144.dp.roundToPx() }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ObservationPhoto(
                photo = observation.photoPath,
                contentDescription = null,
                maxSizePx = thumbPx,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = formatDateTime(observation.metadata.takenAt),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = item.regionName ?: if (observation.metadata.location != null) "Fora do Brasil" else "Sem localização",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }
    }
}
