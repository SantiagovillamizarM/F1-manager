package com.f1manager.infraestructura.ui.screens.equipos;

import com.f1manager.infraestructura.ui.util.GestorEscenas;
import com.f1manager.infraestructura.ui.components.BarraMiniIconos;
import com.f1manager.infraestructura.ui.components.TarjetaOpcion;
import com.f1manager.infraestructura.ui.ModuloGestionBase;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.util.List;

public class ModuloEquipos extends ModuloGestionBase {

    public ModuloEquipos(GestorEscenas gestor) {
        super(gestor, "GESTIÓN DE EQUIPOS");
        mostrarMenuTarjetas();
    }

    private void mostrarMenuTarjetas() {
        quitarBarraLateral();

        TarjetaOpcion listar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.escudoEquipo(IconFactory.BLANCO), 60),
                "LISTAR\nEQUIPOS", () -> irA(0), false);
        TarjetaOpcion registrar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.documento(IconFactory.BLANCO), 60),
                "REGISTRAR\nEQUIPO", () -> irA(1), false);
        TarjetaOpcion eliminar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.equis(IconFactory.BLANCO), 60),
                "ELIMINAR\nEQUIPO", () -> irA(2), false);

        HBox fila = new HBox(24, listar, registrar, eliminar);
        fila.setAlignment(Pos.CENTER);
        fila.setPadding(new Insets(40, 0, 0, 0));
        mostrarEnCentro(fila);
    }

    private List<BarraMiniIconos.Item> construirItemsBarra() {
        return List.of(
                new BarraMiniIconos.Item(IconFactory.escudoEquipo(IconFactory.BLANCO), "Listar equipos", () -> irA(0)),
                new BarraMiniIconos.Item(IconFactory.documento(IconFactory.BLANCO), "Registrar equipo", () -> irA(1)),
                new BarraMiniIconos.Item(IconFactory.equis(IconFactory.BLANCO), "Eliminar equipo", () -> irA(2))
        );
    }

    private void irA(int indice) {
        establecerBarraLateral(construirItemsBarra(), indice);
        switch (indice) {
            case 0 -> mostrarEnCentro(new EquiposListarPane());
            case 1 -> mostrarEnCentro(new EquiposRegistrarPane(() -> irA(0)));
            case 2 -> mostrarEnCentro(new EquiposEliminarPane());
        }
    }
}
