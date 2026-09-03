//Esta es la pantalla para eliminar un piloto: muestra la lista de pilotos
//(con buscador), se elige uno (o se escribe su ID a mano) y se borra.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.pilotos;

//Trae el DataStore, que es donde se guarda toda la información del juego (pilotos, equipos, etc.) en memoria
import com.f1manager.aplicacion.DataStore;
//Trae ValidacionException, el error controlado que se lanza cuando algo que escribio el usuario no es valido
import com.f1manager.dominio.excepcion.ValidacionException;
//Trae CampoBusqueda, el componente propio del proyecto con un campo de texto para filtrar/buscar
import com.f1manager.infraestructura.ui.components.CampoBusqueda;
//Trae Piloto, la clase que representa a un piloto con toda su informacion
import com.f1manager.dominio.modelo.Piloto;
//Trae IconFactory, que es la fabrica de iconos/avatares que se usan en la lista de pilotos
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae Insets, que sirve para poner margenes/rellenos alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para definir alineaciones (centrado, izquierda, etc.)
import javafx.geometry.Pos;
//Trae Button, el boton clickeable de JavaFX
import javafx.scene.control.Button;
//Trae Label, que es un componente de JavaFX para mostrar texto en pantalla (no se puede editar)
import javafx.scene.control.Label;
//Trae ScrollPane, un contenedor de JavaFX que le agrega una barra de scroll a lo que tenga adentro
import javafx.scene.control.ScrollPane;
//Trae TextField, el campo de texto de JavaFX donde el usuario puede escribir (por ejemplo, el ID a eliminar)
import javafx.scene.control.TextField;
//Trae HBox, que es un contenedor de JavaFX que acomoda sus elementos uno al lado del otro (en horizontal)
import javafx.scene.layout.HBox;
//Trae StackPane, un contenedor de JavaFX que apila sus elementos uno encima del otro (se usa para el avatar del piloto)
import javafx.scene.layout.StackPane;
//Trae VBox, que es un contenedor de JavaFX que acomoda sus elementos uno debajo del otro (en vertical)
import javafx.scene.layout.VBox;


//Clase publica que extiende de VBox, osea que esta pantalla ES un VBox (una caja vertical)
public class PilotosEliminarPane extends VBox {

    //Caja vertical donde se van agregando las filas de la lista de pilotos (una fila por piloto)
    private final VBox columnaLista = new VBox(10);
    //Campo de texto donde queda el ID del piloto que se va a eliminar (se llena solo al hacer click en una fila, o a mano)
    private final TextField campoId = new TextField();
    //Etiqueta donde se muestran los mensajes de exito o de error
    private final Label mensaje = new Label();
    //Campo de busqueda para filtrar la lista de pilotos por ID, nombre o equipo
    private final CampoBusqueda busqueda = new CampoBusqueda("Buscar por ID, nombre o equipo...");

    //Constructor
    //Arma toda la pantalla: el titulo, el buscador, la lista con scroll y el panel para confirmar la eliminacion
    public PilotosEliminarPane() {
        //Deja 20 pixeles de espacio entre cada elemento de la caja
        setSpacing(20);

        //Crea el texto del titulo de la pantalla
        Label titulo = new Label("Eliminar piloto");
        titulo.getStyleClass().add("titulo-seccion");

        //Le mete scroll a la columna de la lista, para que si hay muchos pilotos se pueda desplazar
        ScrollPane scroll = new ScrollPane(columnaLista);
        //Hace que el ancho del contenido se ajuste al ancho del scroll
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.getStyleClass().add("scroll-oscuro");
        //Deja el fondo transparente para que se vea el fondo de la pantalla
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        //Panel donde va el formulario para eliminar (el ID, el mensaje y los botones)
        VBox panelAccion = new VBox(12);
        panelAccion.getStyleClass().add("panel-glow");
        panelAccion.setPadding(new Insets(24));
        panelAccion.setMaxWidth(480);

        //Texto que le explica al usuario que tiene que escribir el ID del piloto
        Label etiqueta = new Label("Ingrese el ID del piloto a eliminar");
        etiqueta.getStyleClass().add("etiqueta-campo");

        campoId.getStyleClass().add("campo-texto");
        //Texto de ejemplo que se ve cuando el campo esta vacio
        campoId.setPromptText("Ej: 4");

        //Boton para confirmar la eliminacion del piloto
        Button eliminar = new Button("ELIMINAR");
        eliminar.getStyleClass().add("boton-primario");
        eliminar.setOnAction(e -> eliminar());

        //Boton para cancelar: limpia el campo de ID y el mensaje, sin eliminar nada
        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> {
            campoId.clear();
            mensaje.setText("");
        });

        //Le pone el estilo de mensaje de error por defecto (se cambia a "texto-rojo" si todo sale bien)
        mensaje.getStyleClass().add("error-label");
        mensaje.setWrapText(true);

        //Fila con los dos botones, uno al lado del otro
        HBox botones = new HBox(14, eliminar, cancelar);
        panelAccion.getChildren().addAll(etiqueta, campoId, mensaje, botones);

        //Cada vez que el usuario escribe algo en el buscador, se vuelve a armar la lista con el filtro nuevo
       busqueda.getCampoTexto().textProperty().addListener((obs, viejo, nuevo) -> actualizarLista(nuevo));

//Agrega el titulo, el buscador, la lista con scroll y el panel de accion a la pantalla, y arma la lista sin ningun filtro al inicio
getChildren().addAll(titulo, busqueda, scroll, panelAccion);
actualizarLista("");
    }

    //Vuelve a armar la lista de pilotos filtrando por lo que se haya escrito en el buscador
        private void actualizarLista(String filtro) {
        //Limpia la lista actual antes de volver a llenarla
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
            //Al hacer click en la fila, se copia el ID de ese piloto en el campo de texto de arriba
            fila.setOnMouseClicked(e -> campoId.setText(String.valueOf(p.getId())));
            columnaLista.getChildren().add(fila);
        }
    }
    //Intenta eliminar el piloto cuyo ID esta escrito en el campo de texto
    private void eliminar() {
        try {
            //Le pide al DataStore que elimine al piloto con ese ID (si el ID no es valido o no existe, lanza ValidacionException)
            DataStore.getInstancia().eliminarPiloto(campoId.getText());
            //Si no hubo error, cambia el estilo del mensaje a exitoso (texto rojo) y muestra que se elimino bien
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Piloto eliminado correctamente.");
            campoId.clear();
            //Vuelve a armar la lista (con el filtro actual del buscador) para que el piloto eliminado ya no aparezca
           actualizarLista(busqueda.getTexto());
        } catch (ValidacionException ex) {
            //Si algo salio mal (ID vacio, invalido o piloto inexistente), muestra el mensaje de error en rojo tipo "error-label"
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
        }
    }
}
