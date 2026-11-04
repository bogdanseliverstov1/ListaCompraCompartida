package com.example.listacompra

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class SubcategoriaMercadona(val id: Int, val nombre: String, val grupo: String)
data class GrupoMercadona(val nombre: String, val subcategorias: List<SubcategoriaMercadona>)
data class ProductoMercadona(val nombre: String, val precio: String, val imagen: String)

// Catálogo de Mercadona. Usa la API web de tienda.mercadona.es (no es oficial).
// "bcn1" = almacén de Barcelona.
object Mercadona {
    private const val BASE = "https://tienda.mercadona.es/api/categories/"
    private const val PARAMETROS = "lang=es&wh=bcn1"

    private fun descargar(direccion: String): JSONObject {
        val conexion = URL(direccion).openConnection() as HttpURLConnection
        conexion.connectTimeout = 15000
        conexion.readTimeout = 15000
        try {
            return JSONObject(conexion.inputStream.bufferedReader().use { it.readText() })
        } finally {
            conexion.disconnect()
        }
    }

    // Categorías principales con sus subcategorías
    fun grupos(): List<GrupoMercadona> {
        val resultados = descargar("$BASE?$PARAMETROS").getJSONArray("results")
        val lista = mutableListOf<GrupoMercadona>()
        for (i in 0 until resultados.length()) {
            val grupo = resultados.getJSONObject(i)
            val nombreGrupo = grupo.getString("name")
            val subs = grupo.getJSONArray("categories")
            val subcategorias = mutableListOf<SubcategoriaMercadona>()
            for (j in 0 until subs.length()) {
                val s = subs.getJSONObject(j)
                subcategorias.add(SubcategoriaMercadona(s.getInt("id"), s.getString("name"), nombreGrupo))
            }
            lista.add(GrupoMercadona(nombreGrupo, subcategorias))
        }
        return lista
    }

    // Productos de una subcategoría
    fun productos(idSubcategoria: Int): List<ProductoMercadona> {
        val datos = descargar("$BASE$idSubcategoria/?$PARAMETROS")
        val lista = mutableListOf<ProductoMercadona>()
        val categorias = datos.getJSONArray("categories")
        for (i in 0 until categorias.length()) {
            val productos = categorias.getJSONObject(i).getJSONArray("products")
            for (j in 0 until productos.length()) {
                val p = productos.getJSONObject(j)
                val precio = p.optJSONObject("price_instructions")?.optString("unit_price") ?: ""
                lista.add(
                    ProductoMercadona(
                        nombre = p.getString("display_name"),
                        precio = if (precio.isEmpty()) "" else precio.replace('.', ',') + " €",
                        imagen = p.optString("thumbnail")
                    )
                )
            }
        }
        return lista
    }
}
