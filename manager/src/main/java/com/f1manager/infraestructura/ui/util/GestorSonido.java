//Reproduce efectos de sonido cargados desde /audio (src/main/resources/audio) y
//maneja también la música de fondo (reproducir, silenciar, parar).
//Si el archivo todavía no existe, no reproduce nada en vez de fallar.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.util;

//Trae AudioClip, un sonido corto de JavaFX pensado para efectos (se puede reproducir muchas veces seguidas, ej. clics, choques).
import javafx.scene.media.AudioClip;
//Trae Media, que representa el archivo de audio/video cargado (la "fuente"), lo que reproduce en verdad el MediaPlayer.
import javafx.scene.media.Media;
//Trae MediaPlayer, el reproductor de JavaFX pensado para pistas más largas (como la música de fondo), con play/pausa/bucle.
import javafx.scene.media.MediaPlayer;

//Trae la clase URL, que representa la dirección/ubicación de un archivo (en este caso, dentro de los recursos del proyecto).
import java.net.URL;
//Trae HashMap, una implementación de diccionario (clave-valor) que no garantiza ningún orden particular.
import java.util.HashMap;
//Importa la interfaz general Map, que sirve como plantilla para crear diccionarios de clave y valor.
import java.util.Map;

//Clase publica y final (no se puede heredar de ella) llamada "GestorSonido"
public final class GestorSonido {

    //Diccionario (Map) estático que guarda los efectos de sonido ya cargados usando el nombre de
    //archivo como clave, para no tener que leerlos otra vez del disco cada vez que se piden.
    private static final Map<String, AudioClip> CACHE = new HashMap<>();
    // Se guarda como campo estático (no variable local) para que no la recoja el recolector de
    // basura mientras suena: un MediaPlayer sin referencias vivas puede cortarse a mitad de pista.
    private static MediaPlayer musicaDeFondo;

    //Constructor privado y vacío: esta clase es solo de utilidades (puros métodos estáticos),
    //no tiene sentido crear un objeto GestorSonido con "new", por eso se bloquea el constructor.
    private GestorSonido() {
    }

    //Reproduce un efecto de sonido dado su nombre de archivo dentro de /audio (ej. "choque.wav").
    public static void reproducir(String nombreArchivo) {
        //Busca el clip en el CACHE; si no estaba, lo carga con cargar() y lo deja guardado para la próxima vez
        AudioClip clip = CACHE.computeIfAbsent(nombreArchivo, GestorSonido::cargar);
        //Si el archivo existía (clip no es null), lo reproduce; si no, no pasa nada (sin fallar)
        if (clip != null) {
            clip.play();
        }
    }

    //Reproduce música de fondo en bucle infinito (reemplaza la que estuviera sonando, si había).
    public static void reproducirMusicaDeFondo(String nombreArchivo) {
        //Busca el archivo dentro de la carpeta /audio de los recursos del proyecto
        URL recurso = GestorSonido.class.getResource("/audio/" + nombreArchivo);
        //Si el archivo no existe, no hace nada en vez de fallar
        if (recurso == null) {
            return;
        }
        //Si ya había una música de fondo sonando, se detiene primero para no encimar los dos audios
        if (musicaDeFondo != null) {
            musicaDeFondo.stop();
        }
        //Arma el reproductor a partir del Media (la fuente del audio) y lo pone a repetirse para siempre
        musicaDeFondo = new MediaPlayer(new Media(recurso.toExternalForm()));
        musicaDeFondo.setCycleCount(MediaPlayer.INDEFINITE);
        musicaDeFondo.play();
    }

    //Alterna silencio/sonido de la música de fondo y devuelve el nuevo estado (true = muteada).
    public static boolean alternarMuteMusicaDeFondo() {
        //Si todavía no hay ninguna música de fondo cargada, no hay nada que silenciar
        if (musicaDeFondo == null) {
            return false;
        }
        //Invierte el estado actual (si estaba sonando, lo muteamos; si estaba muteado, lo destapamos)
        boolean nuevoEstado = !musicaDeFondo.isMute();
        musicaDeFondo.setMute(nuevoEstado);
        return nuevoEstado;
    }

    //Getter que indica si la música de fondo está actualmente muteada (false si ni siquiera hay música cargada)
    public static boolean isMusicaDeFondoMuteada() {
        return musicaDeFondo != null && musicaDeFondo.isMute();
    }

    //Método privado que hace la lectura real del efecto de sonido desde los recursos del proyecto
    //y arma el AudioClip. Solo lo usa reproducir() (a través de computeIfAbsent), cuando el sonido
    //todavía no estaba guardado en el CACHE.
    private static AudioClip cargar(String nombreArchivo) {
        //Busca el archivo dentro de la carpeta /audio de los recursos del proyecto
        URL recurso = GestorSonido.class.getResource("/audio/" + nombreArchivo);
        //Si lo encontró, crea el AudioClip a partir de su dirección; si no, devuelve null en vez de fallar
        return recurso != null ? new AudioClip(recurso.toExternalForm()) : null;
    }
}
