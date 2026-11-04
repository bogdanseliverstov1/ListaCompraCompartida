@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.listacompra

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FieldValue

// Pantalla con todas las listas del usuario: la suya y las que le han compartido
@Composable
fun PantallaListas(
    listas: List<ListaCompra>,
    email: String,
    onAbrir: (ListaCompra) -> Unit,
    onCerrarSesion: () -> Unit
) {
    var creando by remember { mutableStateOf(false) }
    var listaABorrar by remember { mutableStateOf<ListaCompra?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis listas") },
                colors = coloresBarra(),
                actions = {
                    TextButton(onClick = onCerrarSesion) { Text("Salir", color = Color.White) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { creando = true },
                containerColor = RojoMercadona,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva lista")
            }
        }
    ) { padding ->
        if (listas.isEmpty()) {
            Text("Cargando tus listas...", modifier = Modifier.padding(padding).padding(16.dp), color = Color.Gray)
        }
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(listas) { lista ->
                val esMia = lista.propietario == email
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onAbrir(lista) },
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(lista.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            val detalle = if (esMia) {
                                if (lista.miembros.size > 1) "Tuya · compartida con ${lista.miembros.size - 1}" else "Tuya"
                            } else {
                                "Compartida por ${lista.propietario}"
                            }
                            Text(detalle, color = Color.Gray, fontSize = 13.sp)
                        }
                        IconButton(onClick = { listaABorrar = lista }) {
                            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = RojoMercadona)
                        }
                    }
                }
            }
        }
    }

    if (creando) {
        DialogoTexto(
            titulo = "Nueva lista",
            inicial = "",
            etiqueta = "Nombre de la lista",
            onAceptar = { nombre ->
                db.collection("listas").add(
                    hashMapOf<String, Any>(
                        "nombre" to nombre,
                        "propietario" to email,
                        "miembros" to listOf(email)
                    )
                )
                creando = false
            },
            onCancelar = { creando = false }
        )
    }

    listaABorrar?.let { lista ->
        val esMia = lista.propietario == email
        DialogoConfirmar(
            mensaje = if (esMia) "¿Eliminar la lista «${lista.nombre}» para todos?" else "¿Salir de la lista «${lista.nombre}»?",
            onSi = {
                if (esMia) {
                    borrarLista(lista.id)
                } else {
                    // Si no es tuya, solo te quitas tú de los miembros
                    db.collection("listas").document(lista.id).update("miembros", FieldValue.arrayRemove(email))
                }
                listaABorrar = null
            },
            onNo = { listaABorrar = null }
        )
    }
}
