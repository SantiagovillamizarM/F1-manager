//Sub-pantalla para listar equipos: a la izquierda muestra la lista de
//equipos registrados y, al hacer click en uno, a la derecha se ve su logo,
//sus pilotos y el monoplaza que tiene asignado.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.equipos;

//Trae la clase que guarda y maneja toda la información del programa (equipos, pilotos, vehiculos, etc), funciona como la "base de datos" en memoria
import com.f1manager.infraestructura.persistencia.DataStore;
//Trae la clase Equipo, que es el objeto con los datos de un equipo (nombre, pais, motor, etc)
import com.f1manager.dominio.modelo.Equipo;
//Trae la clase Monoplaza, que es el objeto con los datos técnicos del vehículo de un equipo
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
//Trae ScrollPane, un contenedor con barra de scroll para cuando la lista/detalle no cabe en la pantalla
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

//Clase publica llamada "EquiposListarPane" que hereda de HBox (osea que ella misma es una fila donde a la izquierda va la lista y a la derecha el detalle)
public class EquiposListarPane extends HBox {

    //Columna donde se van poniendo, una debajo de otra, las filas con todos los equipos registrados
    private final VBox columnaLista = new VBox(10);
    //Panel de la derecha donde se muestra el detalle del equipo seleccionado (o el mensaje de "selecciona uno")
    private final StackPane panelDetalle = new StackPane();
    //Guarda cuál fila de la lista está actualmente resaltada, para poder quitarle el resaltado cuando se selecciona otra
    private VBox filaSeleccionada;

    //Constructor
    //Arma la pantalla completa: la lista con scroll a la izquierda y el panel de detalle a la derecha, y carga todos los equipos existentes
    public EquiposListarPane() {
        setSpacing(28);

        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefWidth(360);
        scroll.setMinWidth(300);
        scroll.setPrefHeight(560);

        panelDetalle.getStyleClass().add("panel");
        panelDetalle.setPrefSize(560, 560);
        panelDetalle.setPadding(new Insets(28));
        mostrarMensajeSinSeleccion();

        HBox.setHgrow(scroll, Priority.NEVER);
        HBox.setHgrow(panelDetalle, Priority.ALWAYS);
        getChildren().addAll(scroll, panelDetalle);

        //Si no hay equipos registrados se avisa con un mensaje, si hay se arma una fila por cada uno
        var equipos = DataStore.getInstancia().getEquipos();
        if (equipos.isEmpty()) {
            Label vacio = new Label("No hay equipos registrados todavía.");
            vacio.getStyleClass().add("texto-secundario");
            columnaLista.getChildren().add(vacio);
        } else {
            for (Equipo equipo : equipos) {
                columnaLista.getChildren().add(construirFila(equipo));
            }
        }
    }

    //Arma una fila de la lista con el icono/logo del equipo, su nombre y su país+motor
    private VBox construirFila(Equipo equipo) {
        StackPane icono = IconFactory.contenedor(IconFactory.imagenEquipo(equipo, 40), 54);

        Label nombre = new Label(equipo.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5f6fa;");
        nombre.setWrapText(true);

        Label detalle = new Label(equipo.getPais() + "  ·  " + equipo.getMotor());
        detalle.getStyleClass().add("texto-secundario");

        VBox textos = new VBox(4, nombre, detalle);
        HBox fila = new HBox(14, icono, textos);
        fila.setAlignment(Pos.CENTER_LEFT);

        VBox contenedorFila = new VBox(fila);
        contenedorFila.getStyleClass().add("fila-lista");
        contenedorFila.setOnMouseClicked(e -> seleccionar(equipo, contenedorFila));
        return contenedorFila;
    }

    //Marca visualmente la fila clickeada como seleccionada (y le quita el resaltado a la anterior) y muestra su detalle
    private void seleccionar(Equipo equipo, VBox fila) {
        if (filaSeleccionada != null) {
            filaSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
            filaSeleccionada.getStyleClass().add("fila-lista");
        }
        fila.getStyleClass().remove("fila-lista");
        fila.getStyleClass().add("fila-lista-seleccionada");
        filaSeleccionada = fila;
        mostrarDetalle(equipo);
    }

    //Muestra el mensaje inicial en el panel de detalle, antes de que el usuario seleccione algún equipo
    private void mostrarMensajeSinSeleccion() {
        Label mensaje = new Label("Selecciona un equipo de la lista\npara ver sus pilotos y su monoplaza.");
        mensaje.getStyleClass().add("texto-secundario");
        mensaje.setStyle("-fx-text-alignment: center;");
        panelDetalle.getChildren().setAll(mensaje);
    }

    //Arma y muestra en el panel de detalle toda la información del equipo seleccionado: logo, nombre, país, motor, pilotos y vehículo
    private void mostrarDetalle(Equipo equipo) {
        StackPane logo = new StackPane(IconFactory.imagenEquipo(equipo, 90));
        logo.setAlignment(Pos.CENTER);

        Label nombre = new Label(equipo.getNombre());
        nombre.getStyleClass().add("titulo-seccion");

        Label paisMotor = new Label(equipo.getPais() + "  ·  Motor: " + equipo.getMotor());
        paisMotor.getStyleClass().add("texto-rojo");

        HBox encabezado = new HBox(18, logo, new VBox(6, nombre, paisMotor));
        encabezado.setAlignment(Pos.CENTER_LEFT);

        VBox seccionPilotos = construirSeccionPilotos(equipo);
        VBox seccionVehiculo = construirSeccionVehiculo(equipo);

        VBox contenido = new VBox(16, encabezado, new Separator(), seccionPilotos, new Separator(), seccionVehiculo);

        ScrollPane scrollDetalle = new ScrollPane(contenido);
        scrollDetalle.setFitToWidth(true);
        scrollDetalle.getStyleClass().add("scroll-oscuro");
        scrollDetalle.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        panelDetalle.getChildren().setAll(scrollDetalle);
    }

    //Arma la sección "Pilotos" del detalle: muestra cada piloto del equipo con su avatar, rol, experiencia y habilidad
    private VBox construirSeccionPilotos(Equipo equipo) {
        Label titulo = new Label("Pilotos");
        titulo.getStyleClass().add("etiqueta-campo");

        //Trae del DataStore la lista de pilotos que pertenecen a este equipo
        List<Piloto> pilotos = DataStore.getInstancia().getPilotosPorEquipo(equipo.getNombre());
        VBox seccion = new VBox(10, titulo);
        if (pilotos.isEmpty()) {
            Label vacio = new Label("Sin pilotos asignados actualmente.");
            vacio.getStyleClass().add("texto-secundario");
            seccion.getChildren().add(vacio);
            return seccion;
        }
        for (Piloto p : pilotos) {
            StackPane avatar = IconFactory.avatarPiloto(p, 64);

            Label nombre = new Label(p.getNombre() + "   ·   " + p.getRol().getEtiqueta());
            nombre.getStyleClass().add("texto-normal");
            nombre.setStyle("-fx-font-weight: bold;");

            Label habilidad = new Label(String.format("Experiencia: %d años   ·   Habilidad promedio: %.0f/100",
                    p.getExperienciaAnios(), p.getHabilidadPromedio()));
            habilidad.getStyleClass().add("texto-secundario");

            HBox fila = new HBox(12, avatar, new VBox(2, nombre, habilidad));
            fila.setAlignment(Pos.CENTER_LEFT);
            seccion.getChildren().add(fila);
        }
        return seccion;
    }

    //Arma la sección "Monoplaza" del detalle: muestra el modelo, motor y datos técnicos del vehículo asignado al equipo
    private VBox construirSeccionVehiculo(Equipo equipo) {
        Label titulo = new Label("Monoplaza");
        titulo.getStyleClass().add("etiqueta-campo");

        //Trae del DataStore el vehículo asignado a este equipo (puede no tener ninguno todavía)
        Monoplaza vehiculo = DataStore.getInstancia().getVehiculoPorEquipo(equipo.getNombre());
        if (vehiculo == null) {
            Label vacio = new Label("Sin monoplaza asignado actualmente.");
            vacio.getStyleClass().add("texto-secundario");
            return new VBox(10, titulo, vacio);
        }

        Label modelo = new Label(vehiculo.getModelo() + "   ·   Motor: " + vehiculo.getMotor());
        modelo.getStyleClass().add("texto-normal");
        modelo.setStyle("-fx-font-weight: bold;");

        Label rendimiento = new Label(String.format("Velocidad máxima: %.0f km/h   ·   Aceleración 0-100: %.1f s",
                vehiculo.getVelocidadMaxKmh(), vehiculo.getAceleracion0a100()));
        Label configuracion = new Label(String.format("Carga aerodinámica: %s   ·   Modo: %s   ·   Neumático: %s   ·   Presión: %.1f PSI",
                vehiculo.getCargaAerodinamica().getEtiqueta(), vehiculo.getModoConduccion().getEtiqueta(),
                vehiculo.getTipoNeumatico().getEtiqueta(), vehiculo.getPresionAire()));
        for (Label l : List.of(rendimiento, configuracion)) {
            l.getStyleClass().add("texto-secundario");
            l.setWrapText(true);
        }

        return new VBox(6, titulo, modelo, rendimiento, configuracion);
    }
}
