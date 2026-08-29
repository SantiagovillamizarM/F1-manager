//Carga imágenes de mapa de bits desde /imagenes (src/main/resources/imagenes).
//Si el archivo todavía no existe, devuelve null en vez de fallar.
package com.f1manager.infraestructura.ui.util;

import javafx.scene.image.Image;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class GestorImagenes {

    private static final Map<String, Image> CACHE = new HashMap<>();

    private GestorImagenes() {
    }

    /** Carga (o reutiliza si ya se cargó antes) una imagen dado su nombre de archivo dentro de /imagenes. */
    public static Image cargar(String nombreArchivo) {
        return CACHE.computeIfAbsent(nombreArchivo, GestorImagenes::cargarDesdeRecurso);
    }

    /** URL lista para usar en un Image (ej. para guardarla como foto de un piloto), o null si el archivo no existe. */
    public static String urlDe(String nombreArchivo) {
        URL recurso = GestorImagenes.class.getResource("/imagenes/" + nombreArchivo);
        return recurso != null ? recurso.toExternalForm() : null;
    }

    private static Image cargarDesdeRecurso(String nombreArchivo) {
        URL recurso = GestorImagenes.class.getResource("/imagenes/" + nombreArchivo);
        return recurso != null ? new Image(recurso.toExternalForm()) : null;
    }
}
