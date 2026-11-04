package com.example.listacompra

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore

// Base de datos (Firestore) compartida por toda la app
val db: FirebaseFirestore = FirebaseFirestore.getInstance()

// Diálogo con una caja de texto (sirve para crear y renombrar)
@Composable
fun DialogoTexto(
    titulo: String,
    inicial: String,
    etiqueta: String,
    onAceptar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var texto by remember { mutableStateOf(inicial) }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text(etiqueta) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (texto.isNotBlank()) onAceptar(texto.trim()) }) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

// Diálogo de confirmación Sí / No
@Composable
fun DialogoConfirmar(mensaje: String, onSi: () -> Unit, onNo: () -> Unit) {
    AlertDialog(
        onDismissRequest = onNo,
        text = { Text(mensaje) },
        confirmButton = { TextButton(onClick = onSi) { Text("Sí") } },
        dismissButton = { TextButton(onClick = onNo) { Text("No") } }
    )
}

// Borra una lista entera con sus categorías y productos
fun borrarLista(listaId: String) {
    val lista = db.collection("listas").document(listaId)
    lista.collection("categorias").get().addOnSuccessListener { categorias ->
        lista.collection("productos").get().addOnSuccessListener { productos ->
            val lote = db.batch()
            categorias.documents.forEach { lote.delete(it.reference) }
            productos.documents.forEach { lote.delete(it.reference) }
            lote.delete(lista)
            lote.commit()
        }
    }
}
