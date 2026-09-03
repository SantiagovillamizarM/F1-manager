//Pantalla "Editar circuito": se elige un circuito de la lista (o se
//ingresa su ID) para cargar sus datos actuales en el formulario; al
//guardar, los cambios se validan y se guardan tanto en memoria como
//en MySQL.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.circuitos;

//Trae el DataStore, que es donde se guarda toda la información del programa (circuitos, pilotos, equipos, etc.)
import com.f1manager.aplicacion.DataStore;
//Trae ValidacionException, el error controlado que se lanza cuando el usuario escribe algo inválido
import com.f1manager.dominio.excepcion.ValidacionException;
//Trae Circuito, la clase que representa un circuito (nombre, país, longitud, vueltas, descripción)
import com.f1manager.dominio.modelo.Circuito;
//Trae CampoBusqueda, un componente reutilizable que junta una casilla de texto con estilo de búsqueda
import com.f1manager.infraestructura.ui.components.CampoBusqueda;
//Trae GestorSonido, la clase que reproduce los sonidos de confirmación o de error
import com.f1manager.infraestructura.ui.util.GestorSonido;
//Trae Insets, que sirve para dejar márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (por ejemplo, a la izquierda)
import javafx.geometry.Pos;
//Trae de un solo golpe todos los controles de JavaFX que se usan aquí (Button, Label, TextField, TextArea, etc.)
import javafx.scene.control.*;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila)
import javafx.scene.layout.HBox;
//Trae Priority, que sirve para decirle a un contenedor cuánto espacio extra debe tomar un elemento
import javafx.scene.layout.Priority;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna)
import javafx.scene.layout.VBox;

//Clase publica llamada "CircuitosEditarPane" que hereda de VBox (osea que ella misma es una columna de elementos)
public class CircuitosEditarPane extends VBox {

    //Columna donde se van agregando las filas de la lista de circuitos (se recalcula cada vez que cambia el filtro)
    private final VBox columnaLista = new VBox(10);
    //Casilla de búsqueda para filtrar la lista de circuitos por ID, nombre o país
    private final CampoBusqueda busqueda = new CampoBusqueda("Buscar por ID, nombre o país...");

    //Casilla de solo lectura que muestra el ID del circuito que está cargado en el formulario
    private final TextField campoId = new TextField();
    //Casilla de texto donde se edita el nombre del circuito
    private final TextField campoNombre = new TextField();
    //Casilla de texto donde se edita el país del circuito
    private final TextField campoPais = new TextField();
    //Casilla de texto donde se edita la longitud de la vuelta en kilómetros
    private final TextField campoLongitud = new TextField();
    //Casilla de texto donde se edita el número de vueltas de la carrera
    private final TextField campoVueltas = new TextField();
    //Área de texto (más grande que un TextField) donde se edita la descripción del circuito
    private final TextArea campoDescripcion = new TextArea();
    //Texto donde se muestran los mensajes de error o de confirmación
    private final Label mensaje = new Label();

    //Guarda el ID del circuito que está cargado actualmente en el formulario; -1 significa que todavía no se ha elegido ninguno
    private int idCargado = -1;

    //Constructor
    //Arma toda la pantalla: la lista con búsqueda a la izquierda y el formulario de edición a la derecha
    public CircuitosEditarPane() {
        //Deja 20 pixeles de espacio entre cada elemento de la columna
        setSpacing(20);

        //Crea el texto del título de la pantalla
        Label titulo = new Label("Editar circuito");
        titulo.getStyleClass().add("titulo-seccion");

        //Envuelve la columna de la lista en un ScrollPane para que se pueda desplazar si hay muchos circuitos
        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(560);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        //Panel con el estilo visual de "panel-glow" que envuelve todo el formulario
        VBox panelFormulario = new VBox(16);
        panelFormulario.getStyleClass().add("panel-glow");
        panelFormulario.setPadding(new Insets(30));

        campoId.getStyleClass().add("campo-texto");
        campoId.setPromptText("Seleccione un circuito de la lista");
        //No se puede editar directamente: el ID solo se llena al elegir un circuito de la lista
        campoId.setEditable(false);

        campoNombre.getStyleClass().add("campo-texto");
        campoPais.getStyleClass().add("campo-texto");
        campoLongitud.getStyleClass().add("campo-texto");
        campoVueltas.getStyleClass().add("campo-texto");

        campoDescripcion.getStyleClass().add("campo-area");
        campoDescripcion.setPrefRowCount(4);
        campoDescripcion.setWrapText(true);

        mensaje.setWrapText(true);

        //Botón que intenta guardar los cambios hechos sobre el circuito cargado
        Button guardar = new Button("GUARDAR CAMBIOS");
        guardar.getStyleClass().add("boton-primario");
        guardar.setOnAction(e -> guardar());

        //Botón que limpia el formulario y quita el circuito cargado, sin guardar nada
        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> limpiar());

        //Fila con los dos botones, uno al lado del otro
        HBox botones = new HBox(14, guardar, cancelar);
        botones.setAlignment(Pos.CENTER_LEFT);

        //Agrega al formulario, en orden, cada etiqueta con su casilla correspondiente, el mensaje y los botones
        panelFormulario.getChildren().addAll(
                etiquetaCampo("ID seleccionado"), campoId,
                etiquetaCampo("Nombre"), campoNombre,
                etiquetaCampo("País"), campoPais,
                etiquetaCampo("Longitud (km)"), campoLongitud,
                etiquetaCampo("Vueltas"), campoVueltas,
                etiquetaCampo("Descripción"), campoDescripcion,
                mensaje, botones
        );

        //Cada vez que el texto de la casilla de búsqueda cambia, se vuelve a armar la lista con el nuevo filtro
        busqueda.getCampoTexto().textProperty().addListener((obs, viejo, nuevo) -> actualizarLista(nuevo));

        //Columna de la izquierda con la búsqueda y la lista, con un ancho fijo para que no se coma el espacio del formulario
        VBox columnaIzquierda = new VBox(16, busqueda, scroll);
        columnaIzquierda.setPrefWidth(400);
        columnaIzquierda.setMinWidth(340);
        columnaIzquierda.setMaxWidth(420);

        //El formulario sí crece para ocupar todo el espacio que sobre al lado de la columna izquierda
        HBox.setHgrow(panelFormulario, Priority.ALWAYS);
        HBox contenido = new HBox(24, columnaIzquierda, panelFormulario);

        getChildren().addAll(titulo, contenido);
        actualizarLista("");
    }

    //Crea una etiqueta de texto con el estilo de "etiqueta-campo" para ponerla encima de cada casilla del formulario
    private Label etiquetaCampo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("etiqueta-campo");
        return label;
    }

    //Reconstruye la lista de circuitos filtrando por el texto ingresado (compara contra el ID, el nombre y el país).
    //Al hacer clic en una fila, se cargan los datos de ese circuito en el formulario para poder editarlos.
    private void actualizarLista(String filtro) {
        columnaLista.getChildren().clear();
        //Pasa el filtro a minúsculas (y evita null) para que la búsqueda no distinga mayúsculas de minúsculas
        String texto = filtro == null ? "" : filtro.trim().toLowerCase();
        //Trae todos los circuitos del DataStore y se queda solo con los que coinciden con el filtro
        var circuitos = DataStore.getInstancia().getCircuitos().stream()
                .filter(c -> texto.isEmpty()
                        || String.valueOf(c.getId()).contains(texto)
                        || c.getNombre().toLowerCase().contains(texto)
                        || c.getPais().toLowerCase().contains(texto))
                .toList();
        //Si no hay ningún circuito que coincida, muestra un aviso en vez de la lista
        if (circuitos.isEmpty()) {
            Label vacio = new Label(texto.isEmpty() ? "No hay circuitos registrados."
                    : "No se encontraron circuitos para \"" + filtro.trim() + "\".");
            vacio.getStyleClass().add("texto-secundario");
            columnaLista.getChildren().add(vacio);
            return;
        }
        //Por cada circuito que coincide, arma una fila con su información y la agrega a la columna
        for (Circuito c : circuitos) {
            Label linea = new Label(String.format("ID %d   %s   ·   %s   ·   %.3f km   ·   %d vueltas",
                    c.getId(), c.getNombre(), c.getPais(), c.getLongitudKm(), c.getVueltas()));
            linea.getStyleClass().add("texto-normal");
            VBox fila = new VBox(linea);
            fila.getStyleClass().add("fila-lista");
            //Al hacer clic en la fila, carga ese circuito completo en el formulario para editarlo
            fila.setOnMouseClicked(e -> cargar(c));
            columnaLista.getChildren().add(fila);
        }
    }

    //Copia todos los datos del circuito elegido dentro de las casillas del formulario, y guarda su ID
    //para saber, más adelante, cuál circuito hay que actualizar cuando se presione Guardar
    private void cargar(Circuito circuito) {
        idCargado = circuito.getId();
        campoId.setText(String.valueOf(circuito.getId()));
        campoNombre.setText(circuito.getNombre());
        campoPais.setText(circuito.getPais());
        campoLongitud.setText(String.valueOf(circuito.getLongitudKm()));
        campoVueltas.setText(String.valueOf(circuito.getVueltas()));
        campoDescripcion.setText(circuito.getDescripcion());
        mensaje.setText("");
    }

    //Intenta guardar los cambios hechos en el formulario sobre el circuito cargado. Si no se ha
    //cargado ningún circuito (idCargado sigue en -1), lanza el propio error de validación antes de
    //siquiera consultar al DataStore. Si el DataStore encuentra otro dato inválido, también se atrapa aquí.
    private void guardar() {
        try {
            //Si todavía no se seleccionó ningún circuito de la lista, no tiene sentido guardar nada
            if (idCargado < 0) {
                throw new ValidacionException("Seleccione un circuito de la lista para editarlo.");
            }
            //Le pide al DataStore que valide y actualice el circuito con ese ID usando los textos del formulario
            DataStore.getInstancia().editarCircuito(String.valueOf(idCargado),
                    campoNombre.getText(), campoPais.getText(),
                    campoLongitud.getText(), campoVueltas.getText(), campoDescripcion.getText());
            //Si todo salió bien, muestra el mensaje de éxito en rojo (el color que usa esta pantalla para confirmaciones)
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Circuito actualizado correctamente.");
            //Reproduce el sonido de confirmación
            GestorSonido.reproducir("Confirmado sound.mp3");
            //Vuelve a armar la lista para que se vean reflejados los cambios recién guardados
            actualizarLista(busqueda.getTexto());
        } catch (ValidacionException ex) {
            //Si algo salió mal, muestra el mensaje del error con el estilo de error y reproduce el sonido de error
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
            GestorSonido.reproducir("Error sound.mp3");
        }
    }

    //Vacía todas las casillas del formulario y olvida el circuito cargado, dejando la pantalla como recién abierta
    private void limpiar() {
        idCargado = -1;
        campoId.clear();
        campoNombre.clear();
        campoPais.clear();
        campoLongitud.clear();
        campoVueltas.clear();
        campoDescripcion.clear();
        mensaje.setText("");
    }
}
