//Este es el panel reutilizable con la lista de pilotos a la izquierda y la
//ficha ampliada (al estilo videojuego de gestión deportiva) a la derecha.
//Se usa tanto en la pantalla de Listar como en otras que necesiten mostrar la lista.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.pilotos;

//Trae Piloto, la clase que representa a un piloto con toda su informacion (habilidades, equipo, etc.)
import com.f1manager.dominio.modelo.Piloto;
//Trae IconFactory, que es la fabrica de iconos/avatares que se usan en la lista y en la ficha
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae Insets, que sirve para poner margenes/rellenos alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para definir alineaciones (centrado, izquierda, etc.)
import javafx.geometry.Pos;
//Trae Label, que es un componente de JavaFX para mostrar texto en pantalla (no se puede editar)
import javafx.scene.control.Label;
//Trae ProgressBar, el componente nativo de JavaFX que dibuja una barra de progreso/relleno
import javafx.scene.control.ProgressBar;
//Trae ScrollPane, un contenedor de JavaFX que le agrega una barra de scroll a lo que tenga adentro
import javafx.scene.control.ScrollPane;
//Trae todos los contenedores de layout de JavaFX (HBox, VBox, GridPane, StackPane, Region, Priority, etc.)
import javafx.scene.layout.*;

//Importa la interfaz List, que define el comportamiento general de una lista en Java
import java.util.List;

//Clase publica que extiende de HBox, osea que este panel ES una caja horizontal (lista a la izquierda, ficha a la derecha)
public class PanelListaPilotos extends HBox {

    //Caja vertical donde se van agregando las filas de la lista de pilotos (una fila por piloto)
    private final VBox columnaLista = new VBox(12);
    //Panel de la derecha donde se muestra la ficha ampliada del piloto seleccionado
    private final StackPane panelDetalle = new StackPane();
    //Guarda cual es la fila que esta actualmente seleccionada, para poder quitarle el resaltado cuando se elija otra
    private HBox filaSeleccionada;

    //Constructor
    //Arma el panel completo: la lista con scroll a la izquierda y el panel de la ficha a la derecha, y llena la lista con los pilotos recibidos
    public PanelListaPilotos(List<Piloto> pilotos) {
        setSpacing(28);

        //Le mete scroll a la columna de la lista, para que si hay muchos pilotos se pueda desplazar
        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-oscuro");
        //Deja el fondo transparente para que se vea el fondo de la pantalla
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        // Ancho proporcional al del panel completo (no un valor fijo), para que la
        // lista y la ficha mantengan una proporción equilibrada sin importar cuánto
        // espacio termine sobrando (antes la ficha se quedaba con todo lo restante).
        scroll.prefWidthProperty().bind(widthProperty().multiply(0.42));
        scroll.setMinWidth(340);
        scroll.setPrefHeight(560);

        panelDetalle.getStyleClass().add("panel");
        panelDetalle.setPrefSize(560, 560);
        panelDetalle.setPadding(new Insets(28));
        //Al arrancar todavia no hay ningun piloto seleccionado, asi que muestra el mensaje vacio
        mostrarMensajeVacio();

        //La lista no crece, pero el panel de la ficha si crece para ocupar todo el espacio que sobre
        HBox.setHgrow(scroll, Priority.NEVER);
        HBox.setHgrow(panelDetalle, Priority.ALWAYS);
        getChildren().addAll(scroll, panelDetalle);

        //Llena la lista con los pilotos que llegaron por parametro
        actualizar(pilotos);
    }

    //Vuelve a armar la lista de pilotos desde cero con la lista que le pasen (por ejemplo, cuando cambia la informacion guardada)
    public void actualizar(List<Piloto> pilotos) {
        columnaLista.getChildren().clear();
        //Como se borro la lista, ya no hay ninguna fila seleccionada
        filaSeleccionada = null;
        mostrarMensajeVacio();

        //Si no hay pilotos, muestra un mensaje avisando y no sigue
        if (pilotos.isEmpty()) {
            Label vacio = new Label("No hay pilotos registrados todavía.");
            vacio.getStyleClass().add("texto-secundario");
            columnaLista.getChildren().add(vacio);
            return;
        }
        //Recorre cada piloto y arma su fila para agregarla a la lista
        for (Piloto piloto : pilotos) {
            columnaLista.getChildren().add(construirFila(piloto));
        }
    }

    //Arma una fila de la lista con el avatar del piloto y sus datos basicos (nombre, ID, equipo, rol y habilidad promedio)
    private HBox construirFila(Piloto piloto) {
        //Icono/avatar redondo con la foto del piloto
        StackPane avatar = IconFactory.avatarPiloto(piloto, 46);

        Label nombre = new Label(piloto.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5f6fa;");

        Label detalle = new Label(String.format("ID %d  ·  %s  ·  %s  ·  Habilidad prom. %.0f/100",
                piloto.getId(), piloto.getEquipo(), piloto.getRol().getEtiqueta(), piloto.getHabilidadPromedio()));
        detalle.getStyleClass().add("texto-secundario");

        HBox fila = new HBox(14, avatar, new VBox(4, nombre, detalle));
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("fila-lista");
        //Al hacer click en la fila, selecciona ese piloto y muestra su ficha completa a la derecha
        fila.setOnMouseClicked(e -> seleccionar(piloto, fila));
        return fila;
    }

    //Marca la fila clickeada como seleccionada (le quita el resaltado a la anterior) y muestra la ficha del piloto elegido
    private void seleccionar(Piloto piloto, HBox fila) {
        //Si ya habia una fila seleccionada antes, le quita el estilo de "seleccionada" y le devuelve el estilo normal
        if (filaSeleccionada != null) {
            filaSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
            filaSeleccionada.getStyleClass().add("fila-lista");
        }
        //A la fila nueva le quita el estilo normal y le pone el estilo de "seleccionada"
        fila.getStyleClass().remove("fila-lista");
        fila.getStyleClass().add("fila-lista-seleccionada");
        filaSeleccionada = fila;
        mostrarFicha(piloto);
    }

    //Muestra el mensaje que invita a elegir un piloto, para cuando todavia no hay ninguno seleccionado
    private void mostrarMensajeVacio() {
        Label mensaje = new Label("Selecciona un piloto de la lista\npara ver su ficha completa.");
        mensaje.getStyleClass().add("texto-secundario");
        mensaje.setStyle("-fx-text-alignment: center;");
        panelDetalle.getChildren().setAll(mensaje);
    }

    //Arma y muestra la ficha completa del piloto seleccionado: avatar grande, datos y barras de habilidad
    private void mostrarFicha(Piloto piloto) {
        //Avatar grande del piloto, para la cabecera de la ficha
        StackPane avatar = IconFactory.avatarPiloto(piloto, 140);

        Label nombre = new Label(piloto.getNombre());
        nombre.getStyleClass().add("titulo-seccion");

        Label equipoRol = new Label(piloto.getEquipo() + "  ·  " + piloto.getRol().getEtiqueta());
        equipoRol.getStyleClass().add("texto-rojo");

        HBox encabezado = new HBox(20, avatar, new VBox(6, nombre, equipoRol));
        encabezado.setAlignment(Pos.CENTER_LEFT);

        //Cuadricula donde van los datos basicos (ID, experiencia, habilidad promedio, rol) en pares de columnas
        GridPane datos = new GridPane();
        datos.setHgap(30);
        datos.setVgap(14);
        datos.setPadding(new Insets(20, 0, 0, 0));

        agregarDato(datos, 0, "ID", String.valueOf(piloto.getId()));
        agregarDato(datos, 1, "Experiencia", piloto.getExperienciaAnios() + " años");
        agregarDato(datos, 2, "Habilidad prom.", String.format("%.0f / 100", piloto.getHabilidadPromedio()));
        agregarDato(datos, 3, "Rol", piloto.getRol().getEtiqueta());

        //Una barra de progreso por cada tipo de habilidad del piloto
        VBox barras = new VBox(14,
                construirBarraHabilidad("Habilidad promedio", piloto.getHabilidadPromedio()),
                construirBarraHabilidad("Habilidad en seco", piloto.getHabilidadSeco()),
                construirBarraHabilidad("Habilidad en lluvia", piloto.getHabilidadLluvia()),
                construirBarraHabilidad("Habilidad en clima extremo", piloto.getHabilidadExtremo()),
                construirBarraHabilidad("Habilidad en curva", piloto.getHabilidadCurva()),
                construirBarraHabilidad("Habilidad de adelantamiento", piloto.getHabilidadAdelantamiento()),
                construirBarraHabilidad("Habilidad en recta", piloto.getHabilidadRecta())
        );

        VBox contenido = new VBox(20, encabezado, datos, barras);
        panelDetalle.getChildren().setAll(contenido);
    }

    //Arma una barra de habilidad individual: la etiqueta con el valor arriba, y la barra de progreso rellena segun ese valor
    private VBox construirBarraHabilidad(String etiqueta, double valor) {
        VBox barra = new VBox(6);
        Label etiquetaBarra = new Label(etiqueta);
        etiquetaBarra.getStyleClass().add("etiqueta-campo");

        Label valorBarra = new Label(Math.round(valor) + " / 100");
        valorBarra.getStyleClass().add("texto-rojo");

        //Espacio flexible que empuja el valor hacia la derecha, dejando la etiqueta pegada a la izquierda
        Region espaciadorEncabezado = new Region();
        HBox.setHgrow(espaciadorEncabezado, Priority.ALWAYS);
        HBox encabezadoBarra = new HBox(etiquetaBarra, espaciadorEncabezado, valorBarra);

        // ProgressBar nativo en vez de dos Regions con el ancho enlazado a mano: ese enlace
        // dependía del orden exacto de layout entre ambos Regions y dejaba de pintarse en
        // esta pantalla. El control nativo calcula el relleno proporcional de forma confiable.
        //El valor va de 0 a 100, pero ProgressBar espera un numero entre 0.0 y 1.0, por eso se divide entre 100.0
        ProgressBar progreso = new ProgressBar(valor / 100.0);
        progreso.getStyleClass().add("barra-habilidad");
        progreso.setMaxWidth(Double.MAX_VALUE);
        progreso.setPrefHeight(14);

        barra.getChildren().addAll(encabezadoBarra, progreso);
        return barra;
    }

    //Agrega un dato (etiqueta + valor) a la cuadricula, acomodandolo en 2 columnas segun el numero de fila
    private void agregarDato(GridPane grid, int fila, String etiqueta, String valor) {
        Label labelEtiqueta = new Label(etiqueta);
        labelEtiqueta.getStyleClass().add("etiqueta-campo");
        Label labelValor = new Label(valor);
        labelValor.getStyleClass().add("texto-normal");
        labelValor.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        VBox caja = new VBox(4, labelEtiqueta, labelValor);
        //fila % 2 alterna entre columna 0 y columna 1, y fila / 2 avanza de fila en fila cada 2 datos (division entera)
        grid.add(caja, fila % 2, fila / 2);
    }
}
