package com.f1manager.infraestructura.ui.screens.vehiculos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.modelo.Monoplaza;
import com.f1manager.dominio.modelo.Piloto;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sub-vista "Listar Monoplazas": muestra los vehículos registrados con un
 * ícono representativo (no se depende de imágenes externas para funcionar)
 * y, al seleccionar uno, su información técnica ampliada.
 */
public class VehiculosListarPane extends HBox {

    private final VBox columnaLista = new VBox(12);
    private final StackPane panelDetalle = new StackPane();
    private VBox filaSeleccionada;

    public VehiculosListarPane() {
        setSpacing(28);

        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefWidth(480);
        scroll.setPrefHeight(560);

        panelDetalle.getStyleClass().add("panel");
        panelDetalle.setPrefSize(560, 560);
        panelDetalle.setPadding(new Insets(28));
        mostrarMensajeVacio();

        HBox.setHgrow(panelDetalle, Priority.ALWAYS);
        getChildren().addAll(scroll, panelDetalle);

        for (Monoplaza m : DataStore.getInstancia().getVehiculos()) {
            columnaLista.getChildren().add(construirFila(m));
        }
    }

    private VBox construirFila(Monoplaza m) {
       StackPane icono = IconFactory.contenedor(IconFactory.monoplaza(IconFactory.BLANCO), 70, 40);

        Label nombre = new Label(m.getModelo());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5f6fa;");

        Label detalle = new Label(m.getEquipo() + "  ·  " + m.getMotor());
        detalle.getStyleClass().add("texto-secundario");

        VBox textos = new VBox(4, nombre, detalle);
        HBox fila = new HBox(14, icono, textos);
        fila.setAlignment(Pos.CENTER_LEFT);

        VBox contenedorFila = new VBox(fila);
        contenedorFila.getStyleClass().add("fila-lista");
        contenedorFila.setOnMouseClicked(e -> seleccionar(m, contenedorFila));
        return contenedorFila;
    }

    private void seleccionar(Monoplaza m, VBox fila) {
        if (filaSeleccionada != null) {
            filaSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
            filaSeleccionada.getStyleClass().add("fila-lista");
        }
        fila.getStyleClass().remove("fila-lista");
        fila.getStyleClass().add("fila-lista-seleccionada");
        filaSeleccionada = fila;
        mostrarDetalle(m);
    }

    private void mostrarMensajeVacio() {
        Label mensaje = new Label("Selecciona un monoplaza de la lista\npara ver su información técnica.");
        mensaje.getStyleClass().add("texto-secundario");
        mensaje.setStyle("-fx-text-alignment: center;");
        panelDetalle.getChildren().setAll(mensaje);
    }

    private void mostrarDetalle(Monoplaza m) {
        StackPane icono = IconFactory.contenedor(IconFactory.monoplaza(IconFactory.BLANCO), 90);

        Label titulo = new Label(m.getModelo());
        titulo.getStyleClass().add("titulo-seccion");

        Label equipo = new Label(m.getEquipo());
        equipo.getStyleClass().add("texto-rojo");

        HBox encabezado = new HBox(20, icono, new VBox(6, titulo, equipo));
        encabezado.setAlignment(Pos.CENTER_LEFT);

        Label motor = new Label("Motor: " + m.getMotor());
        Label velocidad = new Label(String.format("Velocidad máxima: %.0f km/h", m.getVelocidadMaxKmh()));
        Label aceleracion = new Label(String.format("Aceleración 0-100 km/h: %.1f s", m.getAceleracion0a100()));
        Label carga = new Label("Carga aerodinámica actual: " + m.getCargaAerodinamica().getEtiqueta());
        Label modo = new Label("Modo de conducción actual: " + m.getModoConduccion().getEtiqueta());
        for (Label l : List.of(motor, velocidad, aceleracion, carga, modo)) {
            l.getStyleClass().add("texto-normal");
        }

        List<Piloto> pilotosAsociados = DataStore.getInstancia().getPilotosPorEquipo(m.getEquipo());
        String textoPilotos = pilotosAsociados.isEmpty() ? "Sin pilotos asignados actualmente."
                : pilotosAsociados.stream().map(p -> p.getNombre() + " (" + p.getRol().getEtiqueta() + ")")
                        .collect(Collectors.joining(", "));
        Label pilotos = new Label("Pilotos asociados: " + textoPilotos);
        pilotos.getStyleClass().add("texto-secundario");
        pilotos.setWrapText(true);

        VBox contenido = new VBox(12, encabezado, motor, velocidad, aceleracion, carga, modo, pilotos);
        panelDetalle.getChildren().setAll(contenido);
    }
}
