package com.f1manager.infraestructura.ui.screens.campeonato;

import com.f1manager.dominio.modelo.Campeonato;
import com.f1manager.dominio.modelo.Circuito;
import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.infraestructura.ui.screens.carrera.AnimacionCarreraPane;
import com.f1manager.infraestructura.ui.screens.carrera.ResultadosCarreraPane;
import com.f1manager.infraestructura.ui.screens.carrera.SeleccionCarreraPane;
import com.f1manager.infraestructura.ui.util.GestorEscenas;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Modo Campeonato: encadena todas las carreras del calendario una tras otra,
 * sumando puntos F1 reales después de cada una, hasta coronar campeón. Reutiliza
 * las mismas pantallas de la carrera suelta (selección, animación, resultados).
 */
public class PantallaCampeonato extends BorderPane {

    private final StackPane areaCentral = new StackPane();
    private final Campeonato campeonato;
    private boolean animando = false;

    public PantallaCampeonato(GestorEscenas gestor) {
        getStyleClass().add("pantalla");
        setPrefSize(1366, 820);
        setPadding(new Insets(10, 0, 14, 0));
        setTop(construirBarraSuperior(gestor));

        ScrollPane scrollCentral = new ScrollPane(areaCentral);
        scrollCentral.setFitToWidth(true);
        scrollCentral.getStyleClass().add("scroll-oscuro");
        scrollCentral.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scrollCentral);

        this.campeonato = new Campeonato(DataStore.getInstancia().getCircuitos(),
                DataStore.getInstancia().getPilotos(), DataStore.getInstancia().getEquipos());

        mostrarBienvenida(gestor);
    }

    private HBox construirBarraSuperior(GestorEscenas gestor) {
        StackPane botonVolver = IconFactory.contenedor(IconFactory.flechaVolver(IconFactory.BLANCO), 40);
        botonVolver.setStyle("-fx-background-color: transparent; -fx-border-color: #232a3d; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        botonVolver.setOnMouseClicked(e -> gestor.volver());

        Label titulo = new Label("CAMPEONATO");
        titulo.getStyleClass().add("titulo-seccion");

        HBox barra = new HBox(20, botonVolver, titulo);
        barra.getStyleClass().add("barra-superior");
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(18, 30, 18, 30));
        return barra;
    }

    private void mostrarBienvenida(GestorEscenas gestor) {
        cambiar(new BienvenidaCampeonatoPane(campeonato, () -> mostrarSeleccionClima(gestor)));
    }

    private void mostrarSeleccionClima(GestorEscenas gestor) {
        Circuito circuitoActual = campeonato.getCircuitoActual();
        SeleccionCarreraPane seleccion = new SeleccionCarreraPane((circuito, clima) ->
                cambiar(new AnimacionCarreraPane(circuito, clima, simulacion -> {
                    campeonato.registrarResultado(simulacion.getResultados());
                    cambiar(new ResultadosCarreraPane(circuito, simulacion,
                            "VER TABLA DE POSICIONES", () -> mostrarTabla(gestor),
                            "ABANDONAR CAMPEONATO", gestor::volver));
                })), circuitoActual);
        cambiar(seleccion);
    }

    private void mostrarTabla(GestorEscenas gestor) {
        cambiar(new TablaPosicionesPane(campeonato, () -> {
            if (campeonato.quedanCarreras()) {
                mostrarSeleccionClima(gestor);
            } else {
                mostrarCampeon(gestor);
            }
        }));
    }

    private void mostrarCampeon(GestorEscenas gestor) {
        cambiar(new CampeonPane(campeonato, gestor::volver));
    }

    private void cambiar(Node vista) {
        if (areaCentral.getChildren().isEmpty()) {
            areaCentral.getChildren().setAll(vista);
            return;
        }
        if (animando) {
            areaCentral.getChildren().setAll(vista);
            return;
        }
        animando = true;
        Node actual = areaCentral.getChildren().get(0);
        FadeTransition salida = new FadeTransition(Duration.millis(220), actual);
        salida.setFromValue(1);
        salida.setToValue(0);
        salida.setOnFinished(e -> {
            areaCentral.getChildren().setAll(vista);
            vista.setOpacity(0);
            FadeTransition entrada = new FadeTransition(Duration.millis(260), vista);
            entrada.setFromValue(0);
            entrada.setToValue(1);
            entrada.setOnFinished(ev -> animando = false);
            entrada.play();
        });
        salida.play();
    }
}
