package com.f1manager.infraestructura.ui.screens.carrera;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.excepcion.ValidacionException;
import com.f1manager.dominio.modelo.Circuito;
import com.f1manager.dominio.modelo.Clima;
import com.f1manager.infraestructura.ui.util.GestorSonido;
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
    private final Label mensajeError = new Label();

    private final Map<Clima, VBox> tarjetasClima = new EnumMap<>(Clima.class);
    private Circuito circuitoSeleccionado;
    private Clima climaSeleccionado;
    private VBox filaCircuitoSeleccionada;

    public SeleccionCarreraPane(BiConsumer<Circuito, Clima> alEmpezar) {
        this(alEmpezar, null, null);
    }

    /**
     * @param circuitoFijo si no es null (modo campeonato: la fecha del calendario ya determina
     *                     el circuito), se omite la lista de selección y queda preseleccionado.
     */
    public SeleccionCarreraPane(BiConsumer<Circuito, Clima> alEmpezar, Circuito circuitoFijo) {
        this(alEmpezar, circuitoFijo, null);
    }

    /**
     * @param circuitoFijo si no es null (modo campeonato: la fecha del calendario ya determina
     *                     el circuito), se omite la lista de selección y queda preseleccionado.
     * @param climaFijo si no es null (modo campeonato: el clima de esa fecha ya se decidió de
     *                  forma dinámica), se omiten las tarjetas de clima elegibles y se muestra
     *                  solo un pronóstico informativo, ya preseleccionado.
     */
    public SeleccionCarreraPane(BiConsumer<Circuito, Clima> alEmpezar, Circuito circuitoFijo, Clima climaFijo) {
        setPadding(new Insets(10));

        if (circuitoFijo == null) {
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
        }

        // ---- Centro: pista + clima + botón ----
        nombreCircuitoLabel.getStyleClass().add("titulo-seccion");
        descripcionCircuitoLabel.getStyleClass().add("texto-secundario");
        descripcionCircuitoLabel.setWrapText(true);
        mostrarSinCircuito();

        StackPane contenedorLienzo = new StackPane(lienzoPista);
        contenedorLienzo.getStyleClass().add("panel");
        contenedorLienzo.setPadding(new Insets(14));

        Label tituloClima = new Label(climaFijo == null
                ? "Seleccione las condiciones climáticas" : "Pronóstico del clima para esta carrera");
        tituloClima.getStyleClass().add("titulo-seccion");

        HBox filaClima = new HBox(16);
        filaClima.setAlignment(Pos.CENTER);
        if (climaFijo == null) {
            for (Clima clima : Clima.values()) {
                VBox tarjeta = construirTarjetaClima(clima);
                tarjetasClima.put(clima, tarjeta);
                filaClima.getChildren().add(tarjeta);
            }
        } else {
            filaClima.getChildren().add(construirTarjetaClimaFija(climaFijo));
            climaSeleccionado = climaFijo;
        }

        botonEmpezar.getStyleClass().add("boton-grande");
        botonEmpezar.setDisable(true);
        botonEmpezar.setOnAction(e -> {
            if (circuitoSeleccionado != null && climaSeleccionado != null) {
                try {
                    // Si algún equipo con pilotos no tiene vehículo, esa carrera lo simularía con
                    // velocidad 0 y sin neumático — se corta aquí en vez de dejarlo llegar así.
                    DataStore.getInstancia().validarEquiposListosParaCarrera();
                    mensajeError.setText("");
                    alEmpezar.accept(circuitoSeleccionado, climaSeleccionado);
                } catch (ValidacionException ex) {
                    mensajeError.setText(ex.getMessage());
                    GestorSonido.reproducir("Error sound.mp3");
                }
            }
        });

        mensajeError.getStyleClass().add("error-label");
        mensajeError.setWrapText(true);
        mensajeError.setMaxWidth(420);

        VBox cajaBoton = new VBox(10, botonEmpezar, mensajeError);
        cajaBoton.setAlignment(Pos.CENTER);
        cajaBoton.setPadding(new Insets(10, 0, 0, 0));

        panelCentro.setAlignment(Pos.TOP_CENTER);
        panelCentro.getChildren().addAll(nombreCircuitoLabel, descripcionCircuitoLabel, contenedorLienzo,
                tituloClima, filaClima, cajaBoton);
        panelCentro.setPadding(new Insets(0, 0, 0, 10));

        // Si la ventana es pequeña y el contenido no cabe completo, se puede hacer
        // scroll en vez de recortar el botón "EMPEZAR CARRERA" fuera de la vista.
        ScrollPane scrollCentro = new ScrollPane(panelCentro);
        scrollCentro.setFitToWidth(true);
        scrollCentro.getStyleClass().add("scroll-oscuro");
        scrollCentro.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scrollCentro);

        if (circuitoFijo != null) {
            seleccionarCircuito(circuitoFijo, null);
        }
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
        // fila es null en modo campeonato (circuito fijo, sin lista para elegir).
        if (fila != null) {
            if (filaCircuitoSeleccionada != null) {
                filaCircuitoSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
                filaCircuitoSeleccionada.getStyleClass().add("fila-lista");
            }
            fila.getStyleClass().remove("fila-lista");
            fila.getStyleClass().add("fila-lista-seleccionada");
            filaCircuitoSeleccionada = fila;
        }

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

    /** Tarjeta de clima informativa (no clickeable), usada en modo campeonato cuando el clima ya se decidió solo. */
    private VBox construirTarjetaClimaFija(Clima clima) {
        Label etiqueta = new Label(clima.getEtiqueta());
        etiqueta.getStyleClass().add("texto-normal");

        VBox tarjeta = new VBox(etiqueta);
        tarjeta.getStyleClass().add("opcion-clima-seleccionada");
        tarjeta.setPrefWidth(130);
        tarjeta.setAlignment(Pos.CENTER);
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
