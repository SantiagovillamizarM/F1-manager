package com.f1manager.infraestructura.ui.screens.carrera;

import com.f1manager.dominio.modelo.Circuito;
import com.f1manager.dominio.modelo.ResultadoCarrera;
import com.f1manager.dominio.servicio.SimuladorCarrera;
import com.f1manager.infraestructura.ui.util.FotosChoque;
import com.f1manager.infraestructura.ui.util.IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class ResultadosCarreraPane extends VBox {

    private final StackPane panelDetalle = new StackPane();
    private final SimuladorCarrera.ResultadoSimulacion simulacion;
    private HBox filaSeleccionada;

    public ResultadosCarreraPane(Circuito circuito, SimuladorCarrera.ResultadoSimulacion simulacion,
                                  Runnable nuevaCarrera, Runnable alMenu) {
        this(circuito, simulacion, "NUEVA CARRERA", nuevaCarrera, "MENÚ PRINCIPAL", alMenu);
    }

    /** Permite personalizar el texto y la acción de los dos botones (usado por el modo campeonato). */
    public ResultadosCarreraPane(Circuito circuito, SimuladorCarrera.ResultadoSimulacion simulacion,
                                  String textoBoton1, Runnable accionBoton1,
                                  String textoBoton2, Runnable accionBoton2) {
        this.simulacion = simulacion;
        setSpacing(20);
        setPadding(new Insets(10));

        Label titulo = new Label("Resultado de la carrera");
        titulo.getStyleClass().add("titulo-principal");

        Label subtitulo = new Label(circuito.getNombre() + "  ·  " + circuito.getPais()
                + "  ·  Clima: " + simulacion.getClimaReal().getEtiqueta()
                + "  ·  " + circuito.getVueltas() + " vueltas");
        subtitulo.getStyleClass().add("texto-secundario");

        VBox tabla = new VBox(2);
        tabla.getStyleClass().add("panel");
        tabla.setPadding(new Insets(10));

        var resultados = simulacion.getResultados();
        double tiempoLider = resultados.get(0).getTiempoSegundos();

        for (ResultadoCarrera r : resultados) {
            tabla.getChildren().add(construirFila(r, tiempoLider));
        }

        ScrollPane scroll = new ScrollPane(tabla);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(460);
        scroll.setPrefWidth(620);
        scroll.getStyleClass().add("scroll-oscuro");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        panelDetalle.getStyleClass().add("panel");
        panelDetalle.setPadding(new Insets(24));
        panelDetalle.setPrefSize(480, 460);
        panelDetalle.setMinWidth(380);

        HBox cuerpo = new HBox(24, scroll, panelDetalle);
        cuerpo.setAlignment(Pos.TOP_CENTER);

        Button boton1 = new Button(textoBoton1);
        boton1.getStyleClass().add("boton-primario");
        boton1.setOnAction(e -> accionBoton1.run());

        Button boton2 = new Button(textoBoton2);
        boton2.getStyleClass().add("boton-secundario");
        boton2.setOnAction(e -> accionBoton2.run());

        HBox botones = new HBox(14, boton1, boton2);
        botones.setAlignment(Pos.CENTER);
        botones.setPadding(new Insets(10, 0, 0, 0));

        VBox contenedorCentrado = new VBox(18, titulo, subtitulo, cuerpo, botones);
        contenedorCentrado.setAlignment(Pos.TOP_CENTER);
        getChildren().add(contenedorCentrado);

        // Por defecto se muestra la ficha del ganador
        mostrarDetalle(resultados.get(0), tiempoLider);
    }

    private HBox construirFila(ResultadoCarrera r, double tiempoLider) {
        boolean esGanador = r.getPosicion() == 1;

        Label posicion = new Label("P " + r.getPosicion());
        posicion.setPrefWidth(50);
        posicion.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        posicion.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: "
                + (esGanador ? "#ffd400" : "#f5f6fa") + ";");

        Label nombre = new Label(r.getPiloto().getNombre());
        nombre.setPrefWidth(190);
        nombre.setStyle("-fx-font-weight: bold; -fx-text-fill: #f5f6fa; -fx-font-size: 14px;");

        Label equipo = new Label(r.getPiloto().getEquipo());
        equipo.getStyleClass().add("texto-secundario");
        equipo.setPrefWidth(190);

        Label tiempo = new Label("Tiempo: " + r.getDiferenciaFormateada(tiempoLider));
        tiempo.getStyleClass().add(esGanador ? "texto-rojo" : "texto-normal");

        Label promedio = new Label("Promedio/vuelta: " + ResultadoCarrera.formatearTiempo(r.getTiempoPromedioVuelta()));
        promedio.getStyleClass().add("texto-secundario");

        VBox columnaTiempo = new VBox(2, tiempo, promedio);
        columnaTiempo.setPrefWidth(220);

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        HBox fila = new HBox(10, posicion, nombre, equipo, espaciador, columnaTiempo);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(12, 16, 12, 16));
        fila.getStyleClass().add("fila-lista");
        fila.setOnMouseClicked(e -> seleccionar(fila, r, tiempoLider));
        return fila;
    }

    private void seleccionar(HBox fila, ResultadoCarrera r, double tiempoLider) {
        if (filaSeleccionada != null) {
            filaSeleccionada.getStyleClass().remove("fila-lista-seleccionada");
            filaSeleccionada.getStyleClass().add("fila-lista");
        }
        fila.getStyleClass().remove("fila-lista");
        fila.getStyleClass().add("fila-lista-seleccionada");
        filaSeleccionada = fila;
        mostrarDetalle(r, tiempoLider);
    }

    private void mostrarDetalle(ResultadoCarrera r, double tiempoLider) {
        StackPane avatar = IconFactory.avatarPiloto(r.getPiloto(), 70);

        Label nombre = new Label(r.getPiloto().getNombre());
        nombre.getStyleClass().add("titulo-seccion");

        Label equipoRol = new Label(r.getPiloto().getEquipo() + "  ·  " + r.getPiloto().getRol().getEtiqueta());
        equipoRol.getStyleClass().add("texto-rojo");

        HBox encabezado = new HBox(18, avatar, new VBox(6, nombre, equipoRol));
        encabezado.setAlignment(Pos.CENTER_LEFT);

        Label vehiculo = new Label("Vehículo: " + (r.getMonoplaza() != null ? r.getMonoplaza().getModelo() : "N/D"));
        Label velocidadMax = new Label(String.format("Velocidad máxima alcanzada: %.0f km/h", r.getVelocidadMaximaAlcanzada()));
        Label posicionFinal = new Label("Posición final: P" + r.getPosicion());
        Label tiempoTotal = new Label("Tiempo total: " + r.getDiferenciaFormateada(tiempoLider));
        Label promedio = new Label("Promedio por vuelta: " + ResultadoCarrera.formatearTiempo(r.getTiempoPromedioVuelta()));
        String neumatico = r.getMonoplaza() != null && r.getMonoplaza().getTipoNeumatico() != null
                ? r.getMonoplaza().getTipoNeumatico().getEtiqueta() : "N/D";
        Label desgaste = new Label(String.format("Neumático: %s  ·  Desgaste de las llantas: %.0f/100", neumatico, r.getDesgasteFinal()));
        List<Integer> paradas = r.getParadasEnBoxes();
        String textoParadas = paradas.isEmpty() ? "Sin paradas en boxes"
                : "Paradas en boxes: vuelta " + paradas.stream().map(String::valueOf).collect(Collectors.joining(", vuelta "));
        Label pits = new Label(textoParadas);
        for (Label l : List.of(vehiculo, velocidadMax, posicionFinal, tiempoTotal, promedio, desgaste, pits)) {
            l.getStyleClass().add("texto-normal");
        }

        Label tituloVueltas = new Label(r.isDnf() ? "Tiempos por vuelta (antes del choque)" : "Tiempos por vuelta");
        tituloVueltas.getStyleClass().add("etiqueta-campo");

        VBox listaVueltas = new VBox(4);
        List<Double> vueltas = r.getTiemposPorVuelta();
        int vueltasCompletadas = r.getVueltasCompletadas();
        for (int i = 0; i < vueltasCompletadas; i++) {
            Label linea = new Label("Vuelta " + (i + 1) + ":  " + ResultadoCarrera.formatearTiempo(vueltas.get(i)));
            linea.getStyleClass().add("texto-secundario");
            listaVueltas.getChildren().add(linea);
        }
        ScrollPane scrollVueltas = new ScrollPane(listaVueltas);
        scrollVueltas.setFitToWidth(true);
        scrollVueltas.setPrefHeight(200);
        scrollVueltas.getStyleClass().add("scroll-oscuro");
        scrollVueltas.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox contenido = new VBox(12, encabezado, vehiculo, velocidadMax, posicionFinal, tiempoTotal, promedio,
                desgaste, pits, tituloVueltas, scrollVueltas);
        if (r.isDnf()) {
            contenido.getChildren().add(construirSeccionFotosChoque(r));
        }

        ScrollPane scrollDetalle = new ScrollPane(contenido);
        scrollDetalle.setFitToWidth(true);
        scrollDetalle.getStyleClass().add("scroll-oscuro");
        scrollDetalle.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        panelDetalle.getChildren().setAll(scrollDetalle);
    }

    /** Las 3 fotos del choque de este piloto, con el título según si fue en solitario o contra otro. */
    private VBox construirSeccionFotosChoque(ResultadoCarrera r) {
        String titulo = r.esChoqueGrupal()
                ? "Imágenes del choque entre " + r.getPiloto().getNombre() + " y " + r.getRivalChoque().getNombre()
                : "Imagen de choque de " + r.getPiloto().getNombre();

        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("etiqueta-campo");
        etiqueta.setWrapText(true);

        FlowPane filaFotos = new FlowPane(10, 10);
        for (Image foto : FotosChoque.paraChoque(simulacion, r)) {
            ImageView vista = new ImageView(foto);
            vista.setPreserveRatio(true);
            vista.setFitWidth(170);
            filaFotos.getChildren().add(vista);
        }

        return new VBox(8, etiqueta, filaFotos);
    }
}