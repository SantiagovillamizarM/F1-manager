package com.f1manager.infraestructura.ui.screens.campeonato;

import com.f1manager.dominio.modelo.Campeonato;
import com.f1manager.dominio.modelo.Piloto;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Map;

/** Pantalla final del campeonato: corona al piloto y al equipo campeón. */
public class CampeonPane extends VBox {

    public CampeonPane(Campeonato campeonato, Runnable alMenu) {
        setSpacing(22);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10));

        Map.Entry<Piloto, Integer> campeonPiloto = campeonato.getClasificacionPilotos().get(0);
        Map.Entry<String, Integer> campeonEquipo = campeonato.getClasificacionEquipos().get(0);

        StackPane trofeo = IconFactory.avatarPiloto(campeonPiloto.getKey(), 100);

        Label titulo = new Label("¡CAMPEÓN DE LA TEMPORADA!");
        titulo.getStyleClass().add("titulo-principal");

        Label nombrePiloto = new Label(campeonPiloto.getKey().getNombre());
        nombrePiloto.getStyleClass().add("texto-rojo");
        nombrePiloto.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        Label detallePiloto = new Label(campeonPiloto.getKey().getEquipo() + "   ·   " + campeonPiloto.getValue() + " puntos");
        detallePiloto.getStyleClass().add("texto-secundario");

        Label equipoTitulo = new Label("Campeón de Constructores: " + campeonEquipo.getKey()
                + " (" + campeonEquipo.getValue() + " puntos)");
        equipoTitulo.getStyleClass().add("texto-normal");
        equipoTitulo.setStyle("-fx-font-size: 15px;");

        Button boton = new Button("MENÚ PRINCIPAL");
        boton.getStyleClass().add("boton-grande");
        boton.setOnAction(e -> alMenu.run());

        getChildren().addAll(trofeo, titulo, nombrePiloto, detallePiloto, equipoTitulo, boton);
    }
}
