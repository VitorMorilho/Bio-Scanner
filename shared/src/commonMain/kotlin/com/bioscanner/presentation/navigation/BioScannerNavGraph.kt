package com.bioscanner.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bioscanner.presentation.auth.AuthScreen
import com.bioscanner.presentation.capture.CaptureScreen
import com.bioscanner.presentation.common.BottomNavTab
import com.bioscanner.presentation.detail.SpeciesDetailScreen
import com.bioscanner.presentation.glossary.GlossaryScreen
import com.bioscanner.presentation.home.HomeScreen
import com.bioscanner.presentation.intro.IntroScreen
import com.bioscanner.presentation.records.ObservationDetailScreen
import com.bioscanner.presentation.records.RecordsScreen
import com.bioscanner.presentation.register.RegisterScreen

@Composable
fun BioScannerNavGraph(
    startDestination: String = Routes.INTRO,
    navController: NavHostController = rememberNavController(),
) {
    val onTabClick: (BottomNavTab) -> Unit = { tab -> navController.navigateToTab(tab.route) }
    val openSpecies: (String) -> Unit = { speciesId -> navController.navigate(Routes.detail(speciesId)) }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.INTRO) {
            IntroScreen(
                onFinished = {
                    // Tira a abertura do back stack: "voltar" na Home fecha o app.
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.INTRO) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onCaptureClick = { navController.navigate(Routes.CAPTURE) },
                onTabClick = onTabClick,
            )
        }
        composable(Routes.GLOSSARY) {
            GlossaryScreen(onTabClick = onTabClick, onSpeciesClick = openSpecies)
        }
        composable(Routes.RECORDS) {
            RecordsScreen(
                onTabClick = onTabClick,
                onRecordClick = { id -> navController.navigate(Routes.observation(id)) },
                onSignInClick = { navController.navigate(Routes.AUTH) },
            )
        }
        composable(Routes.AUTH) {
            // Fecha ao concluir o login ou ao voltar da primeira etapa. A checagem evita fechar
            // duas vezes (login concluído + "voltar" no mesmo instante) e tirar "Meus registros" da pilha.
            AuthScreen(
                onClose = {
                    if (navController.currentDestination?.route == Routes.AUTH) navController.popBackStack()
                },
            )
        }
        composable(Routes.CAPTURE) {
            CaptureScreen(
                onPhotoReady = { photo ->
                    val encoded = encodeRouteArgument(photo.uri)
                    navController.navigate(Routes.register(encoded, photo.fromCamera))
                },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                // Sai da captura/registro direto para a lista, onde o novo registro aparece no topo.
                onSaved = { navController.navigateToTab(Routes.RECORDS) },
                onRetake = { navController.popBackStack() },
            )
        }
        composable(Routes.OBSERVATION) {
            ObservationDetailScreen(onDeleted = { navController.popBackStack() })
        }
        composable(Routes.DETAIL) {
            SpeciesDetailScreen()
        }
    }
}

private val BottomNavTab.route: String
    get() = when (this) {
        BottomNavTab.REGISTER -> Routes.HOME
        BottomNavTab.GLOSSARY -> Routes.GLOSSARY
        BottomNavTab.RECORDS -> Routes.RECORDS
    }

// Navega entre as abas raiz preservando o estado de cada uma, em vez de
// empilhar telas duplicadas no back stack. Ancora na Home, não no início do
// grafo: o início é a abertura (IntroScreen), que já saiu da pilha.
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
