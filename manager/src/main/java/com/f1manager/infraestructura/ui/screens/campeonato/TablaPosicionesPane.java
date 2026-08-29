package com.f1manager.infraestructura.ui.screens.campeonato;

import com.f1manager.dominio.modelo.Campeonato;
import com.f1manager.dominio.modelo.Piloto;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Map;

/** Clasificación de pilotos y de equipos del campeonato, consultable entre carrera y carrera. */
public class TablaPosicionesPane extends VBox {

    public TablaPosicionesPane(Campeonato campeonato, Runnable alContinuar) {
        setSpacing(20);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(10));

        Label titulo = new Label("CLASIFICACIÓN DEL CAMPEONATO");
        titulo.getStyleClass().add("titulo-principal");

        int carrerasCorridas = campeonato.getNumeroCarreraActual() - 1;
        Label subtitulo = new Label("Después de la carrera " + carrerasCorridas
                + " de " + campeonato.getTotalCarreras());
        subtitulo.getStyleClass().add("texto-secundario");

        VBox columnaPilotos = new VBox(2);
        int posicion = 1;
        for (Map.Entry<Piloto, Integer> entrada : campeonato.getClasificacionPilotos()) {
            columnaPilotos.getChildren().add(construirFila(posicion,
                    entrada.getKey().getNombre() + "  ·  " + entrada.getKey().getEquipo(), entrada.getValue(),
                    IconFactory.avatarPiloto(entrada.getKey(), 30)));
            posicion++;
        }

        VBox columnaEquipos = new VBox(2);
        posicion = 1;
        for (Map.Entry<String, Integer> entrada : campeonato.getClasificacionEquipos()) {
            columnaEquipos.getChildren().add(construirFila(posicion, entrada.getKey(), entrada.getValue()));
            posicion++;
        }

        HBox filas = new HBox(24, envolver("PILOTOS", columnaPilotos), envolver("EQUIPOS", columnaEquipos));
        filas.setAlignment(Pos.TOP_CENTER);

        Button boton = new Button(campeonato.quedanCarreras() ? "SIGUIENTE CARRERA" : "VER CAMPEÓN");
        boton.getStyleClass().add("boton-grande");
        boton.setOnAction(e -> alContinuar.run());

        getChildren().addAll(titulo, subtitulo, filas, boton);
    }

    private VBox envolver(String tituloColumna, VBox filas) {
        Label titulo = new Label(tituloColumna);
        titulo.getStyleClass().add("texto-rojo");

        ScrollPane scroll = new ScrollPane(filas);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(380);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox panel = new VBox(12, titulo, scroll);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(18));
        panel.setPrefWidth(340);
        return panel;
    }

    private HBox construirFila(int posicion, String nombre, int puntos) {
        return construirFila(posicion, nombre, puntos, null);
    }

    private HBox construirFila(int posicion, String nombre, int puntos, StackPane avatar) {
        boolean lider = posicion == 1;

        Label posicionLabel = new Label("P" + posicion);
        posicionLabel.setPrefWidth(40);
        // Sin esto, el HBox comprime la etiqueta por debajo de su ancho preferido cuando el
        // nombre es largo, y JavaFX le pone "…" en vez de mostrar el texto completo.
        posicionLabel.setMinWidth(Region.USE_PREF_SIZE);
        posicionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: "
                + (lider ? "#ffd400" : "#f5f6fa") + ";");

        Label nombreLabel = new Label(nombre);
        nombreLabel.getStyleClass().add("texto-normal");
        nombreLabel.setWrapText(true);
        nombreLabel.setPrefWidth(170);
        nombreLabel.setMaxWidth(170);

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        Label puntosLabel = new Label(puntos + " pts");
        puntosLabel.setMinWidth(Region.USE_PREF_SIZE);
        puntosLabel.getStyleClass().add(lider ? "texto-rojo" : "texto-secundario");
        puntosLabel.setStyle(puntosLabel.getStyle() + "-fx-font-weight: bold;");

        HBox fila = avatar != null
                ? new HBox(10, posicionLabel, avatar, nombreLabel, espaciador, puntosLabel)
                : new HBox(10, posicionLabel, nombreLabel, espaciador, puntosLabel);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(8, 10, 8, 10));
        return fila;
    }
}
