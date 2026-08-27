package com.f1manager.infraestructura.ui.screens.equipos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.modelo.Equipo;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Sub-vista "Listar equipos": muestra los equipos como tarjetas con su
 * país, motor e IDs de los pilotos que pertenecen a cada uno.
 */
public class EquiposListarPane extends VBox {

    public EquiposListarPane() {
        setSpacing(20);

        Label titulo = new Label("Equipos registrados");
        titulo.getStyleClass().add("titulo-seccion");

        FlowPane contenedor = new FlowPane(22, 22);
        contenedor.setPrefWrapLength(1100);

        for (Equipo equipo : DataStore.getInstancia().getEquipos()) {
            contenedor.getChildren().add(construirTarjeta(equipo));
        }
        if (DataStore.getInstancia().getEquipos().isEmpty()) {
            Label vacio = new Label("No hay equipos registrados todavía.");
            vacio.getStyleClass().add("texto-secundario");
            contenedor.getChildren().add(vacio);
        }

        getChildren().addAll(titulo, contenedor);
    }

    private VBox construirTarjeta(Equipo equipo) {
        StackPane icono = IconFactory.contenedor(IconFactory.escudoEquipo(IconFactory.BLANCO), 54);

        Label nombre = new Label(equipo.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #f5f6fa;");
        nombre.setWrapText(true);

        Label pais = new Label("País: " + equipo.getPais());
        pais.getStyleClass().add("texto-secundario");

        Label motor = new Label("Motor: " + equipo.getMotor());
        motor.getStyleClass().add("texto-secundario");

        var idsPilotos = DataStore.getInstancia().getIdsPilotosDeEquipo(equipo.getNombre());
        String textoIds = idsPilotos.isEmpty() ? "Sin pilotos asignados"
                : "Pilotos (ID): " + idsPilotos.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("");
        Label pilotos = new Label(textoIds);
        pilotos.getStyleClass().add("texto-secundario");
        pilotos.setWrapText(true);

        VBox tarjeta = new VBox(10, icono, nombre, pais, motor, pilotos);
        tarjeta.getStyleClass().add("panel");
        tarjeta.setPadding(new Insets(20));
        tarjeta.setPrefWidth(280);
        tarjeta.setAlignment(Pos.TOP_LEFT);
        return tarjeta;
    }
}
