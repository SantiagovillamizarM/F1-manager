//Esta es la pantalla que orquesta todo el Modo Campeonato: encadena todas las
//carreras del calendario una tras otra, sumando puntos F1 reales despues de cada
//una, hasta coronar campeon. Reutiliza las mismas pantallas de la carrera suelta
//(seleccion, animacion, resultados), solo que aca se encargan de avisarle a esta
//clase cuando termino algo para poder pasar a la siguiente pantalla.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.campeonato;

//Trae la clase Campeonato, que lleva el calendario, el clima y los puntos de toda la temporada
import com.f1manager.dominio.modelo.Campeonato;
//Trae la clase Circuito para saber en que circuito se corre la carrera actual
import com.f1manager.dominio.modelo.Circuito;
//Trae la clase Clima para poder sortear/leer el clima dinamico de cada carrera
import com.f1manager.dominio.modelo.Clima;
//Trae el DataStore, que guarda toda la informacion (circuitos, pilotos, equipos) cargada en la app
import com.f1manager.infraestructura.persistencia.DataStore;
//Trae la pantalla que dibuja la animacion de la carrera sobre el trazado
import com.f1manager.infraestructura.ui.screens.carrera.AnimacionCarreraPane;
//Trae la pantalla que muestra los resultados finales de una carrera
import com.f1manager.infraestructura.ui.screens.carrera.ResultadosCarreraPane;
//Trae la pantalla donde se elige el circuito y el clima antes de correr
import com.f1manager.infraestructura.ui.screens.carrera.SeleccionCarreraPane;
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
//Trae ScrollPane, un contenedor que agrega una barra de desplazamiento (scroll) cuando el contenido no cabe
import javafx.scene.control.ScrollPane;
//Trae BorderPane, un contenedor que divide la pantalla en zonas (arriba, abajo, centro, izquierda, derecha)
import javafx.scene.layout.BorderPane;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila)
import javafx.scene.layout.HBox;
//Trae StackPane, un contenedor que apila elementos uno encima del otro
import javafx.scene.layout.StackPane;
//Trae Duration, que sirve para indicar cuanto dura una animacion (en milisegundos)
import javafx.util.Duration;

//Esta es la clase publica "PantallaCampeonato" que extiende de BorderPane (osea que ES un BorderPane, dividido en zonas)
public class PantallaCampeonato extends BorderPane {

    //Zona central donde se van intercambiando las distintas vistas (bienvenida, seleccion, animacion, resultados, tabla, campeon)
    private final StackPane areaCentral = new StackPane();
    //El campeonato de esta partida (calendario, puntos, clima, etc), se crea una sola vez en el constructor
    private final Campeonato campeonato;
    //Bandera que indica si ya hay una animacion de cambio de vista corriendo, para no encimar dos animaciones a la vez
    private boolean animando = false;

    //Constructor
    //Arma toda la pantalla del campeonato y arranca mostrando la bienvenida
    public PantallaCampeonato(GestorEscenas gestor) {
        getStyleClass().add("pantalla");
        setPrefSize(1366, 820);
        setPadding(new Insets(10, 0, 14, 0));
        //Pone la barra superior (con el boton de volver y el titulo) en la zona de arriba del BorderPane
        setTop(construirBarraSuperior(gestor));

        //Envuelve la zona central en un ScrollPane por si el contenido de alguna vista no cabe en la pantalla
        ScrollPane scrollCentral = new ScrollPane(areaCentral);
        scrollCentral.setFitToWidth(true);
        scrollCentral.getStyleClass().add("scroll-oscuro");
        scrollCentral.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scrollCentral);

        //Crea el campeonato nuevo tomando el calendario de circuitos, los pilotos y los equipos guardados en el DataStore
        this.campeonato = new Campeonato(DataStore.getInstancia().getCircuitos(),
                DataStore.getInstancia().getPilotos(), DataStore.getInstancia().getEquipos());

        //Muestra la primera pantalla del flujo: la bienvenida con el calendario completo
        mostrarBienvenida(gestor);
    }

    //Este metodo privado arma la barra de arriba con el boton de volver y el titulo "CAMPEONATO"
    private HBox construirBarraSuperior(GestorEscenas gestor) {
        //Boton con forma de flecha para volver al menu anterior
        StackPane botonVolver = IconFactory.contenedor(IconFactory.flechaVolver(IconFactory.BLANCO), 40);
        botonVolver.setStyle("-fx-background-color: transparent; -fx-border-color: #232a3d; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        //Cuando se hace click en la flecha, le pide al gestor de escenas que vuelva a la pantalla anterior
        botonVolver.setOnMouseClicked(e -> gestor.volver());

        //Texto con el titulo de la seccion
        Label titulo = new Label("CAMPEONATO");
        titulo.getStyleClass().add("titulo-seccion");

        //Fila que junta el boton de volver y el titulo
        HBox barra = new HBox(20, botonVolver, titulo);
        barra.getStyleClass().add("barra-superior");
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(18, 30, 18, 30));
        return barra;
    }

    //Muestra la pantalla de bienvenida (con el calendario) y le dice que, al darle "comenzar", pase a elegir el clima
    private void mostrarBienvenida(GestorEscenas gestor) {
        cambiar(new BienvenidaCampeonatoPane(campeonato, () -> mostrarSeleccionClima(gestor)));
    }

    //Muestra la pantalla de seleccion de circuito/clima para la carrera actual del calendario
    private void mostrarSeleccionClima(GestorEscenas gestor) {
        //Trae el circuito que le toca correr ahora segun el indice interno del campeonato
        Circuito circuitoActual = campeonato.getCircuitoActual();
        // En Campeonato el clima no lo elige el jugador: se decide solo por carrera (clima
        // dinámico, ponderado según el país del circuito). En Carrera suelta (PantallaCarrera)
        // sigue siendo 100% elegible por el jugador, sin tocar ese flujo.
        //Pide (o reutiliza si ya se habia sorteado) el clima dinamico ya resuelto para esta carrera
        Clima climaDinamico = campeonato.getClimaActual();
        //Arma la pantalla de seleccion, y le dice que hacer cuando el jugador confirme el circuito y clima:
        //arrancar la animacion de la carrera y, cuando esta termine, registrar el resultado y mostrar los resultados
        SeleccionCarreraPane seleccion = new SeleccionCarreraPane((circuito, clima) ->
                cambiar(new AnimacionCarreraPane(circuito, clima, simulacion -> {
                    //Le suma al campeonato los puntos de esta carrera (actualiza las tablas de pilotos y equipos)
                    campeonato.registrarResultado(simulacion.getResultados());
                    //Muestra los resultados de la carrera con dos botones: uno para ver la tabla de posiciones
                    //y otro para abandonar el campeonato y volver al menu
                    cambiar(new ResultadosCarreraPane(circuito, simulacion,
                            "VER TABLA DE POSICIONES", () -> mostrarTabla(gestor),
                            "ABANDONAR CAMPEONATO", gestor::volver));
                })), circuitoActual, climaDinamico);
        cambiar(seleccion);
    }

    //Muestra la tabla de posiciones actual y decide que sigue cuando el jugador le da "continuar"
    private void mostrarTabla(GestorEscenas gestor) {
        cambiar(new TablaPosicionesPane(campeonato, () -> {
            //Si todavia quedan carreras en el calendario, vuelve a mostrar la seleccion de la siguiente carrera
            if (campeonato.quedanCarreras()) {
                mostrarSeleccionClima(gestor);
            } else {
                //Si ya no quedan carreras, la temporada termino: muestra la pantalla del campeon
                mostrarCampeon(gestor);
            }
        }));
    }

    //Muestra la pantalla final que corona al campeon de pilotos y de equipos
    private void mostrarCampeon(GestorEscenas gestor) {
        cambiar(new CampeonPane(campeonato, gestor::volver));
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
