package com.example.listacompra

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun PantallaLogin(onLoginCorrecto: () -> Unit) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerdeMercadona)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🛒", fontSize = 80.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Lista de la compra",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Crea listas y compártelas con quien quieras",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))

        Button(
            enabled = !cargando,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = VerdeOscuro),
            onClick = {
                cargando = true
                error = ""
                scope.launch {
                    try {
                        val webClientId = contexto.getString(R.string.default_web_client_id)

                        // 1. Pedimos a Google la cuenta del usuario
                        val opcion = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(webClientId)
                            .build()

                        val peticion = GetCredentialRequest.Builder()
                            .addCredentialOption(opcion)
                            .build()

                        val resultado = CredentialManager.create(contexto).getCredential(contexto, peticion)
                        val token = GoogleIdTokenCredential.createFrom(resultado.credential.data).idToken

                        // 2. Entramos en Firebase con corrutinas await()
                        FirebaseAuth.getInstance()
                            .signInWithCredential(GoogleAuthProvider.getCredential(token, null))
                            .await()

                        onLoginCorrecto()
                    } catch (_: GetCredentialCancellationException) {
                        // User cancelled Google Sign-In sheet
                    } catch (_: NoCredentialException) {
                        error = "No hay cuentas disponibles. Revisa que tu archivo google-services.json esté actualizado."
                    } catch (e: Exception) {
                        error = e.message ?: "No se pudo iniciar sesión. Inténtalo de nuevo."
                    } finally {
                        cargando = false
                    }
                }
            }
        ) {
            Text(if (cargando) "Entrando..." else "Iniciar sesión con Google", fontSize = 16.sp)
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(error, color = Color.White, textAlign = TextAlign.Center)
        }
    }
}
