package com.example.listacompra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaApp {
                AppPrincipal()
            }
        }
    }
}

// Si no hay sesión -> pantalla de login. Si hay sesión -> las listas del usuario.
@Composable
fun AppPrincipal() {
    val auth = FirebaseAuth.getInstance()
    var usuario by remember { mutableStateOf(auth.currentUser) }

    val u = usuario
    if (u == null) {
        PantallaLogin(onLoginCorrecto = { usuario = auth.currentUser })
    } else {
        ConSesion(
            uid = u.uid,
            email = (u.email ?: "").lowercase(),
            onCerrarSesion = {
                auth.signOut()
                usuario = null
            }
        )
    }
}

// Aquí se gestionan TODAS las listas del usuario (la suya y las que le han compartido)
@Composable
fun ConSesion(uid: String, email: String, onCerrarSesion: () -> Unit) {
    var listas by remember { mutableStateOf(listOf<ListaCompra>()) }
    var listaAbiertaId by remember { mutableStateOf<String?>(null) }

    // Escucha en tiempo real las listas donde el email del usuario está en "miembros"
    DisposableEffect(email) {
        val registro = db.collection("listas")
            .whereArrayContains("miembros", email)
            .addSnapshotListener { datos, _ ->
                if (datos != null) {
                    listas = datos.toObjects(ListaCompra::class.java)
                        .sortedBy { it.nombre.lowercase() }

                    // Primera vez que entra: se le crea su lista personal (id = su uid)
                    if (datos.isEmpty && !datos.metadata.isFromCache) {
                        db.collection("listas").document(uid).set(
                            hashMapOf<String, Any>(
                                "nombre" to "Mi lista",
                                "propietario" to email,
                                "miembros" to listOf(email)
                            )
                        )
                    }
                }
            }
        onDispose { registro.remove() }
    }

    val listaAbierta = listas.find { it.id == listaAbiertaId }
    if (listaAbierta == null) {
        PantallaListas(
            listas = listas,
            email = email,
            onAbrir = { listaAbiertaId = it.id },
            onCerrarSesion = onCerrarSesion
        )
    } else {
        PantallaLista(
            lista = listaAbierta,
            email = email,
            onVolver = { listaAbiertaId = null }
        )
    }
}
