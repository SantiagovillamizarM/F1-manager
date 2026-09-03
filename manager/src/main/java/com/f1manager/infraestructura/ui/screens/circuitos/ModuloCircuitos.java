//Módulo de Gestión de Circuitos. Al entrar se muestran cuatro tarjetas
//grandes (Listar / Registrar / Buscar / Eliminar / Editar). Al elegir
//cualquiera, esas mismas opciones se convierten en una barra lateral de
//mini íconos y el área central pasa a mostrar la sub-sección elegida.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.circuitos;


//Trae GestorEscenas, la clase que se encarga de cambiar de una pantalla a otra dentro del programa
import com.f1manager.infraestructura.ui.util.GestorEscenas;
//Trae BarraMiniIconos, la barra lateral con iconitos pequeños que aparece cuando ya elegiste una sub-sección
import com.f1manager.infraestructura.ui.components.BarraMiniIconos;
//Trae TarjetaOpcion, la tarjeta grande y clickeable que se muestra en el menú principal del módulo
import com.f1manager.infraestructura.ui.components.TarjetaOpcion;
//Trae ModuloGestionBase, la clase base que ya trae hecha la estructura común de todos los módulos de gestión (barra lateral, área central, etc.)
import com.f1manager.infraestructura.ui.ModuloGestionBase;
//Trae IconFactory, la fábrica que arma los iconos que se usan en las tarjetas y en la barra lateral
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae Insets, que sirve para dejar márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (por ejemplo, centrado)
import javafx.geometry.Pos;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila)
import javafx.scene.layout.HBox;

//Trae la interfaz List, que define el comportamiento general de una lista en Java
import java.util.List;

//Clase publica llamada "ModuloCircuitos" que hereda de ModuloGestionBase (la base común de todos los módulos de gestión)
public class ModuloCircuitos extends ModuloGestionBase {

    //Constructor
    //Arma el módulo con el título "GESTIÓN DE CIRCUITOS" y muestra de entrada el menú de tarjetas
    public ModuloCircuitos(GestorEscenas gestor) {
        super(gestor, "GESTIÓN DE CIRCUITOS");
        mostrarMenuTarjetas();
    }

    //Construye y muestra el menú principal con las cinco tarjetas grandes (una por cada acción del módulo)
    private void mostrarMenuTarjetas() {
        //Quita la barra lateral de mini íconos porque en el menú principal no se necesita
        quitarBarraLateral();

        //Tarjeta para ir a la lista de circuitos (indice 0)
        TarjetaOpcion listar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.logoGestionCircuitos(), 60),
                "LISTAR\nCIRCUITOS", () -> irA(0), false);
        //Tarjeta para ir al formulario de registrar un circuito nuevo (indice 1)
        TarjetaOpcion registrar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.documento(IconFactory.BLANCO), 60),
                "REGISTRAR\nCIRCUITO", () -> irA(1), false);
        //Tarjeta para ir a la búsqueda de circuitos por país (indice 2)
        TarjetaOpcion buscar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.lupa(IconFactory.BLANCO), 60),
                "BUSCAR\nCIRCUITO", () -> irA(2), false);
        //Tarjeta para ir a eliminar un circuito (indice 3)
        TarjetaOpcion eliminar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.equis(IconFactory.BLANCO), 60),
                "ELIMINAR\nCIRCUITO", () -> irA(3), false);
        //Tarjeta para ir a editar un circuito (indice 4)
        TarjetaOpcion editar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.lapiz(IconFactory.BLANCO), 60),
                "EDITAR\nCIRCUITO", () -> irA(4), false);

        //Fila que pone las cinco tarjetas una al lado de la otra, separadas por 24 pixeles
        HBox fila = new HBox(24, listar, registrar, buscar, eliminar, editar);
        fila.setAlignment(Pos.CENTER);
        fila.setPadding(new Insets(40, 0, 0, 0));

        //Muestra la fila de tarjetas en el área central del módulo
        mostrarEnCentro(fila);
    }

    //Arma la lista de items que va a usar la barra lateral de mini íconos, uno por cada sub-sección del módulo
    private List<BarraMiniIconos.Item> construirItemsBarra() {
        return List.of(
                new BarraMiniIconos.Item(IconFactory.logoGestionCircuitos(), "Listar circuitos", () -> irA(0)),
                new BarraMiniIconos.Item(IconFactory.documento(IconFactory.BLANCO), "Registrar circuito", () -> irA(1)),
                new BarraMiniIconos.Item(IconFactory.lupa(IconFactory.BLANCO), "Buscar circuito", () -> irA(2)),
                new BarraMiniIconos.Item(IconFactory.equis(IconFactory.BLANCO), "Eliminar circuito", () -> irA(3)),
                new BarraMiniIconos.Item(IconFactory.lapiz(IconFactory.BLANCO), "Editar circuito", () -> irA(4))
        );
    }

    //Cambia de sub-sección: pone la barra lateral con el item resaltado según el indice y muestra
    //en el área central la pantalla que corresponda a ese indice
    private void irA(int indice) {
        establecerBarraLateral(construirItemsBarra(), indice);
        switch (indice) {
            case 0 -> mostrarEnCentro(new CircuitosListarPane());
            case 1 -> mostrarEnCentro(new CircuitosRegistrarPane(this::irAListar));
            case 2 -> mostrarEnCentro(new CircuitosBuscarPane());
            case 3 -> mostrarEnCentro(new CircuitosEliminarPane());
            case 4 -> mostrarEnCentro(new CircuitosEditarPane());
        }
    }

    //Atajo para volver a la pantalla de "Listar circuitos" (se usa como callback cuando se cancela o se guarda un registro)
    private void irAListar() {
        irA(0);
    }
}
