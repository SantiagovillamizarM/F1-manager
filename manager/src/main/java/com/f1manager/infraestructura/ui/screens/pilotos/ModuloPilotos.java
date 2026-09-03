//Este es el módulo (la pantalla principal) de Gestión de Pilotos: muestra el
//menú de tarjetas grandes para Listar / Registrar / Eliminar / Editar y se
//encarga de cambiar entre esas sub-pantallas.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.pilotos;

//Trae el GestorEscenas, que es el que controla el cambio entre las distintas pantallas del programa
import com.f1manager.infraestructura.ui.util.GestorEscenas;
//Trae BarraMiniIconos, la barra lateral con los iconos chiquitos para navegar entre las sub-pantallas
import com.f1manager.infraestructura.ui.components.BarraMiniIconos;
//Trae TarjetaOpcion, que es el componente visual de las tarjetas grandes clickeables del menú
import com.f1manager.infraestructura.ui.components.TarjetaOpcion;
//Trae ModuloGestionBase, la clase padre que ya trae armado el esqueleto comun de los modulos de gestion (titulo, barra lateral, etc.)
import com.f1manager.infraestructura.ui.ModuloGestionBase;
//Trae IconFactory, que es la fabrica de iconos (dibuja los iconos que se usan en las tarjetas y la barra lateral)
import com.f1manager.infraestructura.ui.util.IconFactory;
//Trae Insets, que sirve para poner margenes/rellenos alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para definir alineaciones (centrado, izquierda, etc.)
import javafx.geometry.Pos;
//Trae HBox, que es un contenedor de JavaFX que acomoda sus elementos uno al lado del otro (en horizontal)
import javafx.scene.layout.HBox;

//Importa la interfaz List, que define el comportamiento general de una lista en Java
import java.util.List;

//Clase publica que extiende de ModuloGestionBase, osea que hereda todo el comportamiento comun de los modulos de gestion
public class ModuloPilotos extends ModuloGestionBase {

    //Constructor
    //Arma el modulo con el titulo "GESTIÓN DE PILOTOS" y de una vez muestra el menu de tarjetas
    public ModuloPilotos(GestorEscenas gestor) {
        super(gestor, "GESTIÓN DE PILOTOS");
        mostrarMenuTarjetas();
    }

    //Arma y muestra el menu principal con las 4 tarjetas grandes (Listar, Registrar, Eliminar, Editar)
    private void mostrarMenuTarjetas() {
        //Como estamos en el menu principal, no se necesita la barra lateral de iconos todavia
        quitarBarraLateral();

        //Tarjeta para ir a la pantalla de listar pilotos (indice 0)
        TarjetaOpcion listar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.logoGestionPilotos(), 60),
                "LISTAR\nPILOTOS", () -> irA(0), false);
        //Tarjeta para ir a la pantalla de registrar un piloto nuevo (indice 1)
        TarjetaOpcion registrar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.documento(IconFactory.BLANCO), 60),
                "REGISTRAR\nPILOTO", () -> irA(1), false);
        //Tarjeta para ir a la pantalla de eliminar un piloto (indice 2)
        TarjetaOpcion eliminar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.equis(IconFactory.BLANCO), 60),
                "ELIMINAR\nPILOTO", () -> irA(2), false);
        //Tarjeta para ir a la pantalla de editar un piloto existente (indice 3)
        TarjetaOpcion editar = new TarjetaOpcion(
                IconFactory.contenedor(IconFactory.lapiz(IconFactory.BLANCO), 60),
                "EDITAR\nPILOTO", () -> irA(3), false);

        //Pone las 4 tarjetas en fila (horizontal), con 24 pixeles de espacio entre cada una
        HBox fila = new HBox(24, listar, registrar, eliminar, editar);
        //Centra la fila de tarjetas
        fila.setAlignment(Pos.CENTER);
        //Le agrega 40 pixeles de margen arriba
        fila.setPadding(new Insets(40, 0, 0, 0));
        //Muestra la fila de tarjetas en el centro de la pantalla
        mostrarEnCentro(fila);
    }

    //Arma la lista de items (icono + texto + accion) que se muestran en la barra lateral una vez ya estamos dentro de una sub-pantalla
    private List<BarraMiniIconos.Item> construirItemsBarra() {
        return List.of(
                new BarraMiniIconos.Item(IconFactory.logoGestionPilotos(), "Listar pilotos", () -> irA(0)),
                new BarraMiniIconos.Item(IconFactory.documento(IconFactory.BLANCO), "Registrar piloto", () -> irA(1)),
                new BarraMiniIconos.Item(IconFactory.equis(IconFactory.BLANCO), "Eliminar piloto", () -> irA(2)),
                new BarraMiniIconos.Item(IconFactory.lapiz(IconFactory.BLANCO), "Editar piloto", () -> irA(3))
        );
    }

    //Cambia de sub-pantalla segun el indice recibido (0 = Listar, 1 = Registrar, 2 = Eliminar, 3 = Editar)
    //y de paso deja la barra lateral armada y marcada en la opcion correcta
    private void irA(int indice) {
        establecerBarraLateral(construirItemsBarra(), indice);
        //Segun el indice, muestra en el centro la pantalla que corresponda
        switch (indice) {
            case 0 -> mostrarEnCentro(new PilotosListarPane());
            case 1 -> mostrarEnCentro(new PilotosRegistrarPane(this::irAListar));
            case 2 -> mostrarEnCentro(new PilotosEliminarPane());
            case 3 -> mostrarEnCentro(new PilotosEditarPane());
        }
    }

    //Metodo de atajo para volver a la pantalla de listar (indice 0), usado por ejemplo cuando se cancela el registro
    private void irAListar() {
        irA(0);
    }
}
