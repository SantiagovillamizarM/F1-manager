//Sub-pantalla para configurar un vehículo: permite elegir un monoplaza ya
//registrado y ajustar su carga aerodinámica, modo de conducción, neumático
//y presión de aire, cosas que afectan de verdad a la simulación de carrera.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.ui.screens.vehiculos;

//Trae la clase que guarda y maneja toda la información del programa (equipos, pilotos, vehiculos, etc), funciona como la "base de datos" en memoria
import com.f1manager.aplicacion.DataStore;
//Trae el error personalizado que se lanza cuando el usuario ingresa datos inválidos
import com.f1manager.dominio.excepcion.ValidacionException;
//Trae el enum CargaAerodinamica (Baja/Media/Alta), una de las opciones configurables del vehículo
import com.f1manager.dominio.modelo.CargaAerodinamica;
//Trae el enum ModoConduccion (Normal/Agresivo/Ahorro), otra de las opciones configurables del vehículo
import com.f1manager.dominio.modelo.ModoConduccion;
//Trae la clase Monoplaza, que es el objeto con los datos técnicos del vehículo que se va a configurar
import com.f1manager.dominio.modelo.Monoplaza;
//Trae el enum TipoNeumatico, la opción de neumático configurable del vehículo
import com.f1manager.dominio.modelo.TipoNeumatico;
//Trae la clase que reproduce los sonidos de confirmación/error de la app
import com.f1manager.infraestructura.ui.util.GestorSonido;
//Trae FXCollections, que sirve para crear listas observables (listas que avisan a la UI cuando cambian, para que el ComboBox se actualice solo)
import javafx.collections.FXCollections;
//Trae Insets, que sirve para poner márgenes/espacios alrededor de un elemento
import javafx.geometry.Insets;
//Trae Pos, que sirve para alinear elementos (centrado, izquierda, etc)
import javafx.geometry.Pos;
//Trae Button, el botón que se puede clickear
import javafx.scene.control.Button;
//Trae ComboBox, que es el menú desplegable donde el usuario elige una opción de una lista (aca se usa para elegir el monoplaza)
import javafx.scene.control.ComboBox;
//Trae Label, que es un texto que se muestra en pantalla (no editable)
import javafx.scene.control.Label;
//Trae TextField, que es el cuadro de texto donde el usuario puede escribir
import javafx.scene.control.TextField;
//Trae HBox, un contenedor que acomoda los elementos uno al lado del otro (en fila horizontal)
import javafx.scene.layout.HBox;
//Trae VBox, un contenedor que acomoda los elementos uno debajo del otro (en columna vertical)
import javafx.scene.layout.VBox;

//Clase publica llamada "VehiculosConfigurarPane" que hereda de VBox (osea que ella misma es una columna donde se van poniendo los elementos)
public class VehiculosConfigurarPane extends VBox {

    //Menú desplegable donde se elige a qué monoplaza registrado se le va a cambiar la configuración
    private final ComboBox<Monoplaza> comboVehiculo = new ComboBox<>();
    //Guarda la carga aerodinámica que el usuario eligió haciendo click en una de las opciones (Baja/Media/Alta)
    private CargaAerodinamica cargaSeleccionada;
    //Guarda el modo de conducción que el usuario eligió haciendo click en una de las opciones (Normal/Agresivo/Ahorro)
    private ModoConduccion modoSeleccionado;
    //Guarda el tipo de neumático que el usuario eligió haciendo click en una de las opciones
    private TipoNeumatico neumaticoSeleccionado;
    //Fila donde se dibujan las opciones de carga aerodinámica (una casilla por cada valor del enum)
    private final HBox filaCargas = new HBox(14);
    //Fila donde se dibujan las opciones de modo de conducción (una casilla por cada valor del enum)
    private final HBox filaModos = new HBox(14);
    //Fila donde se dibujan las opciones de tipo de neumático (una casilla por cada valor del enum)
    private final HBox filaNeumaticos = new HBox(14);
    //Cuadro de texto donde se escribe la presión de aire deseada
    private final TextField campoPresion = new TextField();
    //Label donde se muestra el resultado de la operación (si se guardó bien o si hubo un error)
    private final Label mensaje = new Label();

    //Constructor
    //Arma todo el formulario: el combo para elegir el monoplaza, las filas de opciones clickeables (carga, modo, neumático), el campo de presión y los botones
    public VehiculosConfigurarPane() {
        setSpacing(20);
        setMaxWidth(640);

        Label titulo = new Label("Configurar vehículo");
        titulo.getStyleClass().add("titulo-seccion");

        VBox panel = new VBox(20);
        panel.getStyleClass().add("panel-glow");
        panel.setPadding(new Insets(30));

        Label etiquetaModelo = new Label("Seleccione el modelo del vehículo");
        etiquetaModelo.getStyleClass().add("etiqueta-campo");

        comboVehiculo.getStyleClass().add("combo-oscuro");
        comboVehiculo.setMaxWidth(Double.MAX_VALUE);
        comboVehiculo.setItems(FXCollections.observableArrayList(DataStore.getInstancia().getVehiculos()));
        comboVehiculo.setPromptText("Seleccione un monoplaza");
        //Cada vez que se elige un monoplaza distinto en el combo, se cargan sus valores actuales en las opciones y el campo de presión
        comboVehiculo.setOnAction(e -> cargarConfiguracionActual());

        Label etiquetaCarga = new Label("Carga aerodinámica");
        etiquetaCarga.getStyleClass().add("etiqueta-campo");
        //Arma una casilla clickeable por cada valor del enum CargaAerodinamica; al hacer click guarda cuál se eligió y la resalta
        construirOpciones(filaCargas, CargaAerodinamica.values(), o -> {
            cargaSeleccionada = (CargaAerodinamica) o;
            actualizarSeleccionVisual(filaCargas, o.toString());
        });

        Label etiquetaModo = new Label("Modo de conducción");
        etiquetaModo.getStyleClass().add("etiqueta-campo");
        //Arma una casilla clickeable por cada valor del enum ModoConduccion; al hacer click guarda cuál se eligió y la resalta
        construirOpciones(filaModos, ModoConduccion.values(), o -> {
            modoSeleccionado = (ModoConduccion) o;
            actualizarSeleccionVisual(filaModos, o.toString());
        });

        Label etiquetaNeumatico = new Label("Tipo de neumático");
        etiquetaNeumatico.getStyleClass().add("etiqueta-campo");
        //Arma una casilla clickeable por cada valor del enum TipoNeumatico; al hacer click guarda cuál se eligió y la resalta
        construirOpciones(filaNeumaticos, TipoNeumatico.values(), o -> {
            neumaticoSeleccionado = (TipoNeumatico) o;
            actualizarSeleccionVisual(filaNeumaticos, o.toString());
        });

        Label etiquetaPresion = new Label(String.format("Presión de aire (PSI, entre %.0f y %.0f)",
                Monoplaza.PRESION_MINIMA, Monoplaza.PRESION_MAXIMA));
        etiquetaPresion.getStyleClass().add("etiqueta-campo");
        campoPresion.getStyleClass().add("campo-texto");
        campoPresion.setPromptText("Ej: " + (int) Monoplaza.PRESION_OPTIMA);

        mensaje.setWrapText(true);

        Button guardar = new Button("GUARDAR");
        guardar.getStyleClass().add("boton-primario");
        guardar.setOnAction(e -> guardar());

        //Botón que reinicia todo el formulario: quita la selección del combo, borra las opciones elegidas y les quita el resaltado visual
        Button cancelar = new Button("CANCELAR");
        cancelar.getStyleClass().add("boton-secundario");
        cancelar.setOnAction(e -> {
            comboVehiculo.setValue(null);
            cargaSeleccionada = null;
            modoSeleccionado = null;
            neumaticoSeleccionado = null;
            campoPresion.clear();
            filaCargas.getChildren().forEach(n -> n.getStyleClass().setAll("opcion-clima"));
            filaModos.getChildren().forEach(n -> n.getStyleClass().setAll("opcion-clima"));
            filaNeumaticos.getChildren().forEach(n -> n.getStyleClass().setAll("opcion-clima"));
            mensaje.setText("");
        });

        HBox botones = new HBox(14, guardar, cancelar);
        botones.setAlignment(Pos.CENTER_LEFT);

        panel.getChildren().addAll(
                etiquetaModelo, comboVehiculo,
                etiquetaCarga, filaCargas,
                etiquetaModo, filaModos,
                etiquetaNeumatico, filaNeumaticos,
                etiquetaPresion, campoPresion,
                mensaje, botones
        );

        getChildren().addAll(titulo, panel);
    }

    //Método genérico que arma las casillas clickeables de un grupo de opciones (sirve tanto para carga, modo o neumático,
    //ya que a todos les llega un arreglo de valores de enum y una acción para cuando se selecciona uno)
    private void construirOpciones(HBox contenedor, Object[] valores, java.util.function.Consumer<Object> alSeleccionar) {
        //Por cada valor posible del enum se arma una casillita con su texto
        for (Object valor : valores) {
            Label label = new Label(valor.toString());
            label.getStyleClass().add("texto-normal");
            VBox opcion = new VBox(label);
            opcion.getStyleClass().add("opcion-clima");
            opcion.setPrefWidth(140);
            //Al hacer click en la casilla se ejecuta la acción que le pasaron (guardar el valor elegido y resaltarlo)
            opcion.setOnMouseClicked(e -> alSeleccionar.accept(valor));
            contenedor.getChildren().add(opcion);
        }
    }

    //Recorre todas las casillas de un grupo (carga, modo o neumático) y le pone la clase de "seleccionada" solo a la que coincide con el texto elegido
    private void actualizarSeleccionVisual(HBox contenedor, String textoSeleccionado) {
        for (var nodo : contenedor.getChildren()) {
            VBox opcion = (VBox) nodo;
            Label label = (Label) opcion.getChildren().get(0);
            opcion.getStyleClass().setAll(label.getText().equals(textoSeleccionado) ? "opcion-clima-seleccionada" : "opcion-clima");
        }
    }

    //Cuando se elige un monoplaza en el combo, este método rellena el formulario con la configuración que ya tenía ese vehículo
    private void cargarConfiguracionActual() {
        Monoplaza m = comboVehiculo.getValue();
        //Si no hay ningún monoplaza elegido no hay nada que cargar
        if (m == null) return;
        //Se toman los valores actuales del vehículo y se guardan como los "seleccionados"
        cargaSeleccionada = m.getCargaAerodinamica();
        modoSeleccionado = m.getModoConduccion();
        neumaticoSeleccionado = m.getTipoNeumatico();
        //Y se resaltan visualmente las casillas que correspondan a esos valores actuales
        actualizarSeleccionVisual(filaCargas, cargaSeleccionada.toString());
        actualizarSeleccionVisual(filaModos, modoSeleccionado.toString());
        actualizarSeleccionVisual(filaNeumaticos, neumaticoSeleccionado.toString());
        campoPresion.setText(String.valueOf(m.getPresionAire()));
    }

    //Este método intenta guardar la nueva configuración del monoplaza elegido
    private void guardar() {
        try {
            //Si no se eligió ningún monoplaza en el combo, se avisa con una excepción antes de intentar guardar nada
            if (comboVehiculo.getValue() == null) {
                throw new ValidacionException("Debe seleccionar un modelo de vehículo.");
            }
            //Le pide al DataStore que actualice, por el id del vehículo, la carga aerodinámica, el modo, el neumático y la presión elegidos
            DataStore.getInstancia().configurarVehiculo(comboVehiculo.getValue().getId(), cargaSeleccionada, modoSeleccionado,
                    neumaticoSeleccionado, campoPresion.getText());
            //Si todo salió bien se muestra el mensaje en rojo (positivo) con el nombre del modelo, y se reproduce el sonido de confirmación
            mensaje.getStyleClass().removeAll("error-label");
            mensaje.getStyleClass().add("texto-rojo");
            mensaje.setText("Configuración guardada correctamente para " + comboVehiculo.getValue().getModelo() + ".");
            GestorSonido.reproducir("Confirmado sound.mp3");
        } catch (ValidacionException ex) {
            //Si algo salió mal (nada elegido, presión fuera de rango, etc) se muestra el mensaje de error y se reproduce el sonido de error
            mensaje.getStyleClass().removeAll("texto-rojo");
            mensaje.getStyleClass().add("error-label");
            mensaje.setText(ex.getMessage());
            GestorSonido.reproducir("Error sound.mp3");
        }
    }
}
