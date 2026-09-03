//Componente reutilizado por "Listar circuitos" y "Buscar circuito":
//muestra a la izquierda una lista interactiva de circuitos y a la
//derecha, al seleccionar uno, su descripción y una representación
//visual generada de la pista.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.circuitos;

//Trae Circuito, la clase que representa un circuito (nombre, país, longitud, vueltas, descripción)
import com.f1manager.dominio.modelo.Circuito;
//Trae PistaGenerador, la clase que dibuja una representación visual (un trazado) de un circuito
import com.f1manager.infraestructura.ui.util.PistaGenerador;
//Trae Insets, que sirve para dejar márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (por ejemplo, arriba a la izquierda)
import javafx.geometry.Pos;
//Trae Canvas, un lienzo en blanco donde se puede dibujar a mano (líneas, formas, etc.)
import javafx.scene.canvas.Canvas;
//Trae GraphicsContext, la "herramienta de dibujo" que se usa para pintar sobre un Canvas
import javafx.scene.canvas.GraphicsContext;
//Trae Label, que es un texto que se muestra en pantalla (no se puede editar)
import javafx.scene.control.Label;
//Trae ScrollPane, un contenedor que agrega una barra de desplazamiento cuando el contenido no cabe
import javafx.scene.control.ScrollPane;
//Trae de un solo golpe todos los contenedores de layout de JavaFX que se usan aquí (HBox, VBox, StackPane, Priority)
import javafx.scene.layout.*;

//Trae la interfaz List, que define el comportamiento general de una lista en Java
import java.util.List;

//Clase publica llamada "PanelListaCircuitos" que hereda de HBox (osea que ella misma es una fila con dos partes: lista y detalle)
public class PanelListaCircuitos extends HBox {

    //Columna donde se van agregando las filas de la lista de circuitos
    private final VBox columnaLista = new VBox(12);
    //Panel de la derecha donde se muestra el detalle del circuito seleccionado (o el mensaje de "selecciona uno")
    private final StackPane panelDetalle = new StackPane();
    //Texto a mostrar cuando la lista de circuitos está vacía (cambia según si es "Listar" o "Buscar")
    private final String mensajeListaVacia;
    //Guarda cuál fila está seleccionada actualmente, para poder quitarle el estilo de "seleccionada" si se elige otra
    private VBox filaSeleccionada;

    //Constructor
    //Arma el panel completo: la lista con scroll a la izquierda y el panel de detalle a la derecha, y carga la lista inicial
    public PanelListaCircuitos(List<Circuito> inicial, String mensajeListaVacia) {
        this.mensajeListaVacia = mensajeListaVacia;
        setSpacing(28);

        //Envuelve la columna de la lista en un ScrollPane para que se pueda desplazar si hay muchos circuitos
        ScrollPane scrollLista = new ScrollPane(columnaLista);
        scrollLista.setFitToWidth(true);
        scrollLista.getStyleClass().add("scroll-oscuro");
        scrollLista.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        // Ancho proporcional al del panel completo (no un valor fijo), para que la
        // lista y el detalle mantengan una proporción equilibrada sin importar cuánto
        // espacio termine sobrando (antes el detalle se quedaba con todo lo restante).
        scrollLista.prefWidthProperty().bind(widthProperty().multiply(0.42));
        scrollLista.setMinWidth(340);
        scrollLista.setPrefHeight(560);

        panelDetalle.getStyleClass().add("panel");
        panelDetalle.setPrefSize(560, 560);
        panelDetalle.setPadding(new Insets(24));
        //Al arrancar, todavía no hay ningún circuito seleccionado, así que muestra el mensaje por defecto
        mostrarMensajeSeleccionVacia();

        //La lista no crece si sobra espacio, pero el panel de detalle sí ocupa todo lo que quede libre
        HBox.setHgrow(scrollLista, Priority.NEVER);
        HBox.setHgrow(panelDetalle, Priority.ALWAYS);
        getChildren().addAll(scrollLista, panelDetalle);

        //Carga la lista inicial de circuitos que recibió el constructor
        actualizar(inicial);
    }

    //Reemplaza el conjunto de circuitos mostrados en la lista (lo usa la búsqueda para mostrar solo los resultados)
    public void actualizar(List<Circuito> circuitos) {
        columnaLista.getChildren().clear();
        //Al cambiar la lista, se pierde la selección anterior y vuelve a mostrarse el mensaje de "selecciona uno"
        filaSeleccionada = null;
        mostrarMensajeSeleccionVacia();

        //Si no hay circuitos que mostrar, pone un aviso en vez de la lista
        if (circuitos.isEmpty()) {
            Label vacio = new Label(mensajeListaVacia);
            vacio.getStyleClass().add("texto-secundario");
            columnaLista.getChildren().add(vacio);
            return;
        }

        //Por cada circuito recibido, arma su fila y la agrega a la columna de la lista
        for (Circuito circuito : circuitos) {
            columnaLista.getChildren().add(construirFila(circuito));
        }
    }

    //Arma la fila visual de un circuito: su nombre en negrita y una línea con el resto de sus datos
    private VBox construirFila(Circuito circuito) {
        Label nombre = new Label(circuito.getNombre());
        nombre.getStyleClass().add("texto-normal");
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label detalle = new Label(String.format("ID %d  ·  %s  ·  %.3f km  ·  %d vueltas",
                circuito.getId(), circuito.getPais(), circuito.getLongitudKm(), circuito.getVueltas()));
        detalle.getStyleClass().add("texto-secundario");

        VBox fila = new VBox(4, nombre, detalle);
        fila.getStyleClass().add("fila-lista");
        //Al hacer clic en la fila, selecciona ese circuito y muestra su detalle a la derecha
        fila.setOnMouseClicked(e -> seleccionar(circuito, fila));
        return fila;
    }

    //Marca la fila clickeada como seleccionada (le cambia el estilo visual), le quita la marca a la
    //fila que estaba seleccionada antes, y manda a mostrar el detalle del circuito elegido
    private void seleccionar(Circuito circuito, VBox fila) {
        if (filaSeleccionada != null) {
            filaSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
            filaSeleccionada.getStyleClass().add("fila-lista");
        }
        fila.getStyleClass().remove("fila-lista");
        fila.getStyleClass().add("fila-lista-seleccionada");
        filaSeleccionada = fila;

        mostrarDetalle(circuito);
    }

    //Muestra en el panel de la derecha el mensaje por defecto que invita a seleccionar un circuito de la lista
    private void mostrarMensajeSeleccionVacia() {
        Label mensaje = new Label("Selecciona un circuito de la lista\npara ver su información y trazado.");
        mensaje.getStyleClass().add("texto-secundario");
        mensaje.setStyle("-fx-text-alignment: center;");
        panelDetalle.getChildren().setAll(mensaje);
    }

    //Arma y muestra el detalle completo del circuito elegido: título, datos generales, el dibujo
    //de la pista (generado con PistaGenerador) y la descripción, todo dentro de un scroll por si no cabe
    private void mostrarDetalle(Circuito circuito) {
        Label titulo = new Label(circuito.getNombre());
        titulo.getStyleClass().add("titulo-seccion");

        Label subtitulo = new Label(circuito.getPais() + "  ·  " + circuito.getLongitudKm() + " km  ·  "
                + circuito.getVueltas() + " vueltas  ·  " + String.format("%.1f", circuito.getDistanciaTotalKm()) + " km totales");
        subtitulo.getStyleClass().add("texto-secundario");

        Label descripcion = new Label(circuito.getDescripcion());
        descripcion.getStyleClass().add("texto-normal");
        descripcion.setWrapText(true);

        //Crea el lienzo donde se va a dibujar el trazado del circuito
        Canvas lienzo = new Canvas(480, 300);
        //Obtiene la "herramienta de dibujo" de ese lienzo
        GraphicsContext gc = lienzo.getGraphicsContext2D();
        //Genera el trazado que le corresponde a este circuito en particular
        PistaGenerador pista = PistaGenerador.paraCircuito(circuito);
        //Dibuja la pista dentro del lienzo, ocupando todo su ancho y alto
        pista.dibujar(gc, 0, 0, lienzo.getWidth(), lienzo.getHeight(), true);

        VBox contenido = new VBox(14, titulo, subtitulo, lienzo, descripcion);
        contenido.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollDetalle = new ScrollPane(contenido);
        scrollDetalle.setFitToWidth(true);
        scrollDetalle.getStyleClass().add("scroll-oscuro");
        scrollDetalle.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        panelDetalle.getChildren().setAll(scrollDetalle);
    }
}
