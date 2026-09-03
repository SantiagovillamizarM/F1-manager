//Pantalla "Registrar circuito": formulario con estilo de panel de
//simulador que valida los datos y los guarda de verdad en el sistema.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.circuitos;

//Trae el DataStore, que es donde se guarda toda la información del programa (circuitos, pilotos, equipos, etc.)
import com.f1manager.aplicacion.DataStore;
//Trae ValidacionException, el error controlado que se lanza cuando el usuario escribe algo inválido
import com.f1manager.dominio.excepcion.ValidacionException;
//Trae GestorSonido, la clase que reproduce los sonidos de confirmación o de error
import com.f1manager.infraestructura.ui.util.GestorSonido;
//Trae Insets, que sirve para dejar márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (por ejemplo, a la izquierda)
import javafx.geometry.Pos;
//Trae de un solo golpe todos los controles de JavaFX que se usan aquí (Button, Label, TextField, TextArea, etc.)
import javafx.scene.control.*;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila)
import javafx.scene.layout.HBox;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna)
import javafx.scene.layout.VBox;

//Clase publica llamada "CircuitosRegistrarPane" que hereda de VBox (osea que ella misma es una columna de elementos)
public class CircuitosRegistrarPane extends VBox {

    //Guarda la acción a ejecutar cuando el usuario cancela (normalmente, volver a la pantalla de listar)
    private final Runnable alCancelar;
    //Texto donde se muestran los mensajes de error o de confirmación
    private final Label mensaje = new Label();

    //Casilla de texto donde se escribe el nombre del circuito
    private final TextField campoNombre = new TextField();
    //Casilla de texto donde se escribe el país del circuito
    private final TextField campoPais = new TextField();
    //Casilla de texto donde se escribe la longitud de la vuelta en kilómetros
    private final TextField campoLongitud = new TextField();
    //Casilla de texto donde se escribe el número de vueltas de la carrera
    private final TextField campoVueltas = new TextField();
    //Área de texto (más grande que un TextField) donde se escribe la descripción del circuito
    private final TextArea campoDescripcion = new TextArea();

    //Constructor
    //Arma todo el formulario de registro y guarda el callback que se ejecuta al cancelar
    public CircuitosRegistrarPane(Runnable alCancelar) {
        this.alCancelar = alCancelar;
        //Deja 20 pixeles de espacio entre cada elemento de la columna
        setSpacing(20);
        setMaxWidth(620);

        //Crea el texto del título de la pantalla
        Label titulo = new Label("Registrar nuevo circuito");
        titulo.getStyleClass().add("titulo-seccion");

        //Panel con el estilo visual de "panel-glow" que envuelve todo el formulario
        VBox panelFormulario = new VBox(16);
        panelFormulario.getStyleClass().add("panel-glow");
        panelFormulario.setPadding(new Insets(30));

        campoNombre.getStyleClass().add("campo-texto");
        campoNombre.setPromptText("Ej: Circuit de Monaco");

        campoPais.getStyleClass().add("campo-texto");
        campoPais.setPromptText("Ej: Mónaco");

        campoLongitud.getStyleClass().add("campo-texto");
        campoLongitud.setPromptText("Ej: 5.891");

        campoVueltas.getStyleClass().add("campo-texto");
        campoVueltas.setPromptText("Ej: 52");

        campoDescripcion.getStyleClass().add("campo-area");
        campoDescripcion.setPromptText("Descripción del circuito...");
        campoDescripcion.setPrefRowCount(4);
        campoDescripcion.setWrapText(true);

        mensaje.setWrapText(true);

        //Botón que intenta guardar el circuito con los datos del formulario
        Button guardar = new Button("GUARDAR");
        guardar.getStyleClass().add("boton-primario");
        guardar.setOnAction(e -> guardar());

        //Botón que limpia el formulario y avisa a la pantalla anterior que se canceló (vía el Runnable alCancelar)
        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> {
            limpiar();
            alCancelar.run();
        });

        //Fila con los dos botones, uno al lado del otro
        HBox botones = new HBox(14, guardar, cancelar);
        botones.setAlignment(Pos.CENTER_LEFT);

        //Agrega al formulario, en orden, cada etiqueta con su casilla correspondiente, el mensaje y los botones
        panelFormulario.getChildren().addAll(
                etiquetaCampo("Nombre"), campoNombre,
                etiquetaCampo("País"), campoPais,
                etiquetaCampo("Longitud (km)"), campoLongitud,
                etiquetaCampo("Vueltas"), campoVueltas,
                etiquetaCampo("Descripción"), campoDescripcion,
                mensaje, botones
        );

        getChildren().addAll(titulo, panelFormulario);
    }

    //Crea una etiqueta de texto con el estilo de "etiqueta-campo" para ponerla encima de cada casilla del formulario
    private Label etiquetaCampo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("etiqueta-campo");
        return label;
    }

    //Intenta registrar el circuito con los datos escritos en el formulario. Si el DataStore detecta
    //algo inválido (nombre vacío, longitud o vueltas que no son números, etc.) lanza un ValidacionException
    //y ese error se muestra en pantalla en vez de guardar nada.
    private void guardar() {
        try {
            //Le pide al DataStore que valide y registre el nuevo circuito con los textos tal cual como se escribieron
            DataStore.getInstancia().registrarCircuito(
                    campoNombre.getText(), campoPais.getText(),
                    campoLongitud.getText(), campoVueltas.getText(), campoDescripcion.getText());
            //Si todo salió bien, muestra el mensaje de éxito en rojo (el color que usa esta pantalla para confirmaciones)
            mensaje.setText("Circuito registrado correctamente.");
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            //Reproduce el sonido de confirmación
            GestorSonido.reproducir("Confirmado sound.mp3");
            //Limpia el formulario para que quede listo para registrar otro circuito
            limpiar();
        } catch (ValidacionException ex) {
            //Si algo salió mal, muestra el mensaje del error con el estilo de error y reproduce el sonido de error
            mensaje.setText(ex.getMessage());
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            GestorSonido.reproducir("Error sound.mp3");
        }
    }

    //Vacía todas las casillas del formulario para dejarlo listo para un nuevo registro
    private void limpiar() {
        campoNombre.clear();
        campoPais.clear();
        campoLongitud.clear();
        campoVueltas.clear();
        campoDescripcion.clear();
    }
}
