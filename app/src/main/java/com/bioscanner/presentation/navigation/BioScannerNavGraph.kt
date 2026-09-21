package com.bioscanner.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bioscanner.presentation.capture.CaptureScreen
import com.bioscanner.presentation.detail.SpeciesDetailScreen
import com.bioscanner.presentation.home.HomeScreen
import com.bioscanner.presentation.result.ResultScreen

@Composable
fun BioScannerNavGraph(
    startDestination: String = Routes.HOME,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.HOME) {
            HomeScreen(onCaptureClick = { navController.navigate(Routes.CAPTURE) })
        }
        composable(Routes.CAPTURE) {
            CaptureScreen(
                onImageReady = { uri ->
                    val encoded = Uri.encode(uri.toString())
                    navController.navigate(Routes.result(encoded))
                },
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(
                onViewDetails = { speciesId -> navController.navigate(Routes.detail(speciesId)) },
                onTryAgain = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
        composable(Routes.DETAIL) {
            SpeciesDetailScreen()
        }
    }
}
