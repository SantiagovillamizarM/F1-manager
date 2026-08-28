package com.f1manager.infraestructura.ui.screens.vehiculos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.excepcion.ValidacionException;
import com.f1manager.dominio.modelo.CargaAerodinamica;
import com.f1manager.dominio.modelo.ModoConduccion;
import com.f1manager.dominio.modelo.Monoplaza;
import com.f1manager.dominio.modelo.TipoNeumatico;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Sub-vista "Configurar vehículo": permite elegir un monoplaza y ajustar
 * su carga aerodinámica y modo de conducción, afectando realmente la
 * simulación de carrera.
 */
public class VehiculosConfigurarPane extends VBox {

    private final ComboBox<Monoplaza> comboVehiculo = new ComboBox<>();
    private CargaAerodinamica cargaSeleccionada;
    private ModoConduccion modoSeleccionado;
    private TipoNeumatico neumaticoSeleccionado;
    private final HBox filaCargas = new HBox(14);
    private final HBox filaModos = new HBox(14);
    private final HBox filaNeumaticos = new HBox(14);
    private final Label mensaje = new Label();

    public VehiculosConfigurarPane() {
        setSpacing(20);
        setMaxWidth(640);

        Label titulo = new Label("Configurar vehículo");
        titulo.getStyleClass().add("titulo-seccion");

        VBox panel = new VBox(20);
        panel.getStyleClass().add("panel-glow");
        panel.setPadding(new Insets(30));

        Label etiquetaModelo = new Label("Seleccione el modelo del vehículo");
        etiquetaModelo.getStyleClass().add("etiqueta-campo");

        comboVehiculo.getStyleClass().add("combo-oscuro");
        comboVehiculo.setMaxWidth(Double.MAX_VALUE);
        comboVehiculo.setItems(FXCollections.observableArrayList(DataStore.getInstancia().getVehiculos()));
        comboVehiculo.setPromptText("Seleccione un monoplaza");
        comboVehiculo.setOnAction(e -> cargarConfiguracionActual());

        Label etiquetaCarga = new Label("Carga aerodinámica");
        etiquetaCarga.getStyleClass().add("etiqueta-campo");
        construirOpciones(filaCargas, CargaAerodinamica.values(), o -> {
            cargaSeleccionada = (CargaAerodinamica) o;
            actualizarSeleccionVisual(filaCargas, o.toString());
        });

        Label etiquetaModo = new Label("Modo de conducción");
        etiquetaModo.getStyleClass().add("etiqueta-campo");
        construirOpciones(filaModos, ModoConduccion.values(), o -> {
            modoSeleccionado = (ModoConduccion) o;
            actualizarSeleccionVisual(filaModos, o.toString());
        });

        Label etiquetaNeumatico = new Label("Tipo de neumático");
        etiquetaNeumatico.getStyleClass().add("etiqueta-campo");
        construirOpciones(filaNeumaticos, TipoNeumatico.values(), o -> {
            neumaticoSeleccionado = (TipoNeumatico) o;
            actualizarSeleccionVisual(filaNeumaticos, o.toString());
        });

        mensaje.setWrapText(true);

        Button guardar = new Button("GUARDAR");
        guardar.getStyleClass().add("boton-primario");
        guardar.setOnAction(e -> guardar());

        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> {
            comboVehiculo.setValue(null);
            cargaSeleccionada = null;
            modoSeleccionado = null;
            neumaticoSeleccionado = null;
            filaCargas.getChildren().forEach(n -> n.getStyleClass().setAll("opcion-clima"));
            filaModos.getChildren().forEach(n -> n.getStyleClass().setAll("opcion-clima"));
            filaNeumaticos.getChildren().forEach(n -> n.getStyleClass().setAll("opcion-clima"));
            mensaje.setText("");
        });

        HBox botones = new HBox(14, guardar, cancelar);
        botones.setAlignment(Pos.CENTER_LEFT);

        panel.getChildren().addAll(
                etiquetaModelo, comboVehiculo,
                etiquetaCarga, filaCargas,
                etiquetaModo, filaModos,
                etiquetaNeumatico, filaNeumaticos,
                mensaje, botones
        );

        getChildren().addAll(titulo, panel);
    }

    private void construirOpciones(HBox contenedor, Object[] valores, java.util.function.Consumer<Object> alSeleccionar) {
        for (Object valor : valores) {
            Label label = new Label(valor.toString());
            label.getStyleClass().add("texto-normal");
            VBox opcion = new VBox(label);
            opcion.getStyleClass().add("opcion-clima");
            opcion.setPrefWidth(140);
            opcion.setOnMouseClicked(e -> alSeleccionar.accept(valor));
            contenedor.getChildren().add(opcion);
        }
    }

    private void actualizarSeleccionVisual(HBox contenedor, String textoSeleccionado) {
        for (var nodo : contenedor.getChildren()) {
            VBox opcion = (VBox) nodo;
            Label label = (Label) opcion.getChildren().get(0);
            opcion.getStyleClass().setAll(label.getText().equals(textoSeleccionado) ? "opcion-clima-seleccionada" : "opcion-clima");
        }
    }

    private void cargarConfiguracionActual() {
        Monoplaza m = comboVehiculo.getValue();
        if (m == null) return;
        cargaSeleccionada = m.getCargaAerodinamica();
        modoSeleccionado = m.getModoConduccion();
        neumaticoSeleccionado = m.getTipoNeumatico();
        actualizarSeleccionVisual(filaCargas, cargaSeleccionada.toString());
        actualizarSeleccionVisual(filaModos, modoSeleccionado.toString());
        actualizarSeleccionVisual(filaNeumaticos, neumaticoSeleccionado.toString());
    }

    private void guardar() {
        try {
            if (comboVehiculo.getValue() == null) {
                throw new ValidacionException("Debe seleccionar un modelo de vehículo.");
            }
            DataStore.getInstancia().configurarVehiculo(comboVehiculo.getValue().getId(), cargaSeleccionada, modoSeleccionado,
                    neumaticoSeleccionado);
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Configuración guardada correctamente para " + comboVehiculo.getValue().getModelo() + ".");
        } catch (ValidacionException ex) {
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
        }
    }
}
