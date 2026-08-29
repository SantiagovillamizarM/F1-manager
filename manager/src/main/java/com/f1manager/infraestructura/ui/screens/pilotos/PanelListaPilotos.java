package com.f1manager.infraestructura.ui.screens.pilotos;

import com.f1manager.dominio.modelo.Piloto;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Lista interactiva de pilotos. Al seleccionar uno se muestra una ficha
 * ampliada al estilo de un videojuego de gestión deportiva.
 */
public class PanelListaPilotos extends HBox {

    private final VBox columnaLista = new VBox(12);
    private final StackPane panelDetalle = new StackPane();
    private HBox filaSeleccionada;

    public PanelListaPilotos(List<Piloto> pilotos) {
        setSpacing(28);

        ScrollPane scroll = new ScrollPane(columnaLista);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-oscuro");
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
        mostrarMensajeVacio();

        HBox.setHgrow(scroll, Priority.NEVER);
        HBox.setHgrow(panelDetalle, Priority.ALWAYS);
        getChildren().addAll(scroll, panelDetalle);

        actualizar(pilotos);
    }

    public void actualizar(List<Piloto> pilotos) {
        columnaLista.getChildren().clear();
        filaSeleccionada = null;
        mostrarMensajeVacio();

        if (pilotos.isEmpty()) {
            Label vacio = new Label("No hay pilotos registrados todavía.");
            vacio.getStyleClass().add("texto-secundario");
            columnaLista.getChildren().add(vacio);
            return;
        }
        for (Piloto piloto : pilotos) {
            columnaLista.getChildren().add(construirFila(piloto));
        }
    }

    private HBox construirFila(Piloto piloto) {
        StackPane avatar = IconFactory.avatarPiloto(piloto, 46);

        Label nombre = new Label(piloto.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5f6fa;");

        Label detalle = new Label(String.format("ID %d  ·  %s  ·  %s  ·  Habilidad prom. %.0f/100",
                piloto.getId(), piloto.getEquipo(), piloto.getRol().getEtiqueta(), piloto.getHabilidadPromedio()));
        detalle.getStyleClass().add("texto-secundario");

        HBox fila = new HBox(14, avatar, new VBox(4, nombre, detalle));
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("fila-lista");
        fila.setOnMouseClicked(e -> seleccionar(piloto, fila));
        return fila;
    }

    private void seleccionar(Piloto piloto, HBox fila) {
        if (filaSeleccionada != null) {
            filaSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
            filaSeleccionada.getStyleClass().add("fila-lista");
        }
        fila.getStyleClass().remove("fila-lista");
        fila.getStyleClass().add("fila-lista-seleccionada");
        filaSeleccionada = fila;
        mostrarFicha(piloto);
    }

    private void mostrarMensajeVacio() {
        Label mensaje = new Label("Selecciona un piloto de la lista\npara ver su ficha completa.");
        mensaje.getStyleClass().add("texto-secundario");
        mensaje.setStyle("-fx-text-alignment: center;");
        panelDetalle.getChildren().setAll(mensaje);
    }

    private void mostrarFicha(Piloto piloto) {
        StackPane avatar = IconFactory.avatarPiloto(piloto, 140);

        Label nombre = new Label(piloto.getNombre());
        nombre.getStyleClass().add("titulo-seccion");

        Label equipoRol = new Label(piloto.getEquipo() + "  ·  " + piloto.getRol().getEtiqueta());
        equipoRol.getStyleClass().add("texto-rojo");

        HBox encabezado = new HBox(20, avatar, new VBox(6, nombre, equipoRol));
        encabezado.setAlignment(Pos.CENTER_LEFT);

        GridPane datos = new GridPane();
        datos.setHgap(30);
        datos.setVgap(14);
        datos.setPadding(new Insets(20, 0, 0, 0));

        agregarDato(datos, 0, "ID", String.valueOf(piloto.getId()));
        agregarDato(datos, 1, "Experiencia", piloto.getExperienciaAnios() + " años");
        agregarDato(datos, 2, "Habilidad prom.", String.format("%.0f / 100", piloto.getHabilidadPromedio()));
        agregarDato(datos, 3, "Rol", piloto.getRol().getEtiqueta());

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

    private VBox construirBarraHabilidad(String etiqueta, double valor) {
        VBox barra = new VBox(6);
        Label etiquetaBarra = new Label(etiqueta);
        etiquetaBarra.getStyleClass().add("etiqueta-campo");

        Label valorBarra = new Label(Math.round(valor) + " / 100");
        valorBarra.getStyleClass().add("texto-rojo");

        Region espaciadorEncabezado = new Region();
        HBox.setHgrow(espaciadorEncabezado, Priority.ALWAYS);
        HBox encabezadoBarra = new HBox(etiquetaBarra, espaciadorEncabezado, valorBarra);

        // ProgressBar nativo en vez de dos Regions con el ancho enlazado a mano: ese enlace
        // dependía del orden exacto de layout entre ambos Regions y dejaba de pintarse en
        // esta pantalla. El control nativo calcula el relleno proporcional de forma confiable.
        ProgressBar progreso = new ProgressBar(valor / 100.0);
        progreso.getStyleClass().add("barra-habilidad");
        progreso.setMaxWidth(Double.MAX_VALUE);
        progreso.setPrefHeight(14);

        barra.getChildren().addAll(encabezadoBarra, progreso);
        return barra;
    }

    private void agregarDato(GridPane grid, int fila, String etiqueta, String valor) {
        Label labelEtiqueta = new Label(etiqueta);
        labelEtiqueta.getStyleClass().add("etiqueta-campo");
        Label labelValor = new Label(valor);
        labelValor.getStyleClass().add("texto-normal");
        labelValor.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        VBox caja = new VBox(4, labelEtiqueta, labelValor);
        grid.add(caja, fila % 2, fila / 2);
    }
}
