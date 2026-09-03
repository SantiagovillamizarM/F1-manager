//Carga imágenes de mapa de bits desde /imagenes (src/main/resources/imagenes) y las
//guarda en caché para no tener que volver a leerlas del disco cada vez que se piden.
//Si el archivo todavía no existe, devuelve null en vez de fallar.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.util;

//Trae la clase Image de JavaFX, que representa una imagen ya cargada en memoria lista para mostrarse en pantalla.
import javafx.scene.image.Image;

//Trae la clase URL, que representa la dirección/ubicación de un archivo (en este caso, dentro de los recursos del proyecto).
import java.net.URL;
//Trae HashMap, una implementación de diccionario (clave-valor) que no garantiza ningún orden particular.
import java.util.HashMap;
//Importa la interfaz general Map, que sirve como plantilla para crear diccionarios de clave y valor.
import java.util.Map;

//Clase publica y final (no se puede heredar de ella) llamada "GestorImagenes"
public final class GestorImagenes {

    //Diccionario (Map) estático que guarda las imágenes ya cargadas usando el nombre de archivo como clave,
    //para no tener que leerlas otra vez del disco cada vez que se piden (mejora el rendimiento).
    private static final Map<String, Image> CACHE = new HashMap<>();

    //Constructor privado y vacío: esta clase es solo de utilidades (puros métodos estáticos),
    //no tiene sentido crear un objeto GestorImagenes con "new", por eso se bloquea el constructor.
    private GestorImagenes() {
    }

    //Carga (o reutiliza si ya se cargó antes) una imagen dado su nombre de archivo dentro de /imagenes.
    //computeIfAbsent revisa si la clave (nombreArchivo) ya está guardada en el CACHE: si está, devuelve
    //la imagen guardada sin hacer nada más; si no está, la calcula llamando a cargarDesdeRecurso y deja
    //el resultado guardado en el mapa para la próxima vez que se pida.
    public static Image cargar(String nombreArchivo) {
        return CACHE.computeIfAbsent(nombreArchivo, GestorImagenes::cargarDesdeRecurso);
    }

    //URL lista para usar en un Image (ej. para guardarla como foto de un piloto), o null si el archivo no existe.
    public static String urlDe(String nombreArchivo) {
        //Busca el archivo dentro de la carpeta /imagenes de los recursos del proyecto
        URL recurso = GestorImagenes.class.getResource("/imagenes/" + nombreArchivo);
        //Si lo encontró, devuelve su dirección en formato texto; si no, devuelve null en vez de fallar
        return recurso != null ? recurso.toExternalForm() : null;
    }

    //Método privado que hace la lectura real del archivo desde los recursos del proyecto y arma el
    //objeto Image. Solo lo usa cargar() (a través de computeIfAbsent), cuando la imagen todavía no
    //estaba guardada en el CACHE.
    private static Image cargarDesdeRecurso(String nombreArchivo) {
        //Busca el archivo dentro de la carpeta /imagenes de los recursos del proyecto
        URL recurso = GestorImagenes.class.getResource("/imagenes/" + nombreArchivo);
        //Si lo encontró, crea la Image a partir de su dirección; si no, devuelve null en vez de fallar
        return recurso != null ? new Image(recurso.toExternalForm()) : null;
    }
}
