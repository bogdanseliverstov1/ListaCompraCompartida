package com.example.listacompra

import com.google.firebase.firestore.DocumentId

// Una lista de la compra. "miembros" son los emails que tienen acceso (incluido el propietario).
data class ListaCompra(
    @DocumentId var id: String = "",
    var nombre: String = "",
    var propietario: String = "",
    var miembros: List<String> = emptyList()
)

// Categoría dentro de una lista (ej: "Fruta y verdura")
data class Categoria(
    @DocumentId var id: String = "",
    var nombre: String = ""
)

// Producto dentro de una lista. "completado" = ya está comprado (no se borra).
data class Producto(
    @DocumentId var id: String = "",
    var nombre: String = "",
    var categoriaId: String = "",
    var completado: Boolean = false,
    var emoji: String = "",
    var imagen: String = "",
    var precio: String = ""
)
