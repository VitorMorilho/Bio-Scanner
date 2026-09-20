package com.bioscanner

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity mínima anotada com @AndroidEntryPoint, usada só nos testes
 * instrumentados: hiltViewModel() exige um host compatível com Hilt, o que a
 * ComponentActivity genérica não fornece.
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
