//Reproduce efectos de sonido cargados desde /audio (src/main/resources/audio).
//Si el archivo todavía no existe, no reproduce nada en vez de fallar.
package com.f1manager.infraestructura.ui.util;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class GestorSonido {

    private static final Map<String, AudioClip> CACHE = new HashMap<>();
    // Se guarda como campo estático (no variable local) para que no la recoja el recolector de
    // basura mientras suena: un MediaPlayer sin referencias vivas puede cortarse a mitad de pista.
    private static MediaPlayer musicaDeFondo;

    private GestorSonido() {
    }

    /** Reproduce un efecto de sonido dado su nombre de archivo dentro de /audio (ej. "choque.wav"). */
    public static void reproducir(String nombreArchivo) {
        AudioClip clip = CACHE.computeIfAbsent(nombreArchivo, GestorSonido::cargar);
        if (clip != null) {
            clip.play();
        }
    }

    /** Reproduce música de fondo en bucle infinito (reemplaza la que estuviera sonando, si había). */
    public static void reproducirMusicaDeFondo(String nombreArchivo) {
        URL recurso = GestorSonido.class.getResource("/audio/" + nombreArchivo);
        if (recurso == null) {
            return;
        }
        if (musicaDeFondo != null) {
            musicaDeFondo.stop();
        }
        musicaDeFondo = new MediaPlayer(new Media(recurso.toExternalForm()));
        musicaDeFondo.setCycleCount(MediaPlayer.INDEFINITE);
        musicaDeFondo.play();
    }

    /** Alterna silencio/sonido de la música de fondo y devuelve el nuevo estado (true = muteada). */
    public static boolean alternarMuteMusicaDeFondo() {
        if (musicaDeFondo == null) {
            return false;
        }
        boolean nuevoEstado = !musicaDeFondo.isMute();
        musicaDeFondo.setMute(nuevoEstado);
        return nuevoEstado;
    }

    public static boolean isMusicaDeFondoMuteada() {
        return musicaDeFondo != null && musicaDeFondo.isMute();
    }

    private static AudioClip cargar(String nombreArchivo) {
        URL recurso = GestorSonido.class.getResource("/audio/" + nombreArchivo);
        return recurso != null ? new AudioClip(recurso.toExternalForm()) : null;
    }
}
