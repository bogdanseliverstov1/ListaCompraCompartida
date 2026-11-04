@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.listacompra

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FieldValue

// Pantalla de UNA lista: categorías, productos, compartir y catálogo de Mercadona
@Composable
fun PantallaLista(lista: ListaCompra, email: String, onVolver: () -> Unit) {
    var categorias by remember { mutableStateOf(listOf<Categoria>()) }
    var productos by remember { mutableStateOf(listOf<Producto>()) }
    var nuevoProducto by remember { mutableStateOf("") }

    var verCatalogo by remember { mutableStateOf(false) }
    var compartiendo by remember { mutableStateOf(false) }
    var creandoCategoria by remember { mutableStateOf(false) }
    var categoriaAEditar by remember { mutableStateOf<Categoria?>(null) }
    var categoriaABorrar by remember { mutableStateOf<Categoria?>(null) }
    var productoAEditar by remember { mutableStateOf<Producto?>(null) }

    val docLista = remember(lista.id) { db.collection("listas").document(lista.id) }

    // Escucha en tiempo real las categorías y productos (si otra persona cambia algo, se ve al instante)
    DisposableEffect(lista.id) {
        val r1 = docLista.collection("categorias").addSnapshotListener { datos, _ ->
            if (datos != null) {
                categorias = datos.toObjects(Categoria::class.java).sortedBy { it.nombre.lowercase() }
            }
        }
        val r2 = docLista.collection("productos").addSnapshotListener { datos, _ ->
            if (datos != null) {
                productos = datos.toObjects(Producto::class.java)
            }
        }
        onDispose {
            r1.remove()
            r2.remove()
        }
    }

    // Añade un producto. Si la categoría no existe en la lista, la crea.
    fun anadirProducto(
        nombre: String,
        nombreCategoria: String,
        emoji: String = "",
        imagen: String = "",
        precio: String = ""
    ) {
        val categoriaId = categorias.find { it.nombre.equals(nombreCategoria, ignoreCase = true) }?.id
            ?: docLista.collection("categorias").document().also {
                it.set(hashMapOf("nombre" to nombreCategoria))
            }.id
        docLista.collection("productos").add(
            hashMapOf<String, Any>(
                "nombre" to nombre,
                "categoriaId" to categoriaId,
                "completado" to false,
                "emoji" to emoji,
                "imagen" to imagen,
                "precio" to precio
            )
        )
    }

    // Producto escrito a mano: se clasifica solo ("leche" -> "Huevos, leche y mantequilla")
    fun anadirDesdeTexto() {
        val nombre = nuevoProducto.trim()
        if (nombre.isEmpty()) return
        val habitual = buscarHabitual(nombre)
        anadirProducto(nombre, habitual?.categoria ?: "Otros", habitual?.emoji ?: "")
        nuevoProducto = ""
    }

    if (verCatalogo) {
        PantallaCatalogo(
            onVolver = { verCatalogo = false },
            onAnadir = { nombre, categoria, imagen, precio ->
                anadirProducto(nombre, categoria, "", imagen, precio)
            }
        )
    } else {
        BackHandler { onVolver() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(lista.nombre) },
                    colors = coloresBarra(),
                    navigationIcon = {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = { compartiendo = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).imePadding().fillMaxSize()) {

                // Botones: catálogo y nueva categoría
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { verCatalogo = true }, modifier = Modifier.weight(1f)) {
                        Text("🛒 Catálogo Mercadona")
                    }
                    OutlinedButton(onClick = { creandoCategoria = true }) {
                        Text("+ Categoría")
                    }
                }

                // Productos habituales con su icono (un toque y se añaden)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(habituales) { h ->
                        AssistChip(
                            onClick = { anadirProducto(h.nombre, h.categoria, h.emoji) },
                            label = { Text("${h.emoji} ${h.nombre}") }
                        )
                    }
                }

                // Categorías con sus productos
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    if (categorias.isEmpty()) {
                        item {
                            Text(
                                "La lista está vacía. Escribe un producto abajo o toca uno de los habituales 👆",
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    categorias.forEach { categoria ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .background(VerdeClaro, RoundedCornerShape(8.dp))
                                    .padding(start = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    categoria.nombre,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    color = VerdeOscuro
                                )
                                IconButton(onClick = { categoriaAEditar = categoria }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Renombrar", tint = VerdeOscuro)
                                }
                                IconButton(onClick = { categoriaABorrar = categoria }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = RojoMercadona)
                                }
                            }
                        }
                        // Los completados se quedan al final de su categoría
                        items(productos.filter { it.categoriaId == categoria.id }.sortedBy { it.completado }) { producto ->
                            FilaProducto(
                                producto = producto,
                                onMarcar = {
                                    docLista.collection("productos").document(producto.id)
                                        .update("completado", !producto.completado)
                                },
                                onEditar = { productoAEditar = producto },
                                onBorrar = { docLista.collection("productos").document(producto.id).delete() }
                            )
                        }
                    }
                }

                // Caja para escribir un producto nuevo
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = nuevoProducto,
                        onValueChange = { nuevoProducto = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Nuevo producto") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { anadirDesdeTexto() })
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { anadirDesdeTexto() }) { Text("Añadir") }
                }
            }
        }
    }

    // ---------- Diálogos ----------

    if (compartiendo) {
        DialogoCompartir(lista = lista, email = email, onCerrar = { compartiendo = false })
    }

    if (creandoCategoria) {
        DialogoTexto(
            titulo = "Nueva categoría",
            inicial = "",
            etiqueta = "Nombre de la categoría",
            onAceptar = { nombre ->
                docLista.collection("categorias").add(hashMapOf("nombre" to nombre))
                creandoCategoria = false
            },
            onCancelar = { creandoCategoria = false }
        )
    }

    categoriaAEditar?.let { categoria ->
        DialogoTexto(
            titulo = "Renombrar categoría",
            inicial = categoria.nombre,
            etiqueta = "Nombre de la categoría",
            onAceptar = { nombre ->
                docLista.collection("categorias").document(categoria.id).update("nombre", nombre)
                categoriaAEditar = null
            },
            onCancelar = { categoriaAEditar = null }
        )
    }

    categoriaABorrar?.let { categoria ->
        DialogoConfirmar(
            mensaje = "¿Eliminar la categoría «${categoria.nombre}» y todos sus productos?",
            onSi = {
                productos.filter { it.categoriaId == categoria.id }.forEach {
                    docLista.collection("productos").document(it.id).delete()
                }
                docLista.collection("categorias").document(categoria.id).delete()
                categoriaABorrar = null
            },
            onNo = { categoriaABorrar = null }
        )
    }

    productoAEditar?.let { producto ->
        DialogoEditarProducto(
            producto = producto,
            categorias = categorias,
            onGuardar = { nombre, categoriaId ->
                docLista.collection("productos").document(producto.id)
                    .update(mapOf("nombre" to nombre, "categoriaId" to categoriaId))
                productoAEditar = null
            },
            onCancelar = { productoAEditar = null }
        )
    }
}

// Una fila de producto: casilla de completado, icono/foto, nombre, editar y borrar
@Composable
fun FilaProducto(producto: Producto, onMarcar: () -> Unit, onEditar: () -> Unit, onBorrar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = producto.completado, onCheckedChange = { onMarcar() })

        if (producto.imagen.isNotEmpty()) {
            AsyncImage(model = producto.imagen, contentDescription = null, modifier = Modifier.size(40.dp))
        } else if (producto.emoji.isNotEmpty()) {
            Text(producto.emoji, fontSize = 26.sp)
        }
        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                producto.nombre,
                textDecoration = if (producto.completado) TextDecoration.LineThrough else null,
                color = if (producto.completado) Color.Gray else Color.Black
            )
            if (producto.precio.isNotEmpty()) {
                Text(producto.precio, fontSize = 12.sp, color = Color.Gray)
            }
        }

        IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
        IconButton(onClick = onBorrar) {
            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = RojoMercadona)
        }
    }
}

// Cambiar el nombre y la categoría de un producto
@Composable
fun DialogoEditarProducto(
    producto: Producto,
    categorias: List<Categoria>,
    onGuardar: (String, String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(producto.nombre) }
    var categoriaId by remember { mutableStateOf(producto.categoriaId) }
    var menuAbierto by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Editar producto") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Text("Categoría", fontSize = 12.sp, color = Color.Gray)
                Box {
                    OutlinedButton(onClick = { menuAbierto = true }) {
                        Text(categorias.find { it.id == categoriaId }?.nombre ?: "Elegir")
                    }
                    DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                        categorias.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.nombre) },
                                onClick = {
                                    categoriaId = c.id
                                    menuAbierto = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (nombre.isNotBlank()) onGuardar(nombre.trim(), categoriaId) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

// Compartir la lista: se añade el email de Google de la otra persona
@Composable
fun DialogoCompartir(lista: ListaCompra, email: String, onCerrar: () -> Unit) {
    var nuevo by remember { mutableStateOf("") }
    var aviso by remember { mutableStateOf("") }
    val doc = db.collection("listas").document(lista.id)

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Compartir lista") },
        text = {
            Column {
                Text("Personas con acceso:", fontWeight = FontWeight.Bold)
                lista.miembros.forEach { miembro ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (miembro == lista.propietario) "$miembro (propietario)" else miembro,
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp
                        )
                        // Solo el propietario puede quitar a otras personas
                        if (miembro != lista.propietario && email == lista.propietario) {
                            IconButton(onClick = { doc.update("miembros", FieldValue.arrayRemove(miembro)) }) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = nuevo,
                    onValueChange = { nuevo = it },
                    label = { Text("Email de Google") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                if (aviso.isNotEmpty()) {
                    Text(aviso, color = RojoMercadona, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val emailNuevo = nuevo.trim().lowercase()
                if (!emailNuevo.contains("@")) {
                    aviso = "Escribe un email válido"
                } else {
                    doc.update("miembros", FieldValue.arrayUnion(emailNuevo))
                    nuevo = ""
                    aviso = ""
                }
            }) { Text("Añadir") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}
