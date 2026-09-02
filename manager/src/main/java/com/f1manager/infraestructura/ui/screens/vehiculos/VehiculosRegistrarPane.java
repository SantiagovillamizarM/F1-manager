package com.f1manager.infraestructura.ui.screens.vehiculos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.excepcion.ValidacionException;
import com.f1manager.dominio.modelo.Monoplaza;
import com.f1manager.infraestructura.ui.util.GestorSonido;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Sub-vista "Registrar vehículo": crea el monoplaza de un equipo que
 * todavía no tiene uno (sin esto, ese equipo podía correr con velocidad 0
 * y sin neumático asignado).
 */
public class VehiculosRegistrarPane extends VBox {

    private final TextField campoModelo = new TextField();
    private final ComboBox<String> comboEquipo = new ComboBox<>();
    private final TextField campoMotor = new TextField();
    private final TextField campoVelocidad = new TextField();
    private final TextField campoAceleracion = new TextField();
    private final Label mensaje = new Label();

    public VehiculosRegistrarPane(Runnable alCancelar) {
        setSpacing(20);
        setMaxWidth(600);

        Label titulo = new Label("Registrar nuevo vehículo");
        titulo.getStyleClass().add("titulo-seccion");

        VBox panel = new VBox(16);
        panel.getStyleClass().add("panel-glow");
        panel.setPadding(new Insets(30));

        campoModelo.getStyleClass().add("campo-texto");
        campoModelo.setPromptText("Ej: SF-24");

        comboEquipo.getStyleClass().add("combo-oscuro");
        comboEquipo.setMaxWidth(Double.MAX_VALUE);
        comboEquipo.setPromptText("Seleccione un equipo sin vehículo asignado");
        actualizarEquiposDisponibles();

        campoMotor.getStyleClass().add("campo-texto");
        campoMotor.setPromptText("Ej: Ferrari");

        campoVelocidad.getStyleClass().add("campo-texto");
        campoVelocidad.setPromptText(String.format("Ej: 350 (km/h, entre %.0f y %.0f)",
                Monoplaza.VELOCIDAD_MINIMA_KMH, Monoplaza.VELOCIDAD_MAXIMA_KMH));

        campoAceleracion.getStyleClass().add("campo-texto");
        campoAceleracion.setPromptText(String.format("Ej: 2.6 (segundos 0-100 km/h, entre %.1f y %.1f)",
                Monoplaza.ACELERACION_MINIMA_S, Monoplaza.ACELERACION_MAXIMA_S));

        mensaje.setWrapText(true);

        Button guardar = new Button("GUARDAR");
        guardar.getStyleClass().add("boton-primario");
        guardar.setOnAction(e -> guardar());

        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> {
            limpiar();
            alCancelar.run();
        });

        HBox botones = new HBox(14, guardar, cancelar);
        botones.setAlignment(Pos.CENTER_LEFT);

        panel.getChildren().addAll(
                etiqueta("Modelo"), campoModelo,
                etiqueta("Equipo"), comboEquipo,
                etiqueta("Motor"), campoMotor,
                etiqueta("Velocidad máxima"), campoVelocidad,
                etiqueta("Aceleración 0-100 km/h"), campoAceleracion,
                mensaje, botones
        );

        getChildren().addAll(titulo, panel);
    }

    private Label etiqueta(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("etiqueta-campo");
        return label;
    }

    /** Solo los equipos que todavía no tienen un vehículo asignado (un vehículo por equipo). */
    private void actualizarEquiposDisponibles() {
        var nombres = DataStore.getInstancia().getEquipos().stream()
                .filter(e -> DataStore.getInstancia().getVehiculoPorEquipo(e.getNombre()) == null)
                .map(com.f1manager.dominio.modelo.Equipo::getNombre).toList();
        comboEquipo.setItems(FXCollections.observableArrayList(nombres));
    }

    private void guardar() {
        try {
            actualizarEquiposDisponibles();
            DataStore.getInstancia().registrarVehiculo(campoModelo.getText(), comboEquipo.getValue(),
                    campoMotor.getText(), campoVelocidad.getText(), campoAceleracion.getText());
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Vehículo registrado correctamente.");
            GestorSonido.reproducir("Confirmado sound.mp3");
            limpiar();
        } catch (ValidacionException ex) {
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
            GestorSonido.reproducir("Error sound.mp3");
        }
    }

    private void limpiar() {
        campoModelo.clear();
        comboEquipo.setValue(null);
        campoMotor.clear();
        campoVelocidad.clear();
        campoAceleracion.clear();
        actualizarEquiposDisponibles();
    }
}
