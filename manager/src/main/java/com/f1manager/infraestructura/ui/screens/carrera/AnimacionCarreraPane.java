package com.f1manager.infraestructura.ui.screens.carrera;

import com.f1manager.infraestructura.persistencia.DataStore;
import com.f1manager.dominio.modelo.*;
import com.f1manager.dominio.servicio.SimuladorCarrera;
import com.f1manager.infraestructura.ui.util.PistaGenerador;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.function.Consumer;

/**
 * Anima la carrera sobre el trazado del circuito seleccionado. Los
 * resultados finales se calculan de antemano mediante el
 * {@link SimuladorCarrera} (para que el ganador quede determinado por la
 * lógica de simulación) y la animación representa visualmente ese mismo
 * resultado: cada piloto avanza a una velocidad proporcional a su tiempo
 * final, por lo que el orden de llegada de la animación coincide con la
 * clasificación real.
 */
public class AnimacionCarreraPane extends BorderPane {

    private static final long DURACION_ANIMACION_MS = 9000;
    private static final Color[] PALETA = {
            Color.web("#ff2b2b"), Color.web("#00d4ff"), Color.web("#ffd400"), Color.web("#39ff88"),
            Color.web("#ff8a00"), Color.web("#b388ff"), Color.web("#ff4fd8"), Color.web("#8bc34a"),
            Color.web("#40c4ff"), Color.web("#ff6e6e"), Color.web("#c0ca33"), Color.web("#90a4ae")
    };

    private final Canvas lienzo = new Canvas(760, 460);
    private final VBox columnaClasificacionEnVivo = new VBox(8);
    private final Circuito circuito;
    private final List<ResultadoCarrera> resultados;
    private final Clima climaReal;
    private final Map<Piloto, Color> colores = new LinkedHashMap<>();
    private final Map<Piloto, Double> factorVelocidad = new LinkedHashMap<>();
    private AnimationTimer timer;
    private long inicioMs = -1;

    public AnimacionCarreraPane(Circuito circuito, Clima climaElegido, Consumer<SimuladorCarrera.ResultadoSimulacion> alFinalizar) {
        this.circuito = circuito;

        SimuladorCarrera simulador = new SimuladorCarrera();
        List<Piloto> pilotos = DataStore.getInstancia().getPilotos();
        SimuladorCarrera.ResultadoSimulacion simulacion = simulador.simular(circuito, climaElegido, pilotos,
                p -> DataStore.getInstancia().getVehiculoPorEquipo(p.getEquipo()));
        this.resultados = simulacion.getResultados();
        this.climaReal = simulacion.getClimaReal();

        double tiempoLider = resultados.get(0).getTiempoSegundos();
        int i = 0;
        for (ResultadoCarrera r : resultados) {
            colores.put(r.getPiloto(), PALETA[i % PALETA.length]);
            factorVelocidad.put(r.getPiloto(), tiempoLider / r.getTiempoSegundos());
            i++;
        }

        setPadding(new Insets(10));

        Label titulo = new Label("Carrera en curso — " + circuito.getNombre() + "  ·  Clima: " + climaReal.getEtiqueta());
        titulo.getStyleClass().add("titulo-seccion");
        VBox cajaTitulo = new VBox(titulo);
        cajaTitulo.setPadding(new Insets(0, 0, 16, 0));
        setTop(cajaTitulo);

        StackPane contenedorLienzo = new StackPane(lienzo);
        contenedorLienzo.getStyleClass().add("panel");
        contenedorLienzo.setPadding(new Insets(16));
        setCenter(contenedorLienzo);

        Label tituloClasificacion = new Label("EN VIVO");
        tituloClasificacion.getStyleClass().add("texto-rojo");
        VBox panelDerecho = new VBox(12, tituloClasificacion, columnaClasificacionEnVivo);
        panelDerecho.getStyleClass().add("panel");
        panelDerecho.setPadding(new Insets(18));
        panelDerecho.setPrefWidth(300);
        setRight(panelDerecho);
        BorderPane.setMargin(panelDerecho, new Insets(0, 0, 0, 16));

        iniciarAnimacion(alFinalizar, simulacion);
    }

    private void iniciarAnimacion(Consumer<SimuladorCarrera.ResultadoSimulacion> alFinalizar,
                                   SimuladorCarrera.ResultadoSimulacion simulacion) {
        PistaGenerador pista = PistaGenerador.paraCircuito(circuito);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long ahoraMs = now / 1_000_000;
                if (inicioMs < 0) {
                    inicioMs = ahoraMs;
                }
                long transcurrido = ahoraMs - inicioMs;
                double fraccionTiempo = Math.min(1.0, transcurrido / (double) DURACION_ANIMACION_MS);

                dibujarFrame(pista, fraccionTiempo, transcurrido);

                if (fraccionTiempo >= 1.0) {
                    stop();
                    alFinalizar.accept(simulacion);
                }
            }
        };
        timer.start();
    }

    private void dibujarFrame(PistaGenerador pista, double fraccionTiempo, long transcurridoMs) {
        GraphicsContext gc = lienzo.getGraphicsContext2D();
        double ancho = lienzo.getWidth();
        double alto = lienzo.getHeight();
        gc.clearRect(0, 0, ancho, alto);
        pista.dibujar(gc, 0, 0, ancho, alto, true);

        // Calcular avance de cada piloto y ordenarlos para la clasificación en vivo
        List<Piloto> ordenActual = new ArrayList<>(colores.keySet());
        Map<Piloto, Double> vueltasAvanzadas = new HashMap<>();
        for (Piloto p : ordenActual) {
            double vueltas = fraccionTiempo * circuito.getVueltas() * factorVelocidad.get(p);
            vueltasAvanzadas.put(p, vueltas);
        }
        ordenActual.sort((a, b) -> Double.compare(vueltasAvanzadas.get(b), vueltasAvanzadas.get(a)));

        // Dibujar cada piloto como un punto de color sobre la pista
        for (Piloto p : ordenActual) {
            double vueltas = vueltasAvanzadas.get(p);
            double fraccionVuelta = vueltas % 1.0;
            var punto = pista.posicionEnFraccion(fraccionVuelta, 0, 0, ancho, alto);
            Color color = colores.get(p);

            gc.setFill(color);
            gc.fillOval(punto.getX() - 6, punto.getY() - 6, 12, 12);
            gc.setStroke(Color.web("#05070d"));
            gc.setLineWidth(1.5);
            gc.strokeOval(punto.getX() - 6, punto.getY() - 6, 12, 12);
        }

        actualizarClasificacionEnVivo(ordenActual, vueltasAvanzadas, transcurridoMs);
    }

    private void actualizarClasificacionEnVivo(List<Piloto> orden, Map<Piloto, Double> vueltasAvanzadas, long transcurridoMs) {
        columnaClasificacionEnVivo.getChildren().clear();
        double segundosTranscurridos = transcurridoMs / 1000.0;
        String tiempo = ResultadoCarrera.formatearTiempo(segundosTranscurridos);

        int posicion = 1;
        for (Piloto p : orden) {
            int vuelta = Math.min(circuito.getVueltas(), (int) Math.floor(vueltasAvanzadas.get(p)) + 1);
            Label linea = new Label(posicion + "  " + p.getNombre() + "   ·   " + p.getEquipo()
                    + "   ·   Vuelta " + vuelta + "/" + circuito.getVueltas() + "   ·   " + tiempo);
            linea.setStyle("-fx-text-fill: " + toHex(colores.get(p)) + "; -fx-font-size: 11px; -fx-font-weight: bold;");
            linea.setWrapText(true);
            columnaClasificacionEnVivo.getChildren().add(linea);
            posicion++;
        }
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X", (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
    }
}
