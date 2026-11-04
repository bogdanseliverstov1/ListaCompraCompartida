package com.example.listacompra

import java.text.Normalizer

// Producto habitual con su icono y la categoría a la que pertenece
data class Habitual(val nombre: String, val emoji: String, val categoria: String)

val habituales = listOf(
    // Los más específicos primero (ej: "Patatas fritas" antes que "Patata")
    Habitual("Patatas fritas", "🍟", "Aperitivos"),
    Habitual("Pasta de dientes", "🪥", "Cuidado personal"),
    Habitual("Papel higiénico", "🧻", "Cuidado personal"),
    Habitual("Papel de cocina", "🧻", "Limpieza y hogar"),
    Habitual("Salsa", "🥫", "Aceite, especias y salsas"),

    Habitual("Leche", "🥛", "Huevos, leche y mantequilla"),
    Habitual("Huevo", "🥚", "Huevos, leche y mantequilla"),
    Habitual("Mantequilla", "🧈", "Huevos, leche y mantequilla"),
    Habitual("Yogur", "🥣", "Postres y yogures"),
    Habitual("Queso", "🧀", "Charcutería y quesos"),
    Habitual("Jamón", "🍖", "Charcutería y quesos"),

    Habitual("Manzana", "🍎", "Fruta y verdura"),
    Habitual("Plátano", "🍌", "Fruta y verdura"),
    Habitual("Naranja", "🍊", "Fruta y verdura"),
    Habitual("Limón", "🍋", "Fruta y verdura"),
    Habitual("Fresa", "🍓", "Fruta y verdura"),
    Habitual("Uva", "🍇", "Fruta y verdura"),
    Habitual("Pera", "🍐", "Fruta y verdura"),
    Habitual("Sandía", "🍉", "Fruta y verdura"),
    Habitual("Tomate", "🍅", "Fruta y verdura"),
    Habitual("Lechuga", "🥬", "Fruta y verdura"),
    Habitual("Zanahoria", "🥕", "Fruta y verdura"),
    Habitual("Patata", "🥔", "Fruta y verdura"),
    Habitual("Cebolla", "🧅", "Fruta y verdura"),
    Habitual("Ajo", "🧄", "Fruta y verdura"),
    Habitual("Pepino", "🥒", "Fruta y verdura"),
    Habitual("Aguacate", "🥑", "Fruta y verdura"),
    Habitual("Brócoli", "🥦", "Fruta y verdura"),
    Habitual("Champiñón", "🍄", "Fruta y verdura"),

    Habitual("Pollo", "🍗", "Carne"),
    Habitual("Carne", "🥩", "Carne"),
    Habitual("Salchicha", "🌭", "Carne"),
    Habitual("Bacon", "🥓", "Carne"),

    Habitual("Pescado", "🐟", "Marisco y pescado"),
    Habitual("Salmón", "🐟", "Marisco y pescado"),
    Habitual("Atún", "🐟", "Marisco y pescado"),
    Habitual("Merluza", "🐟", "Marisco y pescado"),
    Habitual("Gamba", "🦐", "Marisco y pescado"),

    Habitual("Pan", "🍞", "Panadería y pastelería"),
    Habitual("Baguette", "🥖", "Panadería y pastelería"),
    Habitual("Croissant", "🥐", "Panadería y pastelería"),

    Habitual("Arroz", "🍚", "Arroz, legumbres y pasta"),
    Habitual("Pasta", "🍝", "Arroz, legumbres y pasta"),
    Habitual("Lenteja", "🥫", "Arroz, legumbres y pasta"),
    Habitual("Aceite", "🫒", "Aceite, especias y salsas"),
    Habitual("Sal", "🧂", "Aceite, especias y salsas"),
    Habitual("Café", "☕", "Cacao, café e infusiones"),
    Habitual("Cereal", "🥣", "Cereales y galletas"),
    Habitual("Galleta", "🍪", "Cereales y galletas"),
    Habitual("Chocolate", "🍫", "Azúcar, caramelos y chocolate"),
    Habitual("Azúcar", "🍬", "Azúcar, caramelos y chocolate"),

    Habitual("Agua", "💧", "Agua y refrescos"),
    Habitual("Refresco", "🥤", "Agua y refrescos"),
    Habitual("Zumo", "🧃", "Agua y refrescos"),
    Habitual("Cerveza", "🍺", "Bodega"),
    Habitual("Vino", "🍷", "Bodega"),

    Habitual("Pizza", "🍕", "Congelados"),
    Habitual("Helado", "🍦", "Congelados"),

    Habitual("Detergente", "🧴", "Limpieza y hogar"),
    Habitual("Lavavajillas", "🧽", "Limpieza y hogar"),
    Habitual("Lejía", "🧴", "Limpieza y hogar"),
    Habitual("Esponja", "🧽", "Limpieza y hogar"),

    Habitual("Champú", "🧴", "Cuidado personal"),
    Habitual("Jabón", "🧼", "Cuidado personal"),
    Habitual("Desodorante", "🧴", "Cuidado personal")
)

// Quita mayúsculas y tildes: "Plátano" -> "platano"
fun normalizar(texto: String): String {
    return Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
}

// Clasificación automática: si escribes "leche" o "leches" encuentra el habitual "Leche".
// Busca la palabra completa (con "s" o "es" opcional al final), así "sal" no coincide con "salmón".
fun buscarHabitual(texto: String): Habitual? {
    val t = normalizar(texto.trim())
    return habituales.firstOrNull { h ->
        Regex("(^| )" + Regex.escape(normalizar(h.nombre)) + "(s|es)?( |$)").containsMatchIn(t)
    }
}
