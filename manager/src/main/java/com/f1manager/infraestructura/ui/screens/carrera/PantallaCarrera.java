//Esta es la pantalla de Carrera suelta (la seccion visualmente mas importante de
//la aplicacion). Orquesta tres estados con una transicion de fundido entre ellos:
//1) Seleccion de circuito y clima. 2) Animacion de la carrera sobre el trazado.
//3) Resultados finales (clasificacion).

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.carrera;

//Trae el GestorEscenas, que se encarga de cambiar entre las pantallas grandes de la app (menu, campeonato, carrera, etc)
import com.f1manager.infraestructura.ui.util.GestorEscenas;
//Trae la fabrica de iconos, usada para dibujar la flecha de "volver"
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae FadeTransition, una animacion que hace que un elemento aparezca o desaparezca suavemente (fundido)
import javafx.animation.FadeTransition;
//Trae Insets, que sirve para poner margenes/espacios alrededor de los elementos
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear los elementos (centrado, arriba, izquierda, etc)
import javafx.geometry.Pos;
//Trae Node, la clase base de todo lo que se puede dibujar en pantalla en JavaFX (sirve para recibir "cualquier vista")
import javafx.scene.Node;
//Trae Label, que es un texto que se muestra en pantalla (no se puede editar)
import javafx.scene.control.Label;
//Trae BorderPane, un contenedor que divide la pantalla en zonas (arriba, abajo, centro, izquierda, derecha)
import javafx.scene.layout.BorderPane;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila)
import javafx.scene.layout.HBox;
//Trae StackPane, un contenedor que apila elementos uno encima del otro
import javafx.scene.layout.StackPane;
//Trae Duration, que sirve para indicar cuanto dura una animacion (en milisegundos)
import javafx.util.Duration;

//Esta es la clase publica "PantallaCarrera" que extiende de BorderPane (osea que ES un BorderPane, dividido en zonas)
public class PantallaCarrera extends BorderPane {

    //Zona central donde se van intercambiando las tres vistas (seleccion, animacion, resultados)
    private final StackPane areaCentral = new StackPane();
    //Bandera que indica si ya hay una animacion de cambio de vista corriendo, para no encimar dos animaciones a la vez
    private boolean animando = false;

    //Constructor
    //Arma toda la pantalla de carrera suelta y arranca mostrando la seleccion de circuito y clima
    public PantallaCarrera(GestorEscenas gestor) {
        getStyleClass().add("pantalla");
        setPrefSize(1366, 820);
        // Deja aire arriba y abajo: sin este padding, la barra superior y el
        // contenido de cada estado (selección/animación/resultados) quedan
        // pegados directamente a los bordes de la ventana.
        setPadding(new Insets(6, 0, 8, 0));
        //Pone la barra superior (con el boton de volver y el titulo) en la zona de arriba del BorderPane
        setTop(construirBarraSuperior(gestor));

        //Pone la zona central directamente en el centro del BorderPane (sin scroll, a diferencia de Campeonato)
        setCenter(areaCentral);

        //Muestra la primera vista del flujo: la seleccion de circuito y clima
        mostrarSeleccion(gestor);
    }

    //Este metodo privado arma la barra de arriba con el boton de volver y el titulo "CARRERA"
    private HBox construirBarraSuperior(GestorEscenas gestor) {
        //Boton con forma de flecha para volver al menu anterior
        StackPane botonVolver = IconFactory.contenedor(IconFactory.flechaVolver(IconFactory.BLANCO), 40);
        botonVolver.setStyle("-fx-background-color: transparent; -fx-border-color: #232a3d; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        //Cuando se hace click en la flecha, le pide al gestor de escenas que vuelva a la pantalla anterior
        botonVolver.setOnMouseClicked(e -> gestor.volver());

        //Texto con el titulo de la seccion
        Label titulo = new Label("CARRERA");
        titulo.getStyleClass().add("titulo-seccion");

        //Fila que junta el boton de volver y el titulo
        HBox barra = new HBox(20, botonVolver, titulo);
        barra.getStyleClass().add("barra-superior");
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(10, 30, 10, 30));
        return barra;
    }

    //Muestra la pantalla de seleccion de circuito y clima, y encadena lo que sigue cuando el jugador confirma:
    //arranca la animacion de la carrera y, cuando esta termina, muestra los resultados
    private void mostrarSeleccion(GestorEscenas gestor) {
        SeleccionCarreraPane seleccion = new SeleccionCarreraPane((circuito, clima) ->
                cambiar(new AnimacionCarreraPane(circuito, clima, simulacion ->
                        //Al terminar la carrera, muestra los resultados con "NUEVA CARRERA" (vuelve a la seleccion)
                        //y "MENU PRINCIPAL" (vuelve al menu) como botones por defecto
                        cambiar(new ResultadosCarreraPane(circuito, simulacion,
                                () -> mostrarSeleccion(gestor), gestor::volver)))));
        cambiar(seleccion);
    }

    //Este metodo privado se encarga de cambiar la vista que se ve en la zona central, con una animacion de fundido
    //(fade out de la vista vieja y fade in de la vista nueva) para que no se vea un cambio brusco
    private void cambiar(Node vista) {
        //Si todavia no hay ninguna vista puesta (primera vez), la pone directo sin animar
        if (areaCentral.getChildren().isEmpty()) {
            areaCentral.getChildren().setAll(vista);
            return;
        }
        //Si ya hay una animacion en curso, cambia directo sin animar (para no encimar animaciones)
        if (animando) {
            areaCentral.getChildren().setAll(vista);
            return;
        }
        animando = true;
        //Guarda la vista que esta actualmente en pantalla, para desvanecerla
        Node actual = areaCentral.getChildren().get(0);
        //Animacion de salida: la vista actual pasa de opacidad 1 (visible) a 0 (invisible) en 220 milisegundos
        FadeTransition salida = new FadeTransition(Duration.millis(220), actual);
        salida.setFromValue(1);
        salida.setToValue(0);
        //Cuando termina de desaparecer la vista vieja...
        salida.setOnFinished(e -> {
            //Pone la vista nueva en su lugar
            areaCentral.getChildren().setAll(vista);
            //La deja invisible para poder animarla apareciendo
            vista.setOpacity(0);
            //Animacion de entrada: la vista nueva pasa de opacidad 0 (invisible) a 1 (visible) en 260 milisegundos
            FadeTransition entrada = new FadeTransition(Duration.millis(260), vista);
            entrada.setFromValue(0);
            entrada.setToValue(1);
            //Cuando termina de aparecer, baja la bandera para permitir la siguiente animacion
            entrada.setOnFinished(ev -> animando = false);
            entrada.play();
        });
        salida.play();
    }
}
