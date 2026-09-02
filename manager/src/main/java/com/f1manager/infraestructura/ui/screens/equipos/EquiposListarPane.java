package com.f1manager.infraestructura.ui.screens.equipos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.modelo.Equipo;
import com.f1manager.dominio.modelo.Monoplaza;
import com.f1manager.dominio.modelo.Piloto;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Sub-vista "Listar equipos": a la izquierda, la lista de equipos
 * registrados; al seleccionar uno, a la derecha se muestra su logo, sus
 * pilotos y el monoplaza que tiene asignado.
 */
public class EquiposListarPane extends HBox {

    private final VBox columnaLista = new VBox(10);
    private final StackPane panelDetalle = new StackPane();
    private VBox filaSeleccionada;

    public EquiposListarPane() {
        setSpacing(28);

        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefWidth(360);
        scroll.setMinWidth(300);
        scroll.setPrefHeight(560);

        panelDetalle.getStyleClass().add("panel");
        panelDetalle.setPrefSize(560, 560);
        panelDetalle.setPadding(new Insets(28));
        mostrarMensajeSinSeleccion();

        HBox.setHgrow(scroll, Priority.NEVER);
        HBox.setHgrow(panelDetalle, Priority.ALWAYS);
        getChildren().addAll(scroll, panelDetalle);

        var equipos = DataStore.getInstancia().getEquipos();
        if (equipos.isEmpty()) {
            Label vacio = new Label("No hay equipos registrados todavía.");
            vacio.getStyleClass().add("texto-secundario");
            columnaLista.getChildren().add(vacio);
        } else {
            for (Equipo equipo : equipos) {
                columnaLista.getChildren().add(construirFila(equipo));
            }
        }
    }

    private VBox construirFila(Equipo equipo) {
        StackPane icono = IconFactory.contenedor(IconFactory.imagenEquipo(equipo, 40), 54);

        Label nombre = new Label(equipo.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5f6fa;");
        nombre.setWrapText(true);

        Label detalle = new Label(equipo.getPais() + "  ·  " + equipo.getMotor());
        detalle.getStyleClass().add("texto-secundario");

        VBox textos = new VBox(4, nombre, detalle);
        HBox fila = new HBox(14, icono, textos);
        fila.setAlignment(Pos.CENTER_LEFT);

        VBox contenedorFila = new VBox(fila);
        contenedorFila.getStyleClass().add("fila-lista");
        contenedorFila.setOnMouseClicked(e -> seleccionar(equipo, contenedorFila));
        return contenedorFila;
    }

    private void seleccionar(Equipo equipo, VBox fila) {
        if (filaSeleccionada != null) {
            filaSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
            filaSeleccionada.getStyleClass().add("fila-lista");
        }
        fila.getStyleClass().remove("fila-lista");
        fila.getStyleClass().add("fila-lista-seleccionada");
        filaSeleccionada = fila;
        mostrarDetalle(equipo);
    }

    private void mostrarMensajeSinSeleccion() {
        Label mensaje = new Label("Selecciona un equipo de la lista\npara ver sus pilotos y su monoplaza.");
        mensaje.getStyleClass().add("texto-secundario");
        mensaje.setStyle("-fx-text-alignment: center;");
        panelDetalle.getChildren().setAll(mensaje);
    }

    private void mostrarDetalle(Equipo equipo) {
        StackPane logo = new StackPane(IconFactory.imagenEquipo(equipo, 90));
        logo.setAlignment(Pos.CENTER);

        Label nombre = new Label(equipo.getNombre());
        nombre.getStyleClass().add("titulo-seccion");

        Label paisMotor = new Label(equipo.getPais() + "  ·  Motor: " + equipo.getMotor());
        paisMotor.getStyleClass().add("texto-rojo");

        HBox encabezado = new HBox(18, logo, new VBox(6, nombre, paisMotor));
        encabezado.setAlignment(Pos.CENTER_LEFT);

        VBox seccionPilotos = construirSeccionPilotos(equipo);
        VBox seccionVehiculo = construirSeccionVehiculo(equipo);

        VBox contenido = new VBox(16, encabezado, new Separator(), seccionPilotos, new Separator(), seccionVehiculo);

        ScrollPane scrollDetalle = new ScrollPane(contenido);
        scrollDetalle.setFitToWidth(true);
        scrollDetalle.getStyleClass().add("scroll-oscuro");
        scrollDetalle.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        panelDetalle.getChildren().setAll(scrollDetalle);
    }

    private VBox construirSeccionPilotos(Equipo equipo) {
        Label titulo = new Label("Pilotos");
        titulo.getStyleClass().add("etiqueta-campo");

        List<Piloto> pilotos = DataStore.getInstancia().getPilotosPorEquipo(equipo.getNombre());
        VBox seccion = new VBox(10, titulo);
        if (pilotos.isEmpty()) {
            Label vacio = new Label("Sin pilotos asignados actualmente.");
            vacio.getStyleClass().add("texto-secundario");
            seccion.getChildren().add(vacio);
            return seccion;
        }
        for (Piloto p : pilotos) {
            StackPane avatar = IconFactory.avatarPiloto(p, 64);

            Label nombre = new Label(p.getNombre() + "   ·   " + p.getRol().getEtiqueta());
            nombre.getStyleClass().add("texto-normal");
            nombre.setStyle("-fx-font-weight: bold;");

            Label habilidad = new Label(String.format("Experiencia: %d años   ·   Habilidad promedio: %.0f/100",
                    p.getExperienciaAnios(), p.getHabilidadPromedio()));
            habilidad.getStyleClass().add("texto-secundario");

            HBox fila = new HBox(12, avatar, new VBox(2, nombre, habilidad));
            fila.setAlignment(Pos.CENTER_LEFT);
            seccion.getChildren().add(fila);
        }
        return seccion;
    }

    private VBox construirSeccionVehiculo(Equipo equipo) {
        Label titulo = new Label("Monoplaza");
        titulo.getStyleClass().add("etiqueta-campo");

        Monoplaza vehiculo = DataStore.getInstancia().getVehiculoPorEquipo(equipo.getNombre());
        if (vehiculo == null) {
            Label vacio = new Label("Sin monoplaza asignado actualmente.");
            vacio.getStyleClass().add("texto-secundario");
            return new VBox(10, titulo, vacio);
        }

        Label modelo = new Label(vehiculo.getModelo() + "   ·   Motor: " + vehiculo.getMotor());
        modelo.getStyleClass().add("texto-normal");
        modelo.setStyle("-fx-font-weight: bold;");

        Label rendimiento = new Label(String.format("Velocidad máxima: %.0f km/h   ·   Aceleración 0-100: %.1f s",
                vehiculo.getVelocidadMaxKmh(), vehiculo.getAceleracion0a100()));
        Label configuracion = new Label(String.format("Carga aerodinámica: %s   ·   Modo: %s   ·   Neumático: %s   ·   Presión: %.1f PSI",
                vehiculo.getCargaAerodinamica().getEtiqueta(), vehiculo.getModoConduccion().getEtiqueta(),
                vehiculo.getTipoNeumatico().getEtiqueta(), vehiculo.getPresionAire()));
        for (Label l : List.of(rendimiento, configuracion)) {
            l.getStyleClass().add("texto-secundario");
            l.setWrapText(true);
        }

        return new VBox(6, titulo, modelo, rendimiento, configuracion);
    }
}
