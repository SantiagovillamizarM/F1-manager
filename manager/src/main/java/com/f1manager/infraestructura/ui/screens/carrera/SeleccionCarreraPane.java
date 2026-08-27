package com.f1manager.infraestructura.ui.screens.carrera;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.modelo.Circuito;
import com.f1manager.dominio.modelo.Clima;
import com.f1manager.infraestructura.ui.util.PistaGenerador;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Pantalla de selección previa a la carrera: elegir circuito (izquierda),
 * ver su representación visual en el centro y elegir las condiciones
 * climáticas antes de presionar "EMPEZAR CARRERA".
 */
public class SeleccionCarreraPane extends BorderPane {

    private final VBox columnaCircuitos = new VBox(12);
    private final VBox panelCentro = new VBox(20);
    private final Canvas lienzoPista = new Canvas(560, 340);
    private final Label nombreCircuitoLabel = new Label();
    private final Label descripcionCircuitoLabel = new Label();
    private final Button botonEmpezar = new Button("EMPEZAR CARRERA");

    private final Map<Clima, VBox> tarjetasClima = new EnumMap<>(Clima.class);
    private Circuito circuitoSeleccionado;
    private Clima climaSeleccionado;
    private VBox filaCircuitoSeleccionada;

    public SeleccionCarreraPane(BiConsumer<Circuito, Clima> alEmpezar) {
        setPadding(new Insets(10));

        // ---- Izquierda: lista de circuitos ----
        Label tituloCircuitos = new Label("Circuitos disponibles");
        tituloCircuitos.getStyleClass().add("titulo-seccion");

        ScrollPane scroll = new ScrollPane(columnaCircuitos);
        scroll.setFitToWidth(true);
        scroll.setPrefWidth(360);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        for (Circuito c : DataStore.getInstancia().getCircuitos()) {
            columnaCircuitos.getChildren().add(construirFilaCircuito(c));
        }

        VBox columnaIzquierda = new VBox(16, tituloCircuitos, scroll);
        columnaIzquierda.setPadding(new Insets(0, 20, 0, 0));
        columnaIzquierda.setPrefWidth(380);
        setLeft(columnaIzquierda);

        // ---- Centro: pista + clima + botón ----
        nombreCircuitoLabel.getStyleClass().add("titulo-seccion");
        descripcionCircuitoLabel.getStyleClass().add("texto-secundario");
        descripcionCircuitoLabel.setWrapText(true);
        mostrarSinCircuito();

        StackPane contenedorLienzo = new StackPane(lienzoPista);
        contenedorLienzo.getStyleClass().add("panel");
        contenedorLienzo.setPadding(new Insets(14));

        Label tituloClima = new Label("Seleccione las condiciones climáticas");
        tituloClima.getStyleClass().add("titulo-seccion");

        HBox filaClima = new HBox(16);
        filaClima.setAlignment(Pos.CENTER);
        for (Clima clima : Clima.values()) {
            VBox tarjeta = construirTarjetaClima(clima);
            tarjetasClima.put(clima, tarjeta);
            filaClima.getChildren().add(tarjeta);
        }

        botonEmpezar.getStyleClass().add("boton-grande");
        botonEmpezar.setDisable(true);
        botonEmpezar.setOnAction(e -> {
            if (circuitoSeleccionado != null && climaSeleccionado != null) {
                alEmpezar.accept(circuitoSeleccionado, climaSeleccionado);
            }
        });

        VBox cajaBoton = new VBox(botonEmpezar);
        cajaBoton.setAlignment(Pos.CENTER);
        cajaBoton.setPadding(new Insets(10, 0, 0, 0));

        panelCentro.setAlignment(Pos.TOP_CENTER);
        panelCentro.getChildren().addAll(nombreCircuitoLabel, descripcionCircuitoLabel, contenedorLienzo,
                tituloClima, filaClima, cajaBoton);
        panelCentro.setPadding(new Insets(0, 0, 0, 10));
        setCenter(panelCentro);
    }

    private VBox construirFilaCircuito(Circuito circuito) {
        Label nombre = new Label(circuito.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5f6fa;");
        Label detalle = new Label(circuito.getPais() + "  ·  " + circuito.getVueltas() + " vueltas");
        detalle.getStyleClass().add("texto-secundario");

        VBox fila = new VBox(4, nombre, detalle);
        fila.getStyleClass().add("fila-lista");
        fila.setOnMouseClicked(e -> seleccionarCircuito(circuito, fila));
        return fila;
    }

    private void seleccionarCircuito(Circuito circuito, VBox fila) {
        if (filaCircuitoSeleccionada != null) {
            filaCircuitoSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
            filaCircuitoSeleccionada.getStyleClass().add("fila-lista");
        }
        fila.getStyleClass().remove("fila-lista");
        fila.getStyleClass().add("fila-lista-seleccionada");
        filaCircuitoSeleccionada = fila;

        circuitoSeleccionado = circuito;
        nombreCircuitoLabel.setText(circuito.getNombre() + " — " + circuito.getPais());
        descripcionCircuitoLabel.setText(circuito.getDescripcion());

        GraphicsContext gc = lienzoPista.getGraphicsContext2D();
        gc.clearRect(0, 0, lienzoPista.getWidth(), lienzoPista.getHeight());
        PistaGenerador.paraCircuito(circuito).dibujar(gc, 0, 0, lienzoPista.getWidth(), lienzoPista.getHeight(), true);

        actualizarBotonEmpezar();
    }

    private void mostrarSinCircuito() {
        nombreCircuitoLabel.setText("Selecciona un circuito");
        descripcionCircuitoLabel.setText("Elige un circuito de la lista para ver su trazado antes de comenzar la carrera.");
    }

    private VBox construirTarjetaClima(Clima clima) {
        Label etiqueta = new Label(clima.getEtiqueta());
        etiqueta.getStyleClass().add("texto-normal");

        VBox tarjeta = new VBox(etiqueta);
        tarjeta.getStyleClass().add("opcion-clima");
        tarjeta.setPrefWidth(130);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setOnMouseClicked(e -> seleccionarClima(clima));
        return tarjeta;
    }

    private void seleccionarClima(Clima clima) {
        climaSeleccionado = clima;
        for (var entrada : tarjetasClima.entrySet()) {
            entrada.getValue().getStyleClass().setAll(entrada.getKey() == clima ? "opcion-clima-seleccionada" : "opcion-clima");
        }
        actualizarBotonEmpezar();
    }

    private void actualizarBotonEmpezar() {
        botonEmpezar.setDisable(circuitoSeleccionado == null || climaSeleccionado == null);
    }
}
