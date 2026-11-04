@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.listacompra

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores que recuerdan a Mercadona: verde, rojo y fondo claro
val VerdeMercadona = Color(0xFF00833E)
val VerdeOscuro = Color(0xFF00572B)
val VerdeClaro = Color(0xFFE3F2E7)
val RojoMercadona = Color(0xFFD7182A)
val FondoClaro = Color(0xFFF5F8F5)

@Composable
fun TemaApp(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = VerdeMercadona,
            onPrimary = Color.White,
            primaryContainer = VerdeClaro,
            onPrimaryContainer = VerdeOscuro,
            secondary = RojoMercadona,
            onSecondary = Color.White,
            background = FondoClaro,
            surface = Color.White
        ),
        content = contenido
    )
}

// Barra superior verde, igual en todas las pantallas
@Composable
fun coloresBarra() = TopAppBarDefaults.topAppBarColors(
    containerColor = VerdeMercadona,
    titleContentColor = Color.White,
    navigationIconContentColor = Color.White,
    actionIconContentColor = Color.White
)
