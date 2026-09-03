//Sub-pantalla para listar monoplazas: muestra los vehículos registrados
//con un ícono representativo (no depende de imágenes externas para
//funcionar) y, al seleccionar uno, muestra su información técnica ampliada.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.vehiculos;

//Trae la clase que guarda y maneja toda la información del programa (equipos, pilotos, vehiculos, etc), funciona como la "base de datos" en memoria
import com.f1manager.aplicacion.DataStore;
//Trae la clase Monoplaza, que es el objeto con los datos técnicos del vehículo
import com.f1manager.dominio.modelo.Monoplaza;
//Trae la clase Piloto, que es el objeto con los datos de un piloto
import com.f1manager.dominio.modelo.Piloto;
//Trae la fábrica de iconos, de aca se sacan todos los dibujitos/logos que se usan en la pantalla
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae Insets, que sirve para poner márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (centrado, izquierda, etc)
import javafx.geometry.Pos;
//Trae Label, que es un texto que se muestra en pantalla (no editable)
import javafx.scene.control.Label;
//Trae ScrollPane, un contenedor con barra de scroll para cuando la lista no cabe en la pantalla
import javafx.scene.control.ScrollPane;
//Trae Separator, que es una línea divisoria para separar secciones visualmente
import javafx.scene.control.Separator;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila horizontal)
import javafx.scene.layout.HBox;
//Trae Priority, que sirve para decirle a un contenedor cuánto espacio extra debe tomar un elemento (ALWAYS/NEVER)
import javafx.scene.layout.Priority;
//Trae StackPane, un contenedor que apila los elementos uno encima del otro (útil para poner un solo panel que se va reemplazando)
import javafx.scene.layout.StackPane;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna vertical)
import javafx.scene.layout.VBox;

//Importa la interfaz List, que define el comportamiento general de una lista en Java
import java.util.List;
//Trae Collectors, que sirve para juntar los elementos de un stream en un resultado final (aca se usa para unir los nombres de pilotos en un solo texto)
import java.util.stream.Collectors;

//Clase publica llamada "VehiculosListarPane" que hereda de HBox (osea que ella misma es una fila donde a la izquierda va la lista y a la derecha el detalle)
public class VehiculosListarPane extends HBox {

    //Columna donde se van poniendo, una debajo de otra, las filas con todos los monoplazas registrados
    private final VBox columnaLista = new VBox(12);
    //Panel de la derecha donde se muestra el detalle del monoplaza seleccionado (o el mensaje de "selecciona uno")
    private final StackPane panelDetalle = new StackPane();
    //Guarda cuál fila de la lista está actualmente resaltada, para poder quitarle el resaltado cuando se selecciona otra
    private VBox filaSeleccionada;

    //Constructor
    //Arma la pantalla completa: la lista con scroll a la izquierda y el panel de detalle a la derecha, y carga todos los monoplazas existentes
    public VehiculosListarPane() {
        setSpacing(28);

        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        // Ancho proporcional al del panel completo (no un valor fijo), para que la
        // lista y el detalle mantengan una proporción equilibrada sin importar cuánto
        // espacio termine sobrando (antes el detalle se quedaba con todo lo restante).
        scroll.prefWidthProperty().bind(widthProperty().multiply(0.42));
        scroll.setMinWidth(340);
        scroll.setPrefHeight(560);

        panelDetalle.getStyleClass().add("panel");
        panelDetalle.setPrefSize(560, 560);
        panelDetalle.setPadding(new Insets(28));
        mostrarMensajeVacio();

        HBox.setHgrow(scroll, Priority.NEVER);
        HBox.setHgrow(panelDetalle, Priority.ALWAYS);
        getChildren().addAll(scroll, panelDetalle);

        //Se arma una fila por cada monoplaza registrado en el sistema
        for (Monoplaza m : DataStore.getInstancia().getVehiculos()) {
            columnaLista.getChildren().add(construirFila(m));
        }
    }

    //Arma una fila de la lista con el icono del equipo dueño del monoplaza, su modelo y su equipo+motor
    private VBox construirFila(Monoplaza m) {
        StackPane icono = new StackPane(IconFactory.monoplazaDeEquipo(m.getEquipo(), 60));
        icono.setPrefSize(70, 40);
        icono.setMinSize(70, 40);
        icono.setMaxSize(70, 40);

        Label nombre = new Label(m.getModelo());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5f6fa;");

        Label detalle = new Label(m.getEquipo() + "  ·  " + m.getMotor());
        detalle.getStyleClass().add("texto-secundario");

        VBox textos = new VBox(4, nombre, detalle);
        HBox fila = new HBox(14, icono, textos);
        fila.setAlignment(Pos.CENTER_LEFT);

        VBox contenedorFila = new VBox(fila);
        contenedorFila.getStyleClass().add("fila-lista");
        contenedorFila.setOnMouseClicked(e -> seleccionar(m, contenedorFila));
        return contenedorFila;
    }

    //Marca visualmente la fila clickeada como seleccionada (y le quita el resaltado a la anterior) y muestra su detalle
    private void seleccionar(Monoplaza m, VBox fila) {
        if (filaSeleccionada != null) {
            filaSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
            filaSeleccionada.getStyleClass().add("fila-lista");
        }
        fila.getStyleClass().remove("fila-lista");
        fila.getStyleClass().add("fila-lista-seleccionada");
        filaSeleccionada = fila;
        mostrarDetalle(m);
    }

    //Muestra el mensaje inicial en el panel de detalle, antes de que el usuario seleccione algún monoplaza
    private void mostrarMensajeVacio() {
        Label mensaje = new Label("Selecciona un monoplaza de la lista\npara ver su información técnica.");
        mensaje.getStyleClass().add("texto-secundario");
        mensaje.setStyle("-fx-text-alignment: center;");
        panelDetalle.getChildren().setAll(mensaje);
    }

    //Arma y muestra en el panel de detalle toda la información técnica del monoplaza seleccionado
    private void mostrarDetalle(Monoplaza m) {
        StackPane imagen = new StackPane(IconFactory.monoplazaDeEquipo(m.getEquipo(), 380));
        imagen.setAlignment(Pos.CENTER);

        Label titulo = new Label(m.getModelo());
        titulo.getStyleClass().add("titulo-seccion");

        Label equipo = new Label(m.getEquipo());
        equipo.getStyleClass().add("texto-rojo");

        VBox encabezado = new VBox(10, imagen, titulo, equipo);
        encabezado.setAlignment(Pos.CENTER);

        Label motor = new Label("Motor: " + m.getMotor());
        Label velocidad = new Label(String.format("Velocidad máxima: %.0f km/h", m.getVelocidadMaxKmh()));
        Label aceleracion = new Label(String.format("Aceleración 0-100 km/h: %.1f s", m.getAceleracion0a100()));
        Label carga = new Label("Carga aerodinámica actual: " + m.getCargaAerodinamica().getEtiqueta());
        Label modo = new Label("Modo de conducción actual: " + m.getModoConduccion().getEtiqueta());
        Label neumatico = new Label("Neumático actual: " + m.getTipoNeumatico().getEtiqueta());
        Label presion = new Label(String.format("Presión de aire: %.1f PSI", m.getPresionAire()));
        for (Label l : List.of(motor, velocidad, aceleracion, carga, modo, neumatico, presion)) {
            l.getStyleClass().add("texto-normal");
        }

        //Busca los pilotos del equipo dueño de este monoplaza y arma un solo texto con sus nombres y roles separados por comas
        List<Piloto> pilotosAsociados = DataStore.getInstancia().getPilotosPorEquipo(m.getEquipo());
        String textoPilotos = pilotosAsociados.isEmpty() ? "Sin pilotos asignados actualmente."
                : pilotosAsociados.stream().map(p -> p.getNombre() + " (" + p.getRol().getEtiqueta() + ")")
                        .collect(Collectors.joining(", "));
        Label pilotos = new Label("Pilotos asociados: " + textoPilotos);
        pilotos.getStyleClass().add("texto-secundario");
        pilotos.setWrapText(true);

        VBox contenido = new VBox(16, encabezado, new Separator(),
                motor, velocidad, aceleracion, carga, modo, neumatico, presion, pilotos);
        panelDetalle.getChildren().setAll(contenido);
    }
}
