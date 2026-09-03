//Muestra la pantalla de bienvenida al arrancar el programa: empieza todo oscuro y el logo, el
//título y el subtítulo van apareciendo de a poco con fundidos (fade), como la intro de un
//videojuego. Al presionar cualquier tecla (o hacer click) se pasa con otro fade al menú principal.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui;

//Trae el gestor de escenas, que es el que se encarga de cambiar de una pantalla a otra
import com.f1manager.infraestructura.ui.util.GestorEscenas;
//Trae el gestor de sonido, que controla la música de fondo y los efectos de audio
import com.f1manager.infraestructura.ui.util.GestorSonido;
//Trae la fábrica de íconos, que es la que arma el logo
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae la animación de fundido (fade), la que hace que algo aparezca o desaparezca suavemente
import javafx.animation.FadeTransition;
//Trae KeyFrame, que es un "fotograma clave": define qué pasa en un momento puntual de una animación tipo Timeline
import javafx.animation.KeyFrame;
//Trae PauseTransition, una animación que no hace nada visual, solo espera un tiempo y avisa cuando termina
import javafx.animation.PauseTransition;
//Trae SequentialTransition, que ejecuta varias animaciones una detrás de la otra (en secuencia)
import javafx.animation.SequentialTransition;
//Trae Timeline, que repite una acción cada cierto tiempo (se usa para los puntos de "Cargando...")
import javafx.animation.Timeline;
//Trae Pos, que sirve para alinear elementos (por ejemplo al centro)
import javafx.geometry.Pos;
//Trae la etiqueta de texto (Label) de JavaFX
import javafx.scene.control.Label;
//Trae el StackPane, un layout que apila los elementos uno encima de otro
import javafx.scene.layout.StackPane;
//Trae la caja vertical (VBox) de JavaFX que acomoda los elementos uno debajo del otro
import javafx.scene.layout.VBox;
//Trae Duration, que sirve para indicar cuánto dura una animación o una espera
import javafx.util.Duration;

//Clase pública llamada "PantallaBienvenida" que hereda de StackPane (un layout que apila los elementos uno encima de otro)
public class PantallaBienvenida extends StackPane {

    //Bandera privada que evita avanzar dos veces al menú principal (por ejemplo si se hace click y se aprieta una tecla casi al mismo tiempo)
    private boolean avanzando = false;
    //Bandera privada que indica si ya terminó la espera inicial y por lo tanto ya se puede avanzar al menú
    private boolean puedeAvanzar = false;

    //Constructor
    //Arma toda la pantalla de bienvenida: el logo, el título, el subtítulo, la secuencia de fundidos de aparición,
    //la animación de "Cargando..." y los listeners para avanzar al menú principal con una tecla o un click.
    public PantallaBienvenida(GestorEscenas gestor) {
        getStyleClass().add("pantalla");
        setPrefSize(1366, 820);

        var logo = IconFactory.logo(2.0);

        //Título que dice "Bienvenido al F1 Manager", arranca invisible (opacidad 0) para poder aparecer con fade
        Label titulo = new Label("Bienvenido al F1 Manager");
        titulo.getStyleClass().add("titulo-principal");
        titulo.setOpacity(0);

        //Subtítulo que arranca diciendo "Cargando", también invisible al principio
        Label subtitulo = new Label("Cargando");
        subtitulo.getStyleClass().add("subtitulo");
        subtitulo.setOpacity(0);

        //Junta el logo, el título y el subtítulo en una columna vertical (VBox) centrada
        VBox caja = new VBox(26, logo, titulo, subtitulo);
        caja.setAlignment(Pos.CENTER);
        logo.setOpacity(0);

        getChildren().add(caja);
        setOpacity(0);

        // --- Secuencia de aparición progresiva ---
        //Fade de toda la pantalla, de invisible a visible
        FadeTransition fadePantalla = new FadeTransition(Duration.millis(600), this);
        fadePantalla.setFromValue(0);
        fadePantalla.setToValue(1);

        //Fade del logo, de invisible a visible
        FadeTransition fadeLogo = new FadeTransition(Duration.millis(900), logo);
        fadeLogo.setFromValue(0);
        fadeLogo.setToValue(1);

        //Fade del título, de invisible a visible
        FadeTransition fadeTitulo = new FadeTransition(Duration.millis(700), titulo);
        fadeTitulo.setFromValue(0);
        fadeTitulo.setToValue(1);

        //Fade del subtítulo, de invisible a visible
        FadeTransition fadeSubtitulo = new FadeTransition(Duration.millis(700), subtitulo);
        fadeSubtitulo.setFromValue(0);
        fadeSubtitulo.setToValue(1);

        //SequentialTransition hace que estos 4 fundidos ocurran uno detrás del otro (no todos a la vez)
        new SequentialTransition(fadePantalla, fadeLogo, fadeTitulo, fadeSubtitulo).play();

        // Solo suena una vez: esta pantalla solo se construye una vez, al arrancar la aplicación.
        GestorSonido.reproducir("Intro audio.m4a");

        // "Cargando..." con los puntos escribiéndose mientras suena la intro, para que la espera no se vea rara.
        //Array de un solo elemento como truco para poder modificar un número desde dentro de la lambda de abajo (una variable normal no se podría reasignar ahí)
        int[] puntos = {0};
        //Timeline que cada 400ms le suma un punto al subtítulo (y vuelve a 0 después del punto 3), simulando "Cargando.", "Cargando..", etc
        Timeline animacionCarga = new Timeline(new KeyFrame(Duration.millis(400), e -> {
            puntos[0] = (puntos[0] + 1) % 4;
            subtitulo.setText("Cargando" + ".".repeat(puntos[0]));
        }));
        //INDEFINITE hace que la animación se repita para siempre (hasta que se llame stop())
        animacionCarga.setCycleCount(Timeline.INDEFINITE);
        animacionCarga.play();

        // Al terminar la carga (4.5s), se cambia el mensaje y recién ahí se puede avanzar.
        PauseTransition esperaParaAvanzar = new PauseTransition(Duration.seconds(4.5));
        esperaParaAvanzar.setOnFinished(e -> {
            animacionCarga.stop();
            subtitulo.setText("Presiona cualquier tecla para continuar");
            puedeAvanzar = true;
        });
        esperaParaAvanzar.play();

        // --- Avanzar al menú principal con cualquier tecla ---
        //Escucha cuando esta pantalla ya tiene una Scene asignada (no se puede escuchar teclas antes de eso) y ahí
        //le pone el evento de tecla presionada a esa escena para poder avanzar
        sceneProperty().addListener((obs, anterior, nuevaEscena) -> {
            if (nuevaEscena != null) {
                nuevaEscena.setOnKeyPressed(e -> avanzar(gestor));
            }
        });
        //También se puede avanzar haciendo click en cualquier parte de la pantalla
        setOnMouseClicked(e -> avanzar(gestor));
        setFocusTraversable(true);
    }

    //Pasa al menú principal: si todavía no se puede avanzar (no terminó la espera) o ya se está avanzando, no hace nada.
    //Si se puede, arranca la música de fondo y le pide al gestor de escenas que navegue al menú principal.
    private void avanzar(GestorEscenas gestor) {
        if (!puedeAvanzar || avanzando) {
            return;
        }
        avanzando = true;
        GestorSonido.reproducirMusicaDeFondo("musica de fondo.m4a");
        gestor.navegarA(new MenuPrincipal(gestor));
    }
}
