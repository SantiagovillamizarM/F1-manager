package com.f1manager.infraestructura.ui.screens.pilotos;

import com.f1manager.dominio.modelo.Piloto;
import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.excepcion.ValidacionException;
import com.f1manager.dominio.modelo.RolPiloto;
import com.f1manager.infraestructura.ui.util.GestorImagenes;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PilotosRegistrarPane extends VBox {

    private static final String[] AVATARES_PREDETERMINADOS = {
            "avatar 1.png", "avatar 2.png", "avatar 3.png",
            "avatar 4.png", "avatar 5.png", "avatar 6.png", "avatar 7.png"
    };

    private final TextField campoNombre = new TextField();
    private final ComboBox<String> comboEquipo = new ComboBox<>();
    private final ComboBox<RolPiloto> comboRol = new ComboBox<>();
    private final TextField campoExperiencia = new TextField();
    private final TextField campoSeco = new TextField();
    private final TextField campoLluvia = new TextField();
    private final TextField campoExtremo = new TextField();
    private final TextField campoCurva = new TextField();
    private final TextField campoAdelantamiento = new TextField();
    private final TextField campoRecta = new TextField();
    private final Label mensaje = new Label();

    private final ImageView vistaPrevia = new ImageView();
    private final List<StackPane> opcionesImagen = new ArrayList<>();
    private String imagenSeleccionadaUrl = null;

    public PilotosRegistrarPane(Runnable alCancelar) {
        setSpacing(20);
        setMaxWidth(820);

        Label titulo = new Label("Registrar nuevo piloto");
        titulo.getStyleClass().add("titulo-seccion");

        VBox panel = new VBox(22);
        panel.getStyleClass().add("panel-glow");
        panel.setPadding(new Insets(30));

        campoNombre.getStyleClass().add("campo-texto");
        campoNombre.setPromptText("Nombre completo del piloto");

        comboEquipo.getStyleClass().add("combo-oscuro");
        comboEquipo.setMaxWidth(Double.MAX_VALUE);
        comboEquipo.setPromptText("Seleccione un equipo");
        actualizarEquipos();

        comboRol.getStyleClass().add("combo-oscuro");
        comboRol.setMaxWidth(Double.MAX_VALUE);
        comboRol.setItems(FXCollections.observableArrayList(RolPiloto.values()));
        comboRol.setPromptText("Seleccione un rol");

        campoExperiencia.getStyleClass().add("campo-texto");
        campoExperiencia.setPromptText("Ej: 5");

        campoSeco.getStyleClass().add("campo-texto");
        campoSeco.setPromptText("Ej: 88 (1 a 100)");

        campoLluvia.getStyleClass().add("campo-texto");
        campoLluvia.setPromptText("Ej: 88 (1 a 100)");

        campoExtremo.getStyleClass().add("campo-texto");
        campoExtremo.setPromptText("Ej: 88 (1 a 100)");

        campoCurva.getStyleClass().add("campo-texto");
        campoCurva.setPromptText("Ej: 88 (1 a 100)");

        campoAdelantamiento.getStyleClass().add("campo-texto");
        campoAdelantamiento.setPromptText("Ej: 88 (1 a 100)");

        campoRecta.getStyleClass().add("campo-texto");
        campoRecta.setPromptText("Ej: 88 (1 a 100)");

        mensaje.setWrapText(true);

        // --- Datos básicos y habilidades, en 2 columnas para reducir el scroll ---
        GridPane grilla = new GridPane();
        grilla.setHgap(24);
        grilla.setVgap(16);
        grilla.getColumnConstraints().addAll(
                columnaFlexible(), columnaFlexible());

        grilla.add(campoConEtiqueta("Nombre", campoNombre), 0, 0, 2, 1);
        grilla.add(campoConEtiqueta("Equipo", comboEquipo), 0, 1);
        grilla.add(campoConEtiqueta("Rol", comboRol), 1, 1);
        grilla.add(campoConEtiqueta("Años de experiencia", campoExperiencia), 0, 2);
        grilla.add(campoConEtiqueta("Habilidad en seco (1-100)", campoSeco), 1, 2);
        grilla.add(campoConEtiqueta("Habilidad en lluvia (1-100)", campoLluvia), 0, 3);
        grilla.add(campoConEtiqueta("Habilidad en clima extremo (1-100)", campoExtremo), 1, 3);
        grilla.add(campoConEtiqueta("Habilidad en curva (1-100)", campoCurva), 0, 4);
        grilla.add(campoConEtiqueta("Habilidad de adelantamiento (1-100)", campoAdelantamiento), 1, 4);
        grilla.add(campoConEtiqueta("Habilidad en recta (1-100)", campoRecta), 0, 5);

        // --- Foto del piloto: subir desde el PC o elegir un avatar predeterminado ---
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

        panel.getChildren().addAll(grilla, new Separator(), seccionImagen, mensaje, botones);

        getChildren().addAll(titulo, panel);
    }

    private javafx.scene.layout.ColumnConstraints columnaFlexible() {
        var columna = new javafx.scene.layout.ColumnConstraints();
        columna.setPercentWidth(50);
        return columna;
    }

    private VBox campoConEtiqueta(String etiqueta, Control campo) {
        VBox caja = new VBox(6, etiqueta(etiqueta), campo);
        return caja;
    }

    private VBox construirSeccionImagen() {
        Label tituloSeccion = new Label("Foto del piloto");
        tituloSeccion.getStyleClass().add("titulo-seccion");

        vistaPrevia.setFitWidth(64);
        vistaPrevia.setFitHeight(64);
        vistaPrevia.setPreserveRatio(false);

        Button botonSubir = new Button("SUBIR IMAGEN DEL PC");
        botonSubir.getStyleClass().add("boton-secundario");
        botonSubir.setOnAction(e -> subirImagenDesdePc());

        HBox filaSubida = new HBox(16, botonSubir, vistaPrevia);
        filaSubida.setAlignment(Pos.CENTER_LEFT);

        Label etiquetaAvatares = new Label("O elige un avatar predeterminado:");
        etiquetaAvatares.getStyleClass().add("etiqueta-campo");

        FlowPane filaAvatares = new FlowPane(12, 12);
        for (String archivo : AVATARES_PREDETERMINADOS) {
            filaAvatares.getChildren().add(construirOpcionAvatar(archivo));
        }

        VBox seccion = new VBox(14, tituloSeccion, filaSubida, etiquetaAvatares, filaAvatares);
        return seccion;
    }

    private StackPane construirOpcionAvatar(String archivo) {
        Image miniatura = GestorImagenes.cargar("avatars predeterminados/" + archivo);
        ImageView vista = new ImageView(miniatura);
        vista.setFitWidth(56);
        vista.setFitHeight(56);
        vista.setPreserveRatio(false);

        StackPane opcion = new StackPane(vista);
        opcion.getStyleClass().add("opcion-clima");
        opcion.setPrefSize(76, 76);
        opcion.setOnMouseClicked(e -> {
            imagenSeleccionadaUrl = GestorImagenes.urlDe("avatars predeterminados/" + archivo);
            vistaPrevia.setImage(miniatura);
            marcarSeleccionada(opcion);
        });
        opcionesImagen.add(opcion);
        return opcion;
    }

    private void subirImagenDesdePc() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Selecciona la foto del piloto");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = selector.showOpenDialog(getScene() != null ? getScene().getWindow() : null);
        if (archivo == null) {
            return;
        }
        imagenSeleccionadaUrl = archivo.toURI().toString();
        vistaPrevia.setImage(new Image(imagenSeleccionadaUrl, 64, 64, false, true));
        marcarSeleccionada(null);
    }

    /** Resalta la opción de avatar elegida (o ninguna, si la foto vino de subir un archivo). */
    private void marcarSeleccionada(StackPane elegida) {
        for (StackPane opcion : opcionesImagen) {
            opcion.getStyleClass().setAll(opcion == elegida ? "opcion-clima-seleccionada" : "opcion-clima");
        }
    }

    private void actualizarEquipos() {
        var nombres = DataStore.getInstancia().getEquipos().stream()
                .map(com.f1manager.dominio.modelo.Equipo::getNombre).toList();
        comboEquipo.setItems(FXCollections.observableArrayList(nombres));
    }

    private Label etiqueta(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("etiqueta-campo");
        return label;
    }

    private void guardar() {
        try {
            actualizarEquipos();
            Piloto nuevo = DataStore.getInstancia().registrarPiloto(
                    campoNombre.getText(), comboEquipo.getValue(), comboRol.getValue(),
                    campoExperiencia.getText(),
                    campoCurva.getText(), campoAdelantamiento.getText(), campoRecta.getText(),
                    campoLluvia.getText(), campoSeco.getText(), campoExtremo.getText());
            nuevo.setImagenUrl(imagenSeleccionadaUrl);
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Piloto registrado correctamente.");
            limpiar();
        } catch (ValidacionException ex) {
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
        }
    }

    private void limpiar() {
        campoNombre.clear();
        comboEquipo.setValue(null);
        comboRol.setValue(null);
        campoExperiencia.clear();
        campoSeco.clear();
        campoLluvia.clear();
        campoExtremo.clear();
        campoCurva.clear();
        campoAdelantamiento.clear();
        campoRecta.clear();
        imagenSeleccionadaUrl = null;
        vistaPrevia.setImage(null);
        marcarSeleccionada(null);
    }
}
