//Esta es la pantalla para editar un piloto ya existente: a la izquierda hay
//una lista buscable de pilotos, y al elegir uno (o cargarlo por ID) se llena
//el mismo formulario que en el registro pero con sus datos actuales, listo
//para modificarlos y guardar los cambios.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.pilotos;

//Trae el DataStore, que es donde se guarda toda la información del juego (pilotos, equipos, etc.) en memoria
import com.f1manager.aplicacion.DataStore;
//Trae ValidacionException, el error controlado que se lanza cuando algo que escribio el usuario no es valido
import com.f1manager.dominio.excepcion.ValidacionException;
//Trae Piloto, la clase que representa a un piloto con toda su informacion (habilidades, equipo, etc.)
import com.f1manager.dominio.modelo.Piloto;
//Trae RolPiloto, el enum con los roles posibles de un piloto (por ejemplo Titular, Reserva, etc.)
import com.f1manager.dominio.modelo.RolPiloto;
//Trae CampoBusqueda, el componente propio del proyecto con un campo de texto para filtrar/buscar
import com.f1manager.infraestructura.ui.components.CampoBusqueda;
//Trae GestorImagenes, la herramienta propia del proyecto para cargar imagenes (avatares, logos, etc.)
import com.f1manager.infraestructura.ui.util.GestorImagenes;
//Trae GestorSonido, la herramienta propia del proyecto para reproducir sonidos (confirmacion, error, etc.)
import com.f1manager.infraestructura.ui.util.GestorSonido;
//Trae IconFactory, que es la fabrica de iconos/avatares que se usan en la lista de pilotos
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae FXCollections, la herramienta de JavaFX para crear listas observables (listas que los ComboBox pueden "escuchar" y mostrar)
import javafx.collections.FXCollections;
//Trae Insets, que sirve para poner margenes/rellenos alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para definir alineaciones (centrado, izquierda, etc.)
import javafx.geometry.Pos;
//Trae todos los controles basicos de JavaFX (Button, Label, TextField, ComboBox, ScrollPane, Separator, Control, etc.)
import javafx.scene.control.*;
//Trae Image, la clase de JavaFX que representa una imagen ya cargada en memoria
import javafx.scene.image.Image;
//Trae ImageView, el componente de JavaFX que muestra una Image en pantalla
import javafx.scene.image.ImageView;
//Trae FlowPane, un contenedor de JavaFX que acomoda sus elementos en fila y salta de linea automaticamente cuando no caben
import javafx.scene.layout.FlowPane;
//Trae GridPane, un contenedor de JavaFX que organiza sus elementos en una cuadricula de filas y columnas
import javafx.scene.layout.GridPane;
//Trae HBox, que es un contenedor de JavaFX que acomoda sus elementos uno al lado del otro (en horizontal)
import javafx.scene.layout.HBox;
//Trae Priority, que sirve para decirle a un contenedor cuanto debe crecer un elemento comparado con los demas
import javafx.scene.layout.Priority;
//Trae StackPane, un contenedor de JavaFX que apila sus elementos uno encima del otro
import javafx.scene.layout.StackPane;
//Trae VBox, que es un contenedor de JavaFX que acomoda sus elementos uno debajo del otro (en vertical)
import javafx.scene.layout.VBox;
//Trae FileChooser, la ventana nativa de JavaFX para elegir un archivo del computador (se usa para subir la foto)
import javafx.stage.FileChooser;

//Importa la clase File, que representa un archivo del sistema (por ejemplo la foto elegida desde el PC)
import java.io.File;
//Importa la clase de ArrayList para poder usar listas (Son las que tienen <>)
import java.util.ArrayList;
//Importa la interfaz List, que define el comportamiento general de una lista en Java
import java.util.List;

//Clase publica que extiende de VBox, osea que esta pantalla ES un VBox (una caja vertical)
public class PilotosEditarPane extends VBox {

    //Arreglo fijo con los nombres de archivo de los avatares predeterminados que el usuario puede elegir en vez de subir su propia foto
    private static final String[] AVATARES_PREDETERMINADOS = {
            "avatar 1.png", "avatar 2.png", "avatar 3.png",
            "avatar 4.png", "avatar 5.png", "avatar 6.png", "avatar 7.png"
    };

    //Caja vertical donde se van agregando las filas de la lista de pilotos (una fila por piloto)
    private final VBox columnaLista = new VBox(10);
    //Campo de busqueda para filtrar la lista de pilotos por ID, nombre o equipo
    private final CampoBusqueda busqueda = new CampoBusqueda("Buscar por ID, nombre o equipo...");

    //Campo de texto donde se ve el ID del piloto cargado (no se puede editar a mano, solo se llena solo)
    private final TextField campoId = new TextField();
    //Campo de texto donde se escribe/edita el nombre del piloto
    private final TextField campoNombre = new TextField();
    //ComboBox (lista desplegable) donde se elige el equipo del piloto, entre los equipos ya registrados
    private final ComboBox<String> comboEquipo = new ComboBox<>();
    //ComboBox donde se elige el rol del piloto (Titular, Reserva, etc.)
    private final ComboBox<RolPiloto> comboRol = new ComboBox<>();
    //Campo de texto para los años de experiencia del piloto
    private final TextField campoExperiencia = new TextField();
    //Campo de texto para la habilidad del piloto en clima seco (1 a 100)
    private final TextField campoSeco = new TextField();
    //Campo de texto para la habilidad del piloto en lluvia (1 a 100)
    private final TextField campoLluvia = new TextField();
    //Campo de texto para la habilidad del piloto en clima extremo (1 a 100)
    private final TextField campoExtremo = new TextField();
    //Campo de texto para la habilidad del piloto en curva (1 a 100)
    private final TextField campoCurva = new TextField();
    //Campo de texto para la habilidad de adelantamiento del piloto (1 a 100)
    private final TextField campoAdelantamiento = new TextField();
    //Campo de texto para la habilidad del piloto en recta (1 a 100)
    private final TextField campoRecta = new TextField();
    //Etiqueta donde se muestran los mensajes de exito o de error
    private final Label mensaje = new Label();

    //Muestra en chiquito la foto que se eligio (sea subida o un avatar predeterminado)
    private final ImageView vistaPrevia = new ImageView();
    //Lista con todas las "cajitas" clickeables de los avatares predeterminados, para poder resaltar la elegida
    private final List<StackPane> opcionesImagen = new ArrayList<>();
    //Guarda la URL de la imagen que se selecciono (null si todavia no se eligio ninguna)
    private String imagenSeleccionadaUrl = null;
    //Guarda el ID del piloto que esta cargado en el formulario en este momento; -1 significa que no hay ninguno cargado todavia
    private int idCargado = -1;

    //Constructor
    //Arma toda la pantalla: la lista buscable a la izquierda y el formulario con los datos del piloto a la derecha
    public PilotosEditarPane() {
        setSpacing(20);

        Label titulo = new Label("Editar piloto");
        titulo.getStyleClass().add("titulo-seccion");

        //Le mete scroll a la columna de la lista, para que si hay muchos pilotos se pueda desplazar
        ScrollPane scrollLista = new ScrollPane(columnaLista);
        scrollLista.setFitToWidth(true);
        scrollLista.setPrefHeight(620);
        scrollLista.getStyleClass().add("scroll-oscuro");
        //Deja el fondo transparente para que se vea el fondo de la pantalla
        scrollLista.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox panel = new VBox(22);
        panel.getStyleClass().add("panel-glow");
        panel.setPadding(new Insets(30));

        campoId.getStyleClass().add("campo-texto");
        campoId.setPromptText("Seleccione un piloto de la lista");
        //El ID no se puede escribir a mano, solo se llena al elegir un piloto de la lista
        campoId.setEditable(false);

        campoNombre.getStyleClass().add("campo-texto");
        campoNombre.setPromptText("Nombre completo del piloto");

        comboEquipo.getStyleClass().add("combo-oscuro");
        comboEquipo.setMaxWidth(Double.MAX_VALUE);
        comboEquipo.setPromptText("Seleccione un equipo");
        //Llena el ComboBox de equipos con los equipos que ya existen en el DataStore
        actualizarEquipos();

        comboRol.getStyleClass().add("combo-oscuro");
        comboRol.setMaxWidth(Double.MAX_VALUE);
        //Llena el ComboBox de roles con todos los valores posibles del enum RolPiloto
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

        //Cuadricula con 2 columnas para acomodar todos los campos del formulario y que ocupe menos espacio vertical
        GridPane grilla = new GridPane();
        grilla.setHgap(24);
        grilla.setVgap(16);
        grilla.getColumnConstraints().addAll(columnaFlexible(), columnaFlexible());

        //Acomoda cada campo en su celda de la cuadricula (columna, fila)
        grilla.add(campoConEtiqueta("ID", campoId), 0, 0);
        grilla.add(campoConEtiqueta("Nombre", campoNombre), 1, 0);
        grilla.add(campoConEtiqueta("Equipo", comboEquipo), 0, 1);
        grilla.add(campoConEtiqueta("Rol", comboRol), 1, 1);
        grilla.add(campoConEtiqueta("Años de experiencia", campoExperiencia), 0, 2);
        grilla.add(campoConEtiqueta("Habilidad en seco (1-100)", campoSeco), 1, 2);
        grilla.add(campoConEtiqueta("Habilidad en lluvia (1-100)", campoLluvia), 0, 3);
        grilla.add(campoConEtiqueta("Habilidad en clima extremo (1-100)", campoExtremo), 1, 3);
        grilla.add(campoConEtiqueta("Habilidad en curva (1-100)", campoCurva), 0, 4);
        grilla.add(campoConEtiqueta("Habilidad de adelantamiento (1-100)", campoAdelantamiento), 1, 4);
        grilla.add(campoConEtiqueta("Habilidad en recta (1-100)", campoRecta), 0, 5);

        VBox seccionImagen = construirSeccionImagen();

        //Boton para guardar los cambios hechos en el formulario
        Button guardar = new Button("GUARDAR CAMBIOS");
        guardar.getStyleClass().add("boton-primario");
        guardar.setOnAction(e -> guardar());

        //Boton para cancelar: deja el formulario vacio de nuevo, sin guardar ningun cambio
        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> limpiar());

        HBox botones = new HBox(14, guardar, cancelar);
        botones.setAlignment(Pos.CENTER_LEFT);

        panel.getChildren().addAll(grilla, new Separator(), seccionImagen, mensaje, botones);

        //Cada vez que el usuario escribe algo en el buscador, se vuelve a armar la lista con el filtro nuevo
        busqueda.getCampoTexto().textProperty().addListener((obs, viejo, nuevo) -> actualizarLista(nuevo));

        //Columna de la izquierda con el buscador arriba y la lista con scroll debajo
        VBox columnaIzquierda = new VBox(16, busqueda, scrollLista);
        columnaIzquierda.setPrefWidth(400);
        columnaIzquierda.setMinWidth(340);
        columnaIzquierda.setMaxWidth(420);

        //El panel del formulario crece para ocupar todo el espacio que sobre al lado de la lista
        HBox.setHgrow(panel, Priority.ALWAYS);
        HBox contenido = new HBox(24, columnaIzquierda, panel);

        getChildren().addAll(titulo, contenido);
        //Arma la lista de pilotos sin ningun filtro al inicio
        actualizarLista("");
    }

    //Crea una columna de la cuadricula que ocupa el 50% del ancho disponible, para que las 2 columnas queden parejas
    private javafx.scene.layout.ColumnConstraints columnaFlexible() {
        var columna = new javafx.scene.layout.ColumnConstraints();
        columna.setPercentWidth(50);
        return columna;
    }

    //Arma una cajita vertical con la etiqueta arriba y el campo (o combo) debajo, para que cada campo del formulario se vea igual
    private VBox campoConEtiqueta(String etiqueta, Control campo) {
        return new VBox(6, etiqueta(etiqueta), campo);
    }

    //Crea una etiqueta con el estilo de "etiqueta-campo" ya aplicado, para no repetir esas 2 lineas en cada campo
    private Label etiqueta(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("etiqueta-campo");
        return label;
    }

    //Arma toda la seccion de la foto: el boton para subir una imagen del PC y la fila de avatares predeterminados para elegir
    private VBox construirSeccionImagen() {
        Label tituloSeccion = new Label("Foto del piloto");
        tituloSeccion.getStyleClass().add("titulo-seccion");

        vistaPrevia.setFitWidth(64);
        vistaPrevia.setFitHeight(64);
        vistaPrevia.setPreserveRatio(false);

        //Boton que abre el explorador de archivos para elegir una foto del computador
        Button botonSubir = new Button("SUBIR IMAGEN DEL PC");
        botonSubir.getStyleClass().add("boton-secundario");
        botonSubir.setOnAction(e -> subirImagenDesdePc());

        HBox filaSubida = new HBox(16, botonSubir, vistaPrevia);
        filaSubida.setAlignment(Pos.CENTER_LEFT);

        Label etiquetaAvatares = new Label("O elige un avatar predeterminado:");
        etiquetaAvatares.getStyleClass().add("etiqueta-campo");

        //Fila que va acomodando los avatares uno al lado del otro y salta de linea si no caben todos
        FlowPane filaAvatares = new FlowPane(12, 12);
        //Recorre cada archivo de avatar predeterminado y arma su cajita clickeable
        for (String archivo : AVATARES_PREDETERMINADOS) {
            filaAvatares.getChildren().add(construirOpcionAvatar(archivo));
        }

        return new VBox(14, tituloSeccion, filaSubida, etiquetaAvatares, filaAvatares);
    }

    //Arma la cajita clickeable de un avatar predeterminado: la miniatura y lo que pasa cuando se hace click en ella
    private StackPane construirOpcionAvatar(String archivo) {
        //Carga la miniatura del avatar desde la carpeta de avatares predeterminados
        Image miniatura = GestorImagenes.cargar("avatars predeterminados/" + archivo);
        ImageView vista = new ImageView(miniatura);
        vista.setFitWidth(56);
        vista.setFitHeight(56);
        vista.setPreserveRatio(false);

        StackPane opcion = new StackPane(vista);
        opcion.getStyleClass().add("opcion-clima");
        opcion.setPrefSize(76, 76);
        //Al hacer click en el avatar, lo guarda como la imagen elegida, actualiza la vista previa y lo marca como seleccionado
        opcion.setOnMouseClicked(e -> {
            imagenSeleccionadaUrl = GestorImagenes.urlDe("avatars predeterminados/" + archivo);
            vistaPrevia.setImage(miniatura);
            marcarSeleccionada(opcion);
        });
        opcionesImagen.add(opcion);
        return opcion;
    }

    //Abre el explorador de archivos del sistema para que el usuario elija una foto desde su computador
    private void subirImagenDesdePc() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Selecciona la foto del piloto");
        //Solo deja elegir archivos de imagen (png, jpg, jpeg)
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = selector.showOpenDialog(getScene() != null ? getScene().getWindow() : null);
        //Si el usuario cerro la ventana sin elegir nada, no hace nada mas
        if (archivo == null) {
            return;
        }
        //Guarda la ruta del archivo elegido como URL y la muestra en la vista previa
        imagenSeleccionadaUrl = archivo.toURI().toString();
        vistaPrevia.setImage(new Image(imagenSeleccionadaUrl, 64, 64, false, true));
        //Como la foto vino de un archivo subido, ningun avatar predeterminado queda marcado
        marcarSeleccionada(null);
    }

    //Resalta la opcion de avatar elegida (o ninguna, si la foto vino de subir un archivo)
    private void marcarSeleccionada(StackPane elegida) {
        //Recorre todas las opciones de avatar y les pone el estilo de "seleccionada" solo a la que coincide con la elegida
        for (StackPane opcion : opcionesImagen) {
            opcion.getStyleClass().setAll(opcion == elegida ? "opcion-clima-seleccionada" : "opcion-clima");
        }
    }

    //Vuelve a llenar el ComboBox de equipos con los nombres de los equipos que hay actualmente en el DataStore
    private void actualizarEquipos() {
        var nombres = DataStore.getInstancia().getEquipos().stream()
                .map(com.f1manager.dominio.modelo.Equipo::getNombre).toList();
        comboEquipo.setItems(FXCollections.observableArrayList(nombres));
    }

    //Vuelve a armar la lista de pilotos filtrando por lo que se haya escrito en el buscador
    private void actualizarLista(String filtro) {
        columnaLista.getChildren().clear();
        //Si el filtro viene nulo lo deja vacio, y si no lo pasa a minusculas y le quita espacios de mas
        String texto = filtro == null ? "" : filtro.trim().toLowerCase();
        //Filtra la lista de pilotos comparando el texto de busqueda contra el ID, el nombre o el equipo de cada piloto
        var pilotos = DataStore.getInstancia().getPilotos().stream()
                .filter(p -> texto.isEmpty()
                        || String.valueOf(p.getId()).contains(texto)
                        || p.getNombre().toLowerCase().contains(texto)
                        || p.getEquipo().toLowerCase().contains(texto))
                .toList();
        //Si no hay pilotos (o no hay ninguno que coincida con la busqueda), muestra un mensaje avisando
        if (pilotos.isEmpty()) {
            Label vacio = new Label(texto.isEmpty() ? "No hay pilotos registrados."
                    : "No se encontraron pilotos para \"" + filtro.trim() + "\".");
            vacio.getStyleClass().add("texto-secundario");
            columnaLista.getChildren().add(vacio);
            return;
        }
        //Recorre cada piloto que paso el filtro y arma su fila en la lista
        for (Piloto p : pilotos) {
            //Icono/avatar redondo con la foto del piloto
            StackPane avatar = IconFactory.avatarPiloto(p, 34);
            //Texto con el ID, nombre, equipo y rol del piloto
            Label linea = new Label(String.format("ID %d   %s   ·   %s   ·   %s",
                    p.getId(), p.getNombre(), p.getEquipo(), p.getRol().getEtiqueta()));
            linea.getStyleClass().add("texto-normal");
            HBox fila = new HBox(12, avatar, linea);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getStyleClass().add("fila-lista");
            //Al hacer click en la fila, carga los datos de ese piloto en el formulario para poder editarlos
            fila.setOnMouseClicked(e -> cargar(p));
            columnaLista.getChildren().add(fila);
        }
    }

    //Toma un piloto de la lista y llena todo el formulario con sus datos actuales, para poder modificarlos
    private void cargar(Piloto piloto) {
        //Vuelve a refrescar la lista de equipos por si se agrego uno nuevo mientras esta pantalla estaba abierta
        actualizarEquipos();
        //Guarda el ID del piloto que se esta editando, para saber a cual actualizar cuando se guarde
        idCargado = piloto.getId();
        campoId.setText(String.valueOf(piloto.getId()));
        campoNombre.setText(piloto.getNombre());
        comboEquipo.setValue(piloto.getEquipo());
        comboRol.setValue(piloto.getRol());
        campoExperiencia.setText(String.valueOf(piloto.getExperienciaAnios()));
        campoSeco.setText(String.valueOf(piloto.getHabilidadSeco()));
        campoLluvia.setText(String.valueOf(piloto.getHabilidadLluvia()));
        campoExtremo.setText(String.valueOf(piloto.getHabilidadExtremo()));
        campoCurva.setText(String.valueOf(piloto.getHabilidadCurva()));
        campoAdelantamiento.setText(String.valueOf(piloto.getHabilidadAdelantamiento()));
        campoRecta.setText(String.valueOf(piloto.getHabilidadRecta()));
        //Carga la imagen actual del piloto en la vista previa (o la deja vacia si el piloto no tiene imagen propia)
        imagenSeleccionadaUrl = piloto.getImagenUrl();
        vistaPrevia.setImage(imagenSeleccionadaUrl != null
                ? new Image(imagenSeleccionadaUrl, 64, 64, false, true) : null);
        marcarSeleccionada(null);
        mensaje.setText("");
    }

    //Intenta guardar los cambios hechos en el formulario sobre el piloto que esta cargado
    private void guardar() {
        try {
            //Si no hay ningun piloto cargado (idCargado sigue en -1), no se puede guardar nada, asi que avisa con un error
            if (idCargado < 0) {
                throw new ValidacionException("Seleccione un piloto de la lista para editarlo.");
            }
            //Le pasa el ID del piloto cargado y todos los campos del formulario al DataStore para que valide y aplique los cambios
            //(si algo esta vacio o fuera de rango, lanza ValidacionException)
            DataStore.getInstancia().editarPiloto(String.valueOf(idCargado),
                    campoNombre.getText(), comboEquipo.getValue(), comboRol.getValue(),
                    campoExperiencia.getText(),
                    campoCurva.getText(), campoAdelantamiento.getText(), campoRecta.getText(),
                    campoLluvia.getText(), campoSeco.getText(), campoExtremo.getText(),
                    imagenSeleccionadaUrl);
            //Si no hubo error, cambia el estilo del mensaje a exitoso y avisa que la edicion salio bien
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Piloto actualizado correctamente.");
            //Reproduce el sonido de confirmacion
            GestorSonido.reproducir("Confirmado sound.mp3");
            //Vuelve a armar la lista (con el filtro actual del buscador) para que se vean los datos actualizados
            actualizarLista(busqueda.getTexto());
        } catch (ValidacionException ex) {
            //Si algo salio mal, muestra el mensaje de error en rojo tipo "error-label" y reproduce el sonido de error
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
            GestorSonido.reproducir("Error sound.mp3");
        }
    }

    //Deja el formulario vacio y sin ningun piloto cargado, listo para empezar de nuevo
    private void limpiar() {
        idCargado = -1;
        campoId.clear();
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
        mensaje.setText("");
    }
}
