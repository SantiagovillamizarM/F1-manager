package com.f1manager.infraestructura.ui.components;

import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/**
 * Barra de búsqueda reutilizable (lupa + campo de texto), usada para
 * filtrar listas largas en tiempo real, por ejemplo en las secciones de
 * "Eliminar" de cada módulo.
 */
public class CampoBusqueda extends HBox {

    private final TextField campoTexto = new TextField();

    public CampoBusqueda(String promptText) {
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);

        StackPane icono = IconFactory.contenedor(IconFactory.lupa(IconFactory.BLANCO), 46);

        campoTexto.getStyleClass().addAll("campo-texto", "campo-busqueda-texto");
        campoTexto.setPromptText(promptText);
        campoTexto.setPrefHeight(46);
        HBox.setHgrow(campoTexto, Priority.ALWAYS);

        getChildren().addAll(icono, campoTexto);
    }

    public TextField getCampoTexto() {
        return campoTexto;
    }

    public String getTexto() {
        return campoTexto.getText();
    }
}