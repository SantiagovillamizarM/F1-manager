//Reproduce efectos de sonido cargados desde /audio (src/main/resources/audio).
//Si el archivo todavía no existe, no reproduce nada en vez de fallar.
package com.f1manager.infraestructura.ui.util;

import javafx.scene.media.AudioClip;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class GestorSonido {

    private static final Map<String, AudioClip> CACHE = new HashMap<>();

    private GestorSonido() {
    }

    /** Reproduce un efecto de sonido dado su nombre de archivo dentro de /audio (ej. "choque.wav"). */
    public static void reproducir(String nombreArchivo) {
        AudioClip clip = CACHE.computeIfAbsent(nombreArchivo, GestorSonido::cargar);
        if (clip != null) {
            clip.play();
        }
    }

    private static AudioClip cargar(String nombreArchivo) {
        URL recurso = GestorSonido.class.getResource("/audio/" + nombreArchivo);
        return recurso != null ? new AudioClip(recurso.toExternalForm()) : null;
    }
}
