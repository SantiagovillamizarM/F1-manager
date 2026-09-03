//Pantalla "Buscar circuito": deja buscar circuitos escribiendo el país y
//reutiliza el mismo panel visual de listado/detalle que "Listar circuitos".

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.circuitos;

//Trae el DataStore, que es donde se guarda toda la información del programa (circuitos, pilotos, equipos, etc.)
import com.f1manager.infraestructura.persistencia.DataStore;
//Trae ValidacionException, el error controlado que se lanza cuando el usuario escribe algo inválido
import com.f1manager.dominio.excepcion.ValidacionException;
//Trae Insets, que sirve para dejar márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (por ejemplo, a la izquierda)
import javafx.geometry.Pos;
//Trae Button, un botón que se puede presionar
import javafx.scene.control.Button;
//Trae Label, que es un texto que se muestra en pantalla (no se puede editar)
import javafx.scene.control.Label;
//Trae TextField, una casilla de texto donde el usuario puede escribir
import javafx.scene.control.TextField;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila)
import javafx.scene.layout.HBox;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna)
import javafx.scene.layout.VBox;

//Trae Collections, una clase de herramientas de Java para trabajar con listas (aquí se usa para crear una lista vacía)
import java.util.Collections;

//Clase publica llamada "CircuitosBuscarPane" que hereda de VBox (osea que ella misma es una columna de elementos)
public class CircuitosBuscarPane extends VBox {

    //Constructor
    //Arma la pantalla de búsqueda: la casilla de texto para el país, el botón de buscar y el panel de resultados
    public CircuitosBuscarPane() {
        //Deja 20 pixeles de espacio entre cada elemento de la columna
        setSpacing(20);

        //Crea el texto del título de la pantalla
        Label titulo = new Label("Buscar circuito por país");
        //Le agrega el estilo visual "titulo-seccion" (definido en el CSS)
        titulo.getStyleClass().add("titulo-seccion");

        //Casilla donde el usuario escribe el país que quiere buscar
        TextField campoPais = new TextField();
        campoPais.getStyleClass().add("campo-texto");
        campoPais.setPromptText("Ingrese el país, ej: Italia");
        campoPais.setPrefWidth(320);

        //Texto donde se muestran los errores o el aviso de "no se encontró nada"
        Label mensaje = new Label();
        mensaje.getStyleClass().add("error-label");

        //Panel reutilizable de lista+detalle, arranca vacío hasta que el usuario busque algo
        PanelListaCircuitos panel = new PanelListaCircuitos(Collections.emptyList(),
                "Ingrese un país y presione Buscar.");

        //Botón que dispara la búsqueda
        Button buscar = new Button("BUSCAR");
        buscar.getStyleClass().add("boton-primario");
        //Al presionar el botón, busca los circuitos que coincidan con el país escrito
        buscar.setOnAction(e -> {
            try {
                //Le pide al DataStore la lista de circuitos que coinciden con el país ingresado
                var resultados = DataStore.getInstancia().buscarCircuitosPorPais(campoPais.getText());
                //Limpia cualquier mensaje de error anterior
                mensaje.setText("");
                //Actualiza el panel para que muestre los circuitos encontrados
                panel.actualizar(resultados);
                //Si no se encontró ningún circuito, avisa al usuario
                if (resultados.isEmpty()) {
                    mensaje.setText("No se encontraron circuitos para \"" + campoPais.getText().trim() + "\".");
                }
            } catch (ValidacionException ex) {
                //Si el país ingresado no es válido (por ejemplo, vacío), muestra el mensaje del error
                mensaje.setText(ex.getMessage());
            }
        });
        //Si el usuario presiona Enter dentro de la casilla de texto, hace como si hubiera presionado el botón Buscar
        campoPais.setOnAction(e -> buscar.fire());

        //Fila con la casilla de texto y el botón de buscar, uno al lado del otro
        HBox filaBusqueda = new HBox(14, campoPais, buscar);
        filaBusqueda.setAlignment(Pos.CENTER_LEFT);

        //Columna que agrupa la fila de búsqueda y el mensaje de error/aviso
        VBox encabezado = new VBox(10, filaBusqueda, mensaje);
        encabezado.setPadding(new Insets(0, 0, 6, 0));

        //Agrega el título, el encabezado de búsqueda y el panel de resultados a la pantalla
        getChildren().addAll(titulo, encabezado, panel);
    }
}
