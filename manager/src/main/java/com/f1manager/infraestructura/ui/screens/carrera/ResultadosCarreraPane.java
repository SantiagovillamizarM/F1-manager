package com.f1manager.infraestructura.ui.screens.carrera;

import com.f1manager.dominio.modelo.Circuito;
import com.f1manager.dominio.modelo.ResultadoCarrera;
import com.f1manager.dominio.servicio.SimuladorCarrera;
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

import java.util.List;
import java.util.stream.Collectors;

public class ResultadosCarreraPane extends VBox {

    private final StackPane panelDetalle = new StackPane();
    private HBox filaSeleccionada;

    public ResultadosCarreraPane(Circuito circuito, SimuladorCarrera.ResultadoSimulacion simulacion,
                                  Runnable nuevaCarrera, Runnable alMenu) {
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

        Button botonNuevaCarrera = new Button("NUEVA CARRERA");
        botonNuevaCarrera.getStyleClass().add("boton-primario");
        botonNuevaCarrera.setOnAction(e -> nuevaCarrera.run());

        Button botonMenu = new Button("MENÚ PRINCIPAL");
        botonMenu.getStyleClass().add("boton-secundario");
        botonMenu.setOnAction(e -> alMenu.run());

        HBox botones = new HBox(14, botonNuevaCarrera, botonMenu);
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
        StackPane casco = IconFactory.contenedor(IconFactory.casco(IconFactory.BLANCO), 70);

        Label nombre = new Label(r.getPiloto().getNombre());
        nombre.getStyleClass().add("titulo-seccion");

        Label equipoRol = new Label(r.getPiloto().getEquipo() + "  ·  " + r.getPiloto().getRol().getEtiqueta());
        equipoRol.getStyleClass().add("texto-rojo");

        HBox encabezado = new HBox(18, casco, new VBox(6, nombre, equipoRol));
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
        panelDetalle.getChildren().setAll(contenido);
    }
}