//Sub-pantalla para registrar un equipo nuevo: pide nombre, país, motor y
//opcionalmente una imagen/logo subida desde el PC, valida los datos y lo
//guarda en el DataStore.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.equipos;

//Trae la clase que guarda y maneja toda la información del programa (equipos, pilotos, vehiculos, etc), funciona como la "base de datos" en memoria
import com.f1manager.aplicacion.DataStore;
//Trae el error personalizado que se lanza cuando el usuario ingresa datos inválidos
import com.f1manager.dominio.excepcion.ValidacionException;
//Trae la clase que reproduce los sonidos de confirmación/error de la app
import com.f1manager.infraestructura.ui.util.GestorSonido;
//Trae Insets, que sirve para poner márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (centrado, izquierda, etc)
import javafx.geometry.Pos;
//Trae Button, el botón que se puede clickear
import javafx.scene.control.Button;
//Trae Label, que es un texto que se muestra en pantalla (no editable)
import javafx.scene.control.Label;
//Trae TextField, que es el cuadro de texto donde el usuario puede escribir
import javafx.scene.control.TextField;
//Trae Image, que representa una imagen ya cargada en memoria para poder mostrarla
import javafx.scene.image.Image;
//Trae ImageView, que es el componente que efectivamente dibuja/muestra una Image en la pantalla
import javafx.scene.image.ImageView;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila horizontal)
import javafx.scene.layout.HBox;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna vertical)
import javafx.scene.layout.VBox;
//Trae FileChooser, que es la ventanita del sistema operativo para elegir un archivo del PC (la usamos para elegir la imagen del logo)
import javafx.stage.FileChooser;

//Trae la clase File, que representa un archivo del sistema (la imagen elegida en el FileChooser)
import java.io.File;

//Clase publica llamada "EquiposRegistrarPane" que hereda de VBox (osea que ella misma es una columna donde se van poniendo los elementos)
public class EquiposRegistrarPane extends VBox {

    //Cuadro de texto donde se escribe el nombre del equipo nuevo
    private final TextField campoNombre = new TextField();
    //Cuadro de texto donde se escribe el país del equipo nuevo
    private final TextField campoPais = new TextField();
    //Cuadro de texto donde se escribe el motor del equipo nuevo
    private final TextField campoMotor = new TextField();
    //Label donde se muestra el resultado de la operación (si se guardó bien o si hubo un error)
    private final Label mensaje = new Label();

    //Este ImageView muestra en chiquito la imagen que el usuario eligió como logo, antes de guardar
    private final ImageView vistaPrevia = new ImageView();
    //Guarda la URL (ruta convertida a texto) de la imagen que el usuario seleccionó del PC; si sigue en null es porque no eligió ninguna
    private String imagenSeleccionadaUrl = null;

    //Constructor
    //Recibe una acción para ejecutar cuando se cancela (normalmente volver a la pantalla de listar) y arma todo el formulario
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

    //Arma la sección de subir imagen: el botón para elegir el archivo, la vista previa y el texto de ayuda
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

    //Este método abre la ventanita del sistema (FileChooser) para que el usuario elija una imagen del PC,
    //y si elige una, la convierte en URL y la muestra en la vista previa
    private void subirImagenDesdePc() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Selecciona el logo del equipo");
        //Solo deja elegir archivos de imagen (png o jpg)
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        //Abre el diálogo de selección de archivo sobre la ventana actual
        File archivo = selector.showOpenDialog(getScene() != null ? getScene().getWindow() : null);
        //Si el usuario cerró el diálogo sin elegir nada, no se hace nada más
        if (archivo == null) {
            return;
        }
        //Convierte el archivo elegido en una URL de texto (asi es como JavaFX puede cargar la imagen)
        imagenSeleccionadaUrl = archivo.toURI().toString();
        //Carga la imagen en la vista previa a 64x64 para mostrarla chiquita
        vistaPrevia.setImage(new Image(imagenSeleccionadaUrl, 64, 64, false, true));
    }

    //Crea un Label con el estilo de etiqueta de campo, para no repetir código en cada campo del formulario
    private Label etiqueta(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("etiqueta-campo");
        return label;
    }

    //Este método intenta guardar el equipo nuevo con los datos escritos en el formulario
    private void guardar() {
        try {
            //Le pide al DataStore que registre el equipo con nombre, país, motor y la URL de la imagen (puede ser null si no subió ninguna)
            DataStore.getInstancia().registrarEquipo(campoNombre.getText(), campoPais.getText(), campoMotor.getText(),
                    imagenSeleccionadaUrl);
            //Si todo salió bien se muestra el mensaje en rojo (positivo), se reproduce el sonido de confirmación y se limpia el formulario
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Equipo registrado correctamente.");
            GestorSonido.reproducir("Confirmado sound.mp3");
            limpiar();
        } catch (ValidacionException ex) {
            //Si algo salió mal (nombre repetido, campos vacíos, etc) se muestra el mensaje de error y se reproduce el sonido de error
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
            GestorSonido.reproducir("Error sound.mp3");
        }
    }

    //Limpia todos los campos del formulario y la imagen seleccionada, dejando la pantalla lista para un nuevo registro
    private void limpiar() {
        campoNombre.clear();
        campoPais.clear();
        campoMotor.clear();
        imagenSeleccionadaUrl = null;
        vistaPrevia.setImage(null);
    }
}
