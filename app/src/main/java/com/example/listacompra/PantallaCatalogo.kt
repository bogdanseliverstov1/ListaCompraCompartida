@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.listacompra

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Catálogo de Mercadona: categorías -> subcategorías -> productos -> "Añadir" a la lista
@Composable
fun PantallaCatalogo(
    onVolver: () -> Unit,
    onAnadir: (nombre: String, categoria: String, imagen: String, precio: String) -> Unit
) {
    var grupos by remember { mutableStateOf(listOf<GrupoMercadona>()) }
    var grupoAbierto by remember { mutableStateOf<String?>(null) }
    var subcategoria by remember { mutableStateOf<SubcategoriaMercadona?>(null) }
    var productos by remember { mutableStateOf(listOf<ProductoMercadona>()) }
    var anadidos by remember { mutableStateOf(setOf<String>()) }
    var busqueda by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var intento by remember { mutableStateOf(0) }

    // Descarga las categorías al abrir, o los productos al elegir una subcategoría
    LaunchedEffect(subcategoria, intento) {
        cargando = true
        error = ""
        try {
            val sub = subcategoria
            if (sub == null) {
                if (grupos.isEmpty()) {
                    grupos = withContext(Dispatchers.IO) { Mercadona.grupos() }
                }
            } else {
                productos = emptyList()
                productos = withContext(Dispatchers.IO) { Mercadona.productos(sub.id) }
            }
        } catch (e: Exception) {
            error = "No se pudo cargar el catálogo de Mercadona. Comprueba tu conexión a internet."
        }
        cargando = false
    }

    // Atrás: de los productos vuelve a las categorías; de las categorías vuelve a la lista
    fun atras() {
        if (subcategoria != null) {
            subcategoria = null
            busqueda = ""
        } else {
            onVolver()
        }
    }
    BackHandler { atras() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subcategoria?.nombre ?: "Catálogo Mercadona") },
                colors = coloresBarra(),
                navigationIcon = {
                    IconButton(onClick = { atras() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                label = { Text(if (subcategoria == null) "Buscar categoría (ej: leche)" else "Buscar producto") },
                singleLine = true
            )

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VerdeMercadona)
                }
            } else if (error.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(error, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { intento++ }) { Text("Reintentar") }
                }
            } else if (subcategoria != null) {
                // ----- Productos de la subcategoría -----
                val visibles = productos.filter {
                    busqueda.isBlank() || normalizar(it.nombre).contains(normalizar(busqueda))
                }
                LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp)) {
                    items(visibles) { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(model = p.imagen, contentDescription = null, modifier = Modifier.size(56.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.nombre)
                                Text(p.precio, color = VerdeOscuro, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            if (p.nombre in anadidos) {
                                Text("✓ Añadido", color = VerdeMercadona, fontWeight = FontWeight.Bold)
                            } else {
                                OutlinedButton(onClick = {
                                    onAnadir(p.nombre, subcategoria?.grupo ?: "Otros", p.imagen, p.precio)
                                    anadidos = anadidos + p.nombre
                                }) { Text("Añadir") }
                            }
                        }
                    }
                }
            } else if (busqueda.isNotBlank()) {
                // ----- Resultados de buscar entre todas las subcategorías -----
                val resultados = grupos.flatMap { it.subcategorias }
                    .filter { normalizar(it.nombre).contains(normalizar(busqueda)) }
                LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp)) {
                    items(resultados) { sub ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    subcategoria = sub
                                    busqueda = ""
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(sub.nombre, fontWeight = FontWeight.Bold)
                            Text(sub.grupo, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // ----- Categorías principales (se abren al tocarlas) -----
                LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp)) {
                    grupos.forEach { grupo ->
                        item {
                            Text(
                                (if (grupoAbierto == grupo.nombre) "▾  " else "▸  ") + grupo.nombre,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .background(VerdeClaro, RoundedCornerShape(8.dp))
                                    .clickable {
                                        grupoAbierto = if (grupoAbierto == grupo.nombre) null else grupo.nombre
                                    }
                                    .padding(12.dp),
                                fontWeight = FontWeight.Bold,
                                color = VerdeOscuro
                            )
                        }
                        if (grupoAbierto == grupo.nombre) {
                            items(grupo.subcategorias) { sub ->
                                Text(
                                    sub.nombre,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { subcategoria = sub }
                                        .padding(start = 24.dp, top = 10.dp, bottom = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
