//Pantalla "Eliminar circuito": lista los circuitos actuales y deja
//eliminar uno ingresando (o seleccionando) su ID, validando los errores.

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
//Trae Insets, que sirve para dejar márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Button, un botón que se puede presionar
import javafx.scene.control.Button;
//Trae Label, que es un texto que se muestra en pantalla (no se puede editar)
import javafx.scene.control.Label;
//Trae ScrollPane, un contenedor que agrega una barra de desplazamiento cuando el contenido no cabe
import javafx.scene.control.ScrollPane;
//Trae TextField, una casilla de texto donde el usuario puede escribir
import javafx.scene.control.TextField;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila)
import javafx.scene.layout.HBox;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna)
import javafx.scene.layout.VBox;

//Clase publica llamada "CircuitosEliminarPane" que hereda de VBox (osea que ella misma es una columna de elementos)
public class CircuitosEliminarPane extends VBox {

    //Columna donde se van agregando las filas de la lista de circuitos (se recalcula cada vez que cambia el filtro)
    private final VBox columnaLista = new VBox(10);
    //Casilla donde se escribe (o se autocompleta al hacer clic en una fila) el ID del circuito a eliminar
    private final TextField campoId = new TextField();
    //Texto donde se muestran los mensajes de error o de confirmación
    private final Label mensaje = new Label();
    //Casilla de búsqueda para filtrar la lista de circuitos por ID, nombre o país
    private final CampoBusqueda busqueda = new CampoBusqueda("Buscar por ID, nombre o país...");

    //Constructor
    //Arma toda la pantalla: la lista con scroll, la casilla del ID y los botones de eliminar/cancelar
    public CircuitosEliminarPane() {
        //Deja 20 pixeles de espacio entre cada elemento de la columna
        setSpacing(20);

        //Crea el texto del título de la pantalla
        Label titulo = new Label("Eliminar circuito");
        titulo.getStyleClass().add("titulo-seccion");

        //Envuelve la columna de la lista en un ScrollPane para que se pueda desplazar si hay muchos circuitos
        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(420);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        //Panel donde van la casilla del ID, el mensaje y los botones de acción
        VBox panelAccion = new VBox(12);
        panelAccion.getStyleClass().add("panel-glow");
        panelAccion.setPadding(new Insets(24));
        panelAccion.setMaxWidth(480);

        //Etiqueta que indica qué se debe escribir en la casilla de abajo
        Label etiqueta = new Label("Ingrese el ID del circuito a eliminar");
        etiqueta.getStyleClass().add("etiqueta-campo");

        campoId.getStyleClass().add("campo-texto");
        campoId.setPromptText("Ej: 3");

        //Botón que ejecuta la eliminación del circuito
        Button eliminar = new Button("ELIMINAR");
        eliminar.getStyleClass().add("boton-primario");
        eliminar.setOnAction(e -> eliminar());

        //Botón que limpia la casilla del ID y el mensaje, sin eliminar nada
        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> {
            campoId.clear();
            mensaje.setText("");
        });

        mensaje.getStyleClass().add("error-label");
        mensaje.setWrapText(true);

        //Fila con los dos botones, uno al lado del otro
        HBox botones = new HBox(14, eliminar, cancelar);
        panelAccion.getChildren().addAll(etiqueta, campoId, mensaje, botones);

        //Cada vez que el texto de la casilla de búsqueda cambia, se vuelve a armar la lista con el nuevo filtro
        busqueda.getCampoTexto().textProperty().addListener((obs, viejo, nuevo) -> actualizarLista(nuevo));

getChildren().addAll(titulo, busqueda, scroll, panelAccion);
actualizarLista("");
    }

       //Reconstruye la lista de circuitos filtrando por el texto ingresado (compara contra el ID, el nombre y el país).
       //Al hacer clic en una fila, se copia el ID de ese circuito a la casilla de arriba para que quede listo para eliminar.
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
            //Al hacer clic en la fila, copia el ID de ese circuito en la casilla de texto de arriba
            fila.setOnMouseClicked(e -> campoId.setText(String.valueOf(c.getId())));
            columnaLista.getChildren().add(fila);
        }
    }
    //Intenta eliminar el circuito cuyo ID está en la casilla de texto. Si el ID no es válido o no
    //existe, el DataStore lanza un ValidacionException y ese error se muestra en el mensaje.
    private void eliminar() {
        try {
            //Le pide al DataStore que borre el circuito con ese ID (viene como texto y se valida adentro)
            DataStore.getInstancia().eliminarCircuito(campoId.getText());
            //Si todo salió bien, cambia el mensaje a color de éxito y avisa que se eliminó correctamente
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Circuito eliminado correctamente.");
            campoId.clear();
            //Vuelve a armar la lista para que el circuito eliminado ya no aparezca
            actualizarLista(busqueda.getTexto());
        } catch (ValidacionException ex) {
            //Si algo salió mal (ID vacío, no numérico o que no existe), muestra el error en rojo de aviso
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
        }
    }
}
