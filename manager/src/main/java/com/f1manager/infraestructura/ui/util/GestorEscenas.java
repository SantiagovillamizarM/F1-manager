//Controla el cambio entre pantallas de toda la aplicación, aplicando las transiciones de
// fundido (fade) y guardando el historial para poder "volver".

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.util;
//Trae el componente propio FondoAnimado, el fondo animado que se ve detrás de todas las pantallas.
import com.f1manager.infraestructura.ui.components.FondoAnimado;
//Trae FadeTransition, una animación de JavaFX que cambia gradualmente la opacidad (transparencia) de un nodo.
import javafx.animation.FadeTransition;
//Trae SequentialTransition, que permite encadenar animaciones para que se reproduzcan una detrás de otra.
import javafx.animation.SequentialTransition;
//Trae Platform, la clase de JavaFX con utilidades generales de la aplicación (aquí se usa para cerrarla).
import javafx.application.Platform;
//Trae EventTarget, la interfaz que representa "sobre qué objeto cayó" un evento (ej. un clic del mouse).
import javafx.event.EventTarget;
//Trae Node, la clase base de todo lo que se puede dibujar/mostrar en una escena de JavaFX (botones, textos, contenedores, etc.).
import javafx.scene.Node;
//Trae Scene, el "lienzo" completo de JavaFX que se muestra dentro de la ventana (Stage).
import javafx.scene.Scene;
//Trae ButtonBase, la clase base de todos los controles tipo botón de JavaFX.
import javafx.scene.control.ButtonBase;
//Trae MouseEvent, la información de un evento de mouse (clic, posición, etc.).
import javafx.scene.input.MouseEvent;
//Trae Region, la clase base de los contenedores/controles que tienen tamaño (ancho/alto), usada aquí para representar cualquier pantalla.
import javafx.scene.layout.Region;
//Trae StackPane, un contenedor que apila sus hijos uno encima del otro (útil para superponer el fondo animado y el contenido).
import javafx.scene.layout.StackPane;
//Trae Stage, la ventana real del sistema operativo donde se muestra la aplicación.
import javafx.stage.Stage;
//Trae Duration, que representa una cantidad de tiempo (aquí, cuánto dura cada transición de fundido).
import javafx.util.Duration;
//Trae ArrayDeque, una implementación de "pila/cola de dos puntas" (Deque) basada en un arreglo, rápida para apilar y desapilar.
import java.util.ArrayDeque;
//Trae la interfaz Deque, que define el comportamiento de una estructura tipo pila/cola por ambos extremos (se usa aquí como historial de pantallas).
import java.util.Deque;


//Clase publica y final (no se puede heredar de ella) llamada "GestorEscenas"
public final class GestorEscenas {
      //Contenedor que guarda la pantalla actualmente visible (encima del fondo animado)
      private final StackPane contenido = new StackPane();

    //Cuánto dura cada transición de fundido (fade) entre pantallas
    private static final Duration DURACION_FADE = Duration.millis(260);

    //La ventana real de la aplicación
    private final Stage stage;
    //Contenedor raíz de toda la escena: apila el fondo animado debajo y el contenido (pantallas) encima
    private final StackPane raiz = new StackPane();
    //Pila (Deque) que guarda el historial de pantallas visitadas, para poder "volver" a la anterior
    private final Deque<Region> historial = new ArrayDeque<>();
    //Bandera que indica si hay una transición de fundido en curso, para no dejar que se disparen otras encima
    private boolean animando = false;


    //Constructor
    //Arma la ventana principal de la aplicación: prepara el fondo animado, la escena, la hoja de
    //estilos y el sonido de clic sobre cualquier botón, y deja la ventana lista (maximizada) para mostrar pantallas.
    public GestorEscenas(Stage stage) {
        this.stage = stage;
        raiz.setStyle("-fx-background-color: #05070d;");
        FondoAnimado fondo = new FondoAnimado();
        //El fondo animado se estira para ocupar siempre el mismo tamaño que el contenedor raíz
        fondo.prefWidthProperty().bind(raiz.widthProperty());
        fondo.prefHeightProperty().bind(raiz.heightProperty());
        raiz.getChildren().addAll(fondo, contenido);
        Scene escena = new Scene(raiz, 1366, 820);
        escena.getStylesheets().add(getClass().getResource("/estilo.css").toExternalForm());
        //Filtro de eventos: escucha TODOS los clics de mouse de la escena antes que nadie más, y si
        //cayeron sobre un botón (o un hijo de un botón), reproduce el sonido de clic
        escena.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (esBotonOHijoDeBoton(e.getTarget())) {
                GestorSonido.reproducir("Click.mp3");
            }
        });
        stage.setScene(escena);
        stage.setTitle("F1 Manager");
        stage.setMinWidth(1024);
        stage.setMinHeight(680);
        stage.setMaximized(true);
    }

    //Getter
    public Stage getStage() {
        return stage;
    }

    //Muestra la primera pantalla de la aplicación sin transición (no hay nada previo).
    public void mostrarInicial(Region vista) {
        contenido.getChildren().setAll(vista);
        historial.clear();
        historial.push(vista);
    }

    //Navega hacia una nueva pantalla, apilándola en el historial, con transición fade.
    public void navegarA(Region vista) {
        cambiarCon(vista, true);
    }

    //Reemplaza la pantalla actual sin apilar en el historial (por ejemplo, al cambiar de sub-sección).
    public void reemplazar(Region vista) {
        cambiarCon(vista, false);
    }

    //Vuelve a la pantalla anterior del historial, si existe.
    public void volver() {
        // Se ignora mientras haya una transición en curso: si no, clics rápidos
        // podían desapilar varias veces aunque solo la primera transición se
        // llegara a mostrar, desincronizando el historial de lo que se ve en
        // pantalla (y dejando "saltar" pantallas intermedias, como el menú
        // principal, en un "volver" posterior).
        if (animando || historial.size() <= 1) {
            return;
        }
        //Saca del historial la pantalla actual (ya la vamos a dejar) y mira cuál queda de tope (la anterior)
        historial.pop();
        Region anterior = historial.peek();
        cambiarConInterno(anterior, false);
    }

    //Método interno compartido por navegarA() y reemplazar(): decide si apilar o no la nueva
    //pantalla en el historial y delega el cambio visual real a cambiarConInterno().
    private void cambiarCon(Region vista, boolean apilar) {
        if (animando) {
            return;
        }
        if (apilar) {
            historial.push(vista);
        }
        cambiarConInterno(vista, false);
    }

       //Hace el cambio visual real entre la pantalla actual y la nueva, con una animación de fundido
       //(la actual se desvanece y, cuando termina, la nueva aparece encima ya desvaneciéndose hacia adentro).
       private void cambiarConInterno(Region vista, boolean ignorar) {
        if (animando) {
            return;
        }
        //La pantalla que se está mostrando ahora mismo (si el contenedor está vacío, es la primera vez)
        Node actual = contenido.getChildren().isEmpty() ? null : contenido.getChildren().get(0);
        GestorSonido.reproducir("Transicion de esena.m4a");

        //Si no había ninguna pantalla previa, simplemente se muestra la nueva apareciendo con fundido (sin nada que desvanecer antes)
        if (actual == null) {
            vista.setOpacity(0);
            contenido.getChildren().setAll(vista);
            FadeTransition entrada = new FadeTransition(DURACION_FADE, vista);
            entrada.setFromValue(0);
            entrada.setToValue(1);
            entrada.play();
            return;
        }

        //Se marca que hay una animación en curso para bloquear otros cambios de pantalla mientras dura
        animando = true;
        //Animación de salida: la pantalla actual pasa de opacidad 1 (visible) a 0 (invisible)
        FadeTransition salida = new FadeTransition(DURACION_FADE, actual);
        salida.setFromValue(1);
        salida.setToValue(0);

        //Animación de entrada: la pantalla nueva pasa de opacidad 0 a 1 (aparece)
        vista.setOpacity(0);
        FadeTransition entrada = new FadeTransition(DURACION_FADE, vista);
        entrada.setFromValue(0);
        entrada.setToValue(1);

        //Cuando termina de desvanecerse la pantalla vieja, recién ahí se reemplaza por la nueva y arranca su entrada
        salida.setOnFinished(e -> {
            contenido.getChildren().setAll(vista);
            entrada.play();
        });
        //Cuando termina de aparecer la pantalla nueva, se libera la bandera para permitir otro cambio de pantalla
        entrada.setOnFinished(e -> animando = false);

        //Encadena las dos animaciones (salida primero, entrada después) y las reproduce
        new SequentialTransition(salida).play();
    }

    // El click puede caer sobre un hijo interno del elemento clicable (su texto o ícono),
    // por eso se sube por los padres. Cualquier nodo con un manejador de click propio
    // (tarjetas de menú, íconos de la barra lateral, botón de volver) cuenta como "botón",
    // además de los controles Button/ButtonBase reales.
    private static boolean esBotonOHijoDeBoton(EventTarget objetivo) {
        //Convierte el objetivo del evento a Node (si no lo es, queda en null)
        Node nodo = objetivo instanceof Node ? (Node) objetivo : null;
        //Sube por la cadena de padres (getParent()) hasta encontrar un botón o quedarse sin padres
        while (nodo != null) {
            if (nodo instanceof ButtonBase || nodo.getOnMouseClicked() != null) {
                return true;
            }
            nodo = nodo.getParent();
        }
        return false;
    }

    //Cierra correctamente la aplicación.
    public void salir() {
        Platform.exit();
    }
}
