package com.f1manager.infraestructura.ui.screens.campeonato;

import com.f1manager.dominio.modelo.Campeonato;
import com.f1manager.dominio.modelo.Circuito;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/** Pantalla inicial del modo campeonato: muestra el calendario completo antes de arrancar. */
public class BienvenidaCampeonatoPane extends VBox {

    public BienvenidaCampeonatoPane(Campeonato campeonato, Runnable alComenzar) {
        setSpacing(20);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(10));

        Label titulo = new Label("MODO CAMPEONATO");
        titulo.getStyleClass().add("titulo-principal");

        Label subtitulo = new Label(campeonato.getTotalCarreras()
                + " carreras · puntúan las primeras 10 posiciones de cada una (25-18-15-12-10-8-6-4-2-1)");
        subtitulo.getStyleClass().add("texto-secundario");
        subtitulo.setWrapText(true);
        subtitulo.setStyle("-fx-text-alignment: center;");

        VBox panelCalendario = new VBox(10);
        panelCalendario.getStyleClass().add("panel");
        panelCalendario.setPadding(new Insets(20));
        panelCalendario.setMaxWidth(480);

        Label tituloCalendario = new Label("CALENDARIO");
        tituloCalendario.getStyleClass().add("texto-rojo");
        panelCalendario.getChildren().add(tituloCalendario);

        int numero = 1;
        for (Circuito circuito : campeonato.getCalendario()) {
            Label fila = new Label(numero + ".  " + circuito.getNombre() + "   ·   " + circuito.getPais());
            fila.getStyleClass().add("texto-normal");
            panelCalendario.getChildren().add(fila);
            numero++;
        }

        ScrollPane scroll = new ScrollPane(panelCalendario);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(360);
        scroll.setMaxWidth(500);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Button botonComenzar = new Button("COMENZAR CAMPEONATO");
        botonComenzar.getStyleClass().add("boton-grande");
        botonComenzar.setOnAction(e -> alComenzar.run());

        getChildren().addAll(titulo, subtitulo, scroll, botonComenzar);
    }
}
