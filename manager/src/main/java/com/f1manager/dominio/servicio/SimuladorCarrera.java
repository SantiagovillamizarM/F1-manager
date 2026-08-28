//Calcula el resultado de una carrera combinando piloto, vehículo, clima y configuración, 
//con una fórmula matemática más una variación aleatoria controlada.
package com.f1manager.dominio.servicio;
import com.f1manager.dominio.modelo.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.DoublePredicate;
import java.util.function.Function;

/**
    formula:
 
    tiempoBase = vueltas * segundosPorKmBase * longitudKm
 
    Se aplican multiplicadores según:
      - Habilidad del piloto (0 a 100).
      - Años de experiencia del piloto (pequeño bono).
      - Velocidad máxima y aceleración del monoplaza.
      - Carga aerodinámica configurada (agarre / velocidad punta).
      - Modo de conducción configurado (ritmo / riesgo).
      - Clima seleccionado (tiempo / variabilidad).
 
    Finalmente se añade una variación aleatoria acotada (ruido de carrera)
    para que el resultado no sea siempre idéntico entre simulaciones.
 */
public class SimuladorCarrera {

    private static final double SEGUNDOS_POR_KM_BASE = 24.0;

    private final Random random = new Random();


    /**
     * @param esCurvaEnFraccion indica, para una fracción de vuelta (0.0 a 1.0), si ese punto
     *                          del trazado real es una curva. La inyecta quien llama (la UI,
     *                          que ya conoce la forma del circuito) para que el dominio no
     *                          dependa de la clase que dibuja la pista.
     */
    public ResultadoSimulacion simular(Circuito circuito, Clima climaElegido, List<Piloto> pilotos,
                                        Function<Piloto, Monoplaza> monoplazaDe, DoublePredicate esCurvaEnFraccion) {

        Clima climaReal = climaElegido.resolver(random);
        double tiempoBase = circuito.getVueltas() * SEGUNDOS_POR_KM_BASE * circuito.getLongitudKm();

        List<ResultadoCarrera> resultados = new ArrayList<>();

        for (Piloto piloto : pilotos) {

            Monoplaza monoplaza = monoplazaDe.apply(piloto);
            double tiempo = calcularTiempoPiloto(tiempoBase, piloto, monoplaza, climaReal);
            ResultadoCarrera resultado = new ResultadoCarrera(piloto, monoplaza, tiempo);
            resultado.setTiemposPorVuelta(generarTiemposPorVuelta(tiempo, circuito.getVueltas()));
            resultado.setVelocidadMaximaAlcanzada(generarVelocidadMaxima(monoplaza));
            resultados.add(resultado);
        }

        aplicarChoques(circuito.getVueltas(), esCurvaEnFraccion, resultados);

        resultados.sort(comparadorClasificacion());
        for (int i = 0; i < resultados.size(); i++) {
            resultados.get(i).setPosicion(i + 1);
        }

        return new ResultadoSimulacion(climaReal, resultados);
    }

    private static Comparator<ResultadoCarrera> comparadorClasificacion() {
        return (a, b) -> {
            if (a.isDnf() != b.isDnf()) {
                return a.isDnf() ? 1 : -1; // los que no terminan quedan siempre después
            }
            if (a.isDnf()) {
                // Entre los que no terminaron, gana quien llegó más lejos antes de chocar.
                return Double.compare(b.getProgresoChoque(), a.getProgresoChoque());
            }
            return Double.compare(a.getTiempoSegundos(), b.getTiempoSegundos());
        };
    }

    // Cuántas veces por vuelta se evalúa el riesgo de choque (repartidas a lo largo del trazado).
    private static final int CHEQUEOS_POR_VUELTA = 8;
    // Probabilidad base de choque por chequeo, antes de aplicar la habilidad del piloto.
    private static final double PROB_BASE_CHOQUE = 0.0005;
    // De los choques que ocurren, qué proporción involucra también a otro piloto (ambos DNF).
    private static final double PROB_CHOQUE_CON_RIVAL = 0.25;

    /**
     * Recorre la carrera vuelta por vuelta y, en varios puntos de cada una, evalúa si algún
     * piloto choca. La probabilidad es baja pero nunca cero, y depende de su habilidad de
     * adelantamiento combinada con su habilidad en curva o en recta (según el tramo real del
     * trazado). Un choque puede además involucrar al rival con el que más cerca esté peleando
     * en ese momento, dejando a ambos fuera de carrera (DNF) en el mismo punto.
     */
    private void aplicarChoques(int vueltas, DoublePredicate esCurvaEnFraccion, List<ResultadoCarrera> resultados) {
        for (int vuelta = 1; vuelta <= vueltas; vuelta++) {
            for (int chequeo = 0; chequeo < CHEQUEOS_POR_VUELTA; chequeo++) {
                double fraccionVuelta = chequeo / (double) CHEQUEOS_POR_VUELTA;
                boolean curva = esCurvaEnFraccion.test(fraccionVuelta);

                for (ResultadoCarrera resultado : resultados) {
                    if (resultado.isDnf()) {
                        continue;
                    }
                    double probabilidad = probabilidadChoque(resultado.getPiloto(), curva);
                    if (random.nextDouble() >= probabilidad) {
                        continue;
                    }

                    double progreso = (vuelta - 1) + fraccionVuelta;
                    resultado.marcarChoque(progreso);

                    if (random.nextDouble() < PROB_CHOQUE_CON_RIVAL) {
                        ResultadoCarrera rival = buscarRivalMasCercano(resultado, vuelta, fraccionVuelta, resultados);
                        if (rival != null) {
                            rival.marcarChoque(progreso);
                        }
                    }
                }
            }
        }
    }

    /** Probabilidad de choque en un chequeo: más baja cuanto mejor sea la habilidad relevante, nunca 0. */
    private double probabilidadChoque(Piloto piloto, boolean curva) {
        int habilidadTramo = curva ? piloto.getHabilidadCurva() : piloto.getHabilidadRecta();
        double habilidadPromedioRelevante = (habilidadTramo + piloto.getHabilidadAdelantamiento()) / 2.0;
        double factorRiesgo = Math.max(0.15, (100 - habilidadPromedioRelevante) / 100.0);
        return PROB_BASE_CHOQUE * factorRiesgo;
    }

    /** Entre los pilotos que siguen en carrera, el que tenga el tiempo acumulado más parecido (pelea rueda a rueda). */
    private ResultadoCarrera buscarRivalMasCercano(ResultadoCarrera propio, int vuelta, double fraccionVuelta,
                                                    List<ResultadoCarrera> resultados) {
        double tiempoPropio = tiempoAcumuladoHasta(propio, vuelta, fraccionVuelta);
        ResultadoCarrera masCercano = null;
        double menorDiferencia = Double.MAX_VALUE;
        for (ResultadoCarrera candidato : resultados) {
            if (candidato == propio || candidato.isDnf()) {
                continue;
            }
            double diferencia = Math.abs(tiempoAcumuladoHasta(candidato, vuelta, fraccionVuelta) - tiempoPropio);
            if (diferencia < menorDiferencia) {
                menorDiferencia = diferencia;
                masCercano = candidato;
            }
        }
        return masCercano;
    }

    /** Tiempo acumulado de un piloto hasta cierta fracción de una vuelta, usando sus tiempos por vuelta ya generados. */
    private double tiempoAcumuladoHasta(ResultadoCarrera resultado, int vuelta, double fraccionVuelta) {
        List<Double> tiempos = resultado.getTiemposPorVuelta();
        double acumulado = 0;
        for (int i = 0; i < vuelta - 1; i++) {
            acumulado += tiempos.get(i);
        }
        acumulado += tiempos.get(vuelta - 1) * fraccionVuelta;
        return acumulado;
    }

    /** Selecciona la habilidad del piloto correspondiente al clima real de la carrera. */
    private int habilidadPorClima(Piloto piloto, Clima climaReal) {
        return switch (climaReal) {
            case SECO -> piloto.getHabilidadSeco();
            case LLUVIOSO -> piloto.getHabilidadLluvia();
            case EXTREMO -> piloto.getHabilidadExtremo();
            case ALEATORIO -> piloto.getHabilidadSeco(); // climaReal ya viene resuelto, no debería ocurrir
        };
    }

    private double calcularTiempoPiloto(double tiempoBase, Piloto piloto, Monoplaza monoplaza, Clima climaReal) {
        double tiempo = tiempoBase;

        // --- Habilidad del piloto en el clima real de la carrera: hasta ±10% según qué tan lejos esté de 80 ---
        double factorHabilidad = 1.0 + ((80 - habilidadPorClima(piloto, climaReal)) / 100.0) * 0.20;
        tiempo *= factorHabilidad;

        // --- Experiencia: pequeño bono de hasta -3% con 20+ años ---
        double factorExperiencia = 1.0 - Math.min(piloto.getExperienciaAnios(), 20) * 0.0015;
        tiempo *= factorExperiencia;

        // --- Vehículo: velocidad máxima y aceleración ---
        if (monoplaza != null) {
            double factorVelocidad = 1.0 + ((350 - monoplaza.getVelocidadMaxKmh()) / 350.0) * 0.10;
            tiempo *= factorVelocidad;

            double factorAceleracion = 1.0 + ((monoplaza.getAceleracion0a100() - 2.5) * 0.02);
            tiempo *= factorAceleracion;

            // --- Configuración: carga aerodinámica y modo de conducción ---
            CargaAerodinamica carga = monoplaza.getCargaAerodinamica();
            tiempo *= carga.getFactorVelocidadPunta();
            tiempo *= carga.getFactorAgarre();

            ModoConduccion modo = monoplaza.getModoConduccion();
            tiempo *= modo.getFactorRitmo();
        }

        // --- Clima ---
        tiempo *= climaReal.getFactorTiempo();

        // --- Variabilidad aleatoria acotada (ruido de carrera) ---
        double variabilidadBase = 0.012; // 1.2% de variación estándar en condiciones normales
        double factorVariabilidadModo = monoplaza != null ? monoplaza.getModoConduccion().getFactorVariabilidad() : 1.0;
        double desviacion = tiempo * variabilidadBase * factorVariabilidadModo * (climaReal.getFactorVariabilidad() <= 0 ? 1.0 : climaReal.getFactorVariabilidad());
        double ruido = random.nextGaussian() * desviacion;
        // Se acota el ruido para evitar valores extremos poco realistas
        double limite = tiempo * 0.06 * factorVariabilidadModo;
        ruido = Math.max(-limite, Math.min(limite, ruido));

        tiempo += ruido;

        return Math.max(tiempo, tiempoBase * 0.5); // seguridad ante configuraciones extremas
    }
        private List<Double> generarTiemposPorVuelta(double tiempoTotal, int vueltas) {
        List<Double> tiempos = new ArrayList<>();
        double promedio = tiempoTotal / vueltas;
        double sumaGenerada = 0;
        for (int i = 0; i < vueltas; i++) {
            double variacion = 1 + (random.nextGaussian() * 0.02); // ±2% entre vueltas
            double vuelta = promedio * variacion;
            tiempos.add(vuelta);
            sumaGenerada += vuelta;
        }
        double factorAjuste = tiempoTotal / sumaGenerada; // para que la suma coincida con el tiempo total
        for (int i = 0; i < tiempos.size(); i++) {
            tiempos.set(i, tiempos.get(i) * factorAjuste);
        }
        return tiempos;
    }

    private double generarVelocidadMaxima(Monoplaza monoplaza) {
        if (monoplaza == null) return 0;
        double factor = 0.96 + random.nextDouble() * 0.06; // 96% a 102% de la velocidad máxima del auto
        return monoplaza.getVelocidadMaxKmh() * factor;
    }

    /** Resultado envolvente que incluye el clima realmente utilizado en la simulación. */
    public static final class ResultadoSimulacion {
        private final Clima climaReal;
        private final List<ResultadoCarrera> resultados;

        public ResultadoSimulacion(Clima climaReal, List<ResultadoCarrera> resultados) {
            this.climaReal = climaReal;
            this.resultados = resultados;
        }

        public Clima getClimaReal() {
            return climaReal;
        }

        public List<ResultadoCarrera> getResultados() {
            return resultados;
        }
    }
}
