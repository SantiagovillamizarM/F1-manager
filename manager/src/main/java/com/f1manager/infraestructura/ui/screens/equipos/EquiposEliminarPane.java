//Sub-pantalla para eliminar un equipo: muestra la lista de equipos
//registrados (con buscador) y, escribiendo o haciendo click en el nombre,
//permite borrarlo del sistema.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.equipos;

//Trae la clase que guarda y maneja toda la información del programa (equipos, pilotos, vehiculos, etc), funciona como la "base de datos" en memoria
import com.f1manager.infraestructura.persistencia.DataStore;
//Trae el error personalizado que se lanza cuando el usuario ingresa datos inválidos
import com.f1manager.dominio.excepcion.ValidacionException;
//Trae la clase Equipo, que es el objeto con los datos de un equipo (nombre, pais, motor, etc)
import com.f1manager.dominio.modelo.Equipo;
//Trae el campo de búsqueda personalizado que ya trae su propio TextField para escribir el filtro
import com.f1manager.infraestructura.ui.components.CampoBusqueda;
//Trae Insets, que sirve para poner márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Button, el botón que se puede clickear
import javafx.scene.control.Button;
//Trae Label, que es un texto que se muestra en pantalla (no editable)
import javafx.scene.control.Label;
//Trae ScrollPane, un contenedor con barra de scroll para cuando la lista no cabe en la pantalla
import javafx.scene.control.ScrollPane;
//Trae TextField, que es el cuadro de texto donde el usuario puede escribir
import javafx.scene.control.TextField;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila horizontal)
import javafx.scene.layout.HBox;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna vertical)
import javafx.scene.layout.VBox;

//Clase publica llamada "EquiposEliminarPane" que hereda de VBox (osea que ella misma es una columna donde se van poniendo los elementos)
public class EquiposEliminarPane extends VBox {

    //Columna donde se van poniendo, una debajo de otra, las filas con los equipos encontrados
    private final VBox columnaLista = new VBox(10);
    //Cuadro de texto donde el usuario escribe (o se autocompleta al hacer click en la lista) el nombre del equipo a eliminar
    private final TextField campoNombre = new TextField();
    //Label donde se muestra el resultado de la operación (si se eliminó bien o si hubo un error)
    private final Label mensaje = new Label();
    //Campo de búsqueda para filtrar la lista de equipos por nombre, país o motor
    private final CampoBusqueda busqueda = new CampoBusqueda("Buscar por nombre, país o motor...");

    //Constructor
    //Arma toda la pantalla: el título, la lista con scroll, el panel con el campo de nombre y los botones de eliminar/cancelar
    public EquiposEliminarPane() {
        setSpacing(20);

        Label titulo = new Label("Eliminar equipo");
        titulo.getStyleClass().add("titulo-seccion");

        //Scroll que envuelve la columna de la lista, por si hay muchos equipos y no caben en la altura fija
        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(360);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        //Panel (el cuadro con brillo) donde va el campo de nombre, el mensaje y los botones
        VBox panelAccion = new VBox(12);
        panelAccion.getStyleClass().add("panel-glow");
        panelAccion.setPadding(new Insets(24));
        panelAccion.setMaxWidth(480);

        Label etiqueta = new Label("Ingrese el nombre del equipo a eliminar");
        etiqueta.getStyleClass().add("etiqueta-campo");

        campoNombre.getStyleClass().add("campo-texto");
        campoNombre.setPromptText("Ej: Alpine");

        //Botón que dispara la eliminación del equipo escrito en campoNombre
        Button eliminar = new Button("ELIMINAR");
        eliminar.getStyleClass().add("boton-primario");
        eliminar.setOnAction(e -> eliminar());

        //Botón que simplemente limpia el campo y el mensaje, sin eliminar nada
        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> {
            campoNombre.clear();
            mensaje.setText("");
        });

        mensaje.getStyleClass().add("error-label");
        mensaje.setWrapText(true);

        HBox botones = new HBox(14, eliminar, cancelar);
        panelAccion.getChildren().addAll(etiqueta, campoNombre, mensaje, botones);

        //Cada vez que el usuario escribe algo en el campo de búsqueda, se vuelve a armar la lista filtrada
        busqueda.getCampoTexto().textProperty().addListener((obs, viejo, nuevo) -> actualizarLista(nuevo));

getChildren().addAll(titulo, busqueda, scroll, panelAccion);
//Se arma la lista completa (sin filtro) apenas se crea la pantalla
actualizarLista("");
    }

    //Este método reconstruye la columna de la lista de equipos según el texto del filtro que le llega
       private void actualizarLista(String filtro) {
        //Primero se borra todo lo que había en la lista para volver a armarla desde cero
        columnaLista.getChildren().clear();
        //Se normaliza el filtro: si viene nulo se usa texto vacío, y se pasa todo a minúsculas para que la búsqueda no distinga mayúsculas
        String texto = filtro == null ? "" : filtro.trim().toLowerCase();
        //Se filtran los equipos que coincidan con el texto buscado en el nombre, el país o el motor
        var equipos = DataStore.getInstancia().getEquipos().stream()
                .filter(eq -> texto.isEmpty()
                        || eq.getNombre().toLowerCase().contains(texto)
                        || eq.getPais().toLowerCase().contains(texto)
                        || eq.getMotor().toLowerCase().contains(texto))
                .toList();
        //Si no hay ningún equipo que coincida, se muestra un mensaje avisando (distinto según si había filtro o no)
        if (equipos.isEmpty()) {
            Label vacio = new Label(texto.isEmpty() ? "No hay equipos registrados."
                    : "No se encontraron equipos para \"" + filtro.trim() + "\".");
            vacio.getStyleClass().add("texto-secundario");
            columnaLista.getChildren().add(vacio);
            return;
        }
        //Por cada equipo encontrado se arma una fila con su info, y al hacer click en ella se copia el nombre al campo de texto
        for (Equipo eq : equipos) {
            Label linea = new Label(eq.getNombre() + "   ·   " + eq.getPais() + "   ·   " + eq.getMotor());
            linea.getStyleClass().add("texto-normal");
            VBox fila = new VBox(linea);
            fila.getStyleClass().add("fila-lista");
            fila.setOnMouseClicked(e -> campoNombre.setText(eq.getNombre()));
            columnaLista.getChildren().add(fila);
        }
    }

    //Este método intenta eliminar el equipo cuyo nombre está escrito en campoNombre
    private void eliminar() {
        try {
            //Le pide al DataStore que borre el equipo por nombre; si el nombre no existe o está mal, lanza ValidacionException
            DataStore.getInstancia().eliminarEquipo(campoNombre.getText());
            //Si todo salió bien se muestra el mensaje en rojo (positivo) y se limpia el campo
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Equipo eliminado correctamente.");
            campoNombre.clear();
//Se vuelve a armar la lista respetando el filtro que hubiera en el buscador, para que el equipo eliminado desaparezca
actualizarLista(busqueda.getTexto());
         } catch (ValidacionException ex) {
            //Si algo salió mal se muestra el mensaje de error (el texto que trae la excepción)
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
        }
    }
}
