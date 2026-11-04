Lista de la compra compartida

App Android para llevar la lista de la compra entre varias personas. Cada uno inicia sesión con su cuenta de Google, crea sus listas y las comparte por email. Lo que alguien añade o tacha se ve al momento en el móvil de los demás. También tiene el catálogo de Mercadona para añadir productos con su foto y su precio.
Está hecha con Kotlin, Jetpack Compose y Firebase (Authentication y Firestore). He intentado que el código sea sencillo, así que no uso MVVM: el estado se guarda en cada pantalla con `remember` y `mutableStateOf`.

1 Qué hace la app

- Login con Google.
- Cada usuario tiene su lista personal ("Mi lista", que se crea sola la primera vez) y puede crear todas las que quiera.
- Se puede compartir una lista escribiendo el email de Google de otra persona. Cualquier miembro puede añadir gente, pero solo el propietario puede quitarla.
- Los productos se agrupan por categorías, que se pueden crear, renombrar y borrar.
- Hay una fila de productos habituales (leche, pan, manzana...) que se añaden con un toque.
- Si escribes un producto a mano, la app intenta reconocerlo y le pone su categoría y su icono.
  Si no lo reconoce lo mete en "Otros".
  
- Catálogo de Mercadona: se navega por categorías y subcategorías, hay buscador y se añade el producto con foto y precio.
- Al marcar un producto como comprado se tacha y baja al final de su categoría, no se borra.
- Los productos se pueden editar (nombre y categoría) o borrar.
- Si borras una lista que es tuya, desaparece para todos. Si no es tuya, solo sales de ella.

Los colores (verde y rojo con fondo claro) recuerdan a Mercadona.

2 Tecnologías

- Kotlin 2.0.21
- Jetpack Compose con Material 3
- Firebase Authentication y Cloud Firestore
- Credential Manager para el login con Google
- Coil para cargar las imágenes
- Android Gradle Plugin 8.13.2, Gradle 8.14.6 y Java 17
- minSdk 24, compileSdk y targetSdk 35

3 Estructura

```
app/src/main/java/com/example/listacompra/
    MainActivity.kt      decide si mostrar el login o las listas
    PantallaLogin.kt     inicio de sesión con Google
    PantallaListas.kt    pantalla "Mis listas"
    PantallaLista.kt     una lista: categorías, productos y compartir
    PantallaCatalogo.kt  catálogo de Mercadona
    Mercadona.kt         descarga de categorías y productos de Mercadona
    Habituales.kt        productos habituales y clasificación automática
    Componentes.kt       acceso a Firestore, diálogos y borrado de listas
    Modelos.kt           ListaCompra, Categoria y Producto
    Tema.kt              colores y tema
firestore.rules          reglas de seguridad de Firestore
```

4 Base de datos

Todo está en Firestore con esta estructura:

```
listas/{listaId}
    nombre
    propietario        (email)
    miembros           (lista de emails, incluido el propietario)
    categorias/{id}
        nombre
    productos/{id}
        nombre
        categoriaId
        completado
        emoji
        imagen         (URL, solo si viene del catálogo)
        precio
```

El acceso a cada lista se controla por email: solo puede leerla y modificarla quien esté en `miembros`, 
y solo el propietario puede borrarla. Las reglas están en `firestore.rules`.

5 Cómo ponerla en marcha:

Hace falta Android Studio, JDK 17 y un móvil o emulador con Google Play Services y una cuenta de Google.

1. Crear un proyecto en la consola de Firebase y añadir una app Android con el paquete `com.example.listacompra`.
2. Añadir la huella SHA-1 de la clave de depuración en los ajustes de la app. Se saca con `./gradlew signingReport`.
3. Descargar `google-services.json` y copiarlo en la carpeta `app/`.
4. En Authentication, activar el método de acceso Google.
5. Crear la base de datos en Firestore y pegar en la pestaña de reglas el contenido de `firestore.rules`.
6. Copiar el ID de cliente web de Google en `app/src/main/res/values/strings.xml`, en `default_web_client_id`.
7. Abrir el proyecto en Android Studio, sincronizar Gradle y ejecutarlo.

6 Cómo probar lo de compartir:

Hacen falta dos cuentas de Google. Con la primera se abre una lista y se pulsa el icono de compartir para añadir el email de la segunda. Al entrar con la segunda cuenta en otro móvil o emulador, la lista aparece en "Mis listas" como compartida. Si se añade o se tacha un producto en uno de los dos, se actualiza en el otro.

7 ejemplo visual:

  1) abrimos la app:
     <img width="250" height="600" alt="image" src="https://github.com/user-attachments/assets/c51d4fee-b6ac-4326-b0ca-5acdb3448338" />
  
  2) creamos nueva lista:
  <img width="250" height="600" alt="image" src="https://github.com/user-attachments/assets/c6a64eb8-8aa2-4d45-9089-e9750d03d7c7" />
  <img width="250" height="600" alt="image" src="https://github.com/user-attachments/assets/f3f5f793-47bf-4e26-a164-0a732301e412" />

  3) entramos en el catalogo "Mercadona" y añadimos un producto
  <img width="250" height="600" alt="image" src="https://github.com/user-attachments/assets/50788d99-cb40-4237-a006-57afc43d3f4d" />
  <img width="250" height="600" alt="image" src="https://github.com/user-attachments/assets/a05858ac-2cdb-49b3-9af3-ae40cde4fb82" />
  <img width="250" height="600" alt="image" src="https://github.com/user-attachments/assets/3bd7fa88-4693-44b8-873d-c6641d6f0531" />
  <img width="250" height="600" alt="image" src="https://github.com/user-attachments/assets/ec7a113c-e13e-469d-8756-4cdddf4b4488" />
  
  4) compartimos la lista:

     <img width="250" height="600" alt="image" src="https://github.com/user-attachments/assets/a9c8e8f8-4e2e-4121-a493-35c6781a3111" />
     <img width="250" height="600" alt="image" src="https://github.com/user-attachments/assets/dedae5bd-58db-4c54-bc86-3aae63d78ac1" />




8 Notas:

- El catálogo de Mercadona no usa una API oficial, sino las peticiones web de tienda.mercadona.es con el almacén de Barcelona. Puede dejar de funcionar si Mercadona cambia algo.
- El catálogo necesita internet. Si falla, sale un mensaje con un botón para reintentar.
- Para compartir hay que poner el email de la cuenta de Google de la otra persona.
- El `google-services.json` es de nuestro proyecto de Firebase. Para usar la app con otro proyecto hay que cambiarlo por el propio.
