package com.f1manager.infraestructura.ui.screens.equipos;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.excepcion.ValidacionException;
import com.f1manager.infraestructura.ui.util.GestorSonido;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class EquiposRegistrarPane extends VBox {

    private final TextField campoNombre = new TextField();
    private final TextField campoPais = new TextField();
    private final TextField campoMotor = new TextField();
    private final Label mensaje = new Label();

    private final ImageView vistaPrevia = new ImageView();
    private String imagenSeleccionadaUrl = null;

    public EquiposRegistrarPane(Runnable alCancelar) {
        setSpacing(20);
        setMaxWidth(600);

        Label titulo = new Label("Registrar nuevo equipo");
        titulo.getStyleClass().add("titulo-seccion");

        VBox panel = new VBox(16);
        panel.getStyleClass().add("panel-glow");
        panel.setPadding(new Insets(30));

        campoNombre.getStyleClass().add("campo-texto");
        campoNombre.setPromptText("Ej: Williams Racing");

        campoPais.getStyleClass().add("campo-texto");
        campoPais.setPromptText("Ej: Reino Unido");

        campoMotor.getStyleClass().add("campo-texto");
        campoMotor.setPromptText("Ej: Mercedes");

        mensaje.setWrapText(true);

        VBox seccionImagen = construirSeccionImagen();

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
                etiqueta("Nombre del equipo"), campoNombre,
                etiqueta("País"), campoPais,
                etiqueta("Motor"), campoMotor,
                seccionImagen, mensaje, botones
        );

        getChildren().addAll(titulo, panel);
    }

    private VBox construirSeccionImagen() {
        Label tituloSeccion = etiqueta("Subir imagen del PC o usar imagen por defecto");

        vistaPrevia.setFitWidth(64);
        vistaPrevia.setFitHeight(64);
        vistaPrevia.setPreserveRatio(false);

        Button botonSubir = new Button("SUBIR IMAGEN DEL PC");
        botonSubir.getStyleClass().add("boton-secundario");
        botonSubir.setOnAction(e -> subirImagenDesdePc());

        Label ayuda = new Label("Si no subes una, se usará la imagen por defecto de gestión de equipos.");
        ayuda.getStyleClass().add("texto-secundario");
        ayuda.setWrapText(true);

        HBox filaSubida = new HBox(16, botonSubir, vistaPrevia);
        filaSubida.setAlignment(Pos.CENTER_LEFT);

        return new VBox(8, tituloSeccion, filaSubida, ayuda);
    }

    private void subirImagenDesdePc() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Selecciona el logo del equipo");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = selector.showOpenDialog(getScene() != null ? getScene().getWindow() : null);
        if (archivo == null) {
            return;
        }
        imagenSeleccionadaUrl = archivo.toURI().toString();
        vistaPrevia.setImage(new Image(imagenSeleccionadaUrl, 64, 64, false, true));
    }

    private Label etiqueta(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("etiqueta-campo");
        return label;
    }

    private void guardar() {
        try {
            DataStore.getInstancia().registrarEquipo(campoNombre.getText(), campoPais.getText(), campoMotor.getText(),
                    imagenSeleccionadaUrl);
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Equipo registrado correctamente.");
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
        campoNombre.clear();
        campoPais.clear();
        campoMotor.clear();
        imagenSeleccionadaUrl = null;
        vistaPrevia.setImage(null);
    }
}
