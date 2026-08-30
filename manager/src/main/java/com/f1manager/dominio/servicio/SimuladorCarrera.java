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
    // Neumático adecuado para el clima real (ayuda) vs. inadecuado (se resiente / "se desgasta más rápido").
    private static final double FACTOR_NEUMATICO_ACERTADO = 0.97;
    private static final double FACTOR_NEUMATICO_INADECUADO = 1.08;
    // Cuánto más rápido se desgasta el neumático cuando el compuesto no corresponde al clima real.
    private static final double MULTIPLICADOR_DESGASTE_INADECUADO = 1.8;
    // Cuánto puede llegar a ralentizar un neumático 100% desgastado, comparado con uno nuevo.
    private static final double PENALIZACION_MAX_POR_DESGASTE = 0.15;
    // Cuánto crece la variabilidad (para bien o para mal) del tiempo por cada PSI de distancia
    // respecto a la presión óptima, cuando ninguna combinación favorece una dirección clara.
    // En la presión óptima el efecto es exactamente 0.
    private static final double ESCALA_EFECTO_PRESION = 0.01;
    // Cuánto ayuda (o perjudica) por PSI acertar (o no) la dirección de presión favorable,
    // cuando sí hay una combinación de compuesto/modo/carga/clima que la determina.
    private static final double ESCALA_EFECTO_PRESION_DIRIGIDA = 0.012;

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
            double tiempoPromedioPorVuelta = tiempo / circuito.getVueltas();
            VueltasSimuladas vueltas = simularVueltas(monoplaza, climaReal, circuito.getVueltas(), tiempoPromedioPorVuelta);
            double tiempoFinal = vueltas.tiempos().stream().mapToDouble(Double::doubleValue).sum();

            ResultadoCarrera resultado = new ResultadoCarrera(piloto, monoplaza, tiempoFinal);
            resultado.setTiemposPorVuelta(vueltas.tiempos());
            resultado.setDesgastePorVuelta(vueltas.desgaste());
            resultado.setVueltasDePit(vueltas.vueltasDePit());
            resultado.setTemperaturaLlantasPorVuelta(vueltas.temperaturaLlantas());
            resultado.setTemperaturaMotorPorVuelta(vueltas.temperaturaMotor());
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
                    boolean desgasteAlMaximo = vuelta - 1 < resultado.getDesgastePorVuelta().size()
                            && resultado.getDesgastePorVuelta().get(vuelta - 1) >= UMBRAL_DESGASTE_RIESGOSO;
                    double probabilidad = probabilidadChoque(resultado.getPiloto(), curva, desgasteAlMaximo);
                    if (random.nextDouble() >= probabilidad) {
                        continue;
                    }

                    double progreso = (vuelta - 1) + fraccionVuelta;
                    resultado.marcarChoque(progreso);

                    if (random.nextDouble() < PROB_CHOQUE_CON_RIVAL) {
                        ResultadoCarrera rival = buscarRivalMasCercano(resultado, vuelta, fraccionVuelta, resultados);
                        if (rival != null) {
                            rival.marcarChoque(progreso);
                            resultado.setRivalChoque(rival.getPiloto());
                            rival.setRivalChoque(resultado.getPiloto());
                        }
                    }
                }
            }
        }
    }

    /**
     * Probabilidad de choque en un chequeo: más baja cuanto mejor sea la habilidad relevante,
     * nunca 0, y un poco más alta si en ese momento el neumático está al límite de desgaste.
     */
    private double probabilidadChoque(Piloto piloto, boolean curva, boolean desgasteAlMaximo) {
        int habilidadTramo = curva ? piloto.getHabilidadCurva() : piloto.getHabilidadRecta();
        double habilidadPromedioRelevante = (habilidadTramo + piloto.getHabilidadAdelantamiento()) / 2.0;
        double factorRiesgo = Math.max(0.15, (100 - habilidadPromedioRelevante) / 100.0);
        double probabilidad = PROB_BASE_CHOQUE * factorRiesgo;
        if (desgasteAlMaximo) {
            probabilidad *= MULTIPLICADOR_RIESGO_DESGASTE;
        }
        return probabilidad;
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

            // --- Neumáticos: ritmo propio del compuesto, y acierto o error según el clima ---
            TipoNeumatico neumatico = monoplaza.getTipoNeumatico();
            if (neumatico != null) {
                tiempo *= neumatico.getFactorRitmo();
                boolean climaMojado = climaReal == Clima.LLUVIOSO || climaReal == Clima.EXTREMO;
                tiempo *= (neumatico.isParaLluvia() == climaMojado)
                        ? FACTOR_NEUMATICO_ACERTADO
                        : FACTOR_NEUMATICO_INADECUADO;
            }

            // --- Presión de aire: en el valor óptimo no cambia nada. Si la combinación actual
            // (compuesto, modo, carga, clima) favorece presión alta o baja, acertar la dirección
            // ayuda y equivocarla perjudica; si ninguna combinación aplica, no hay una dirección
            // "correcta" que se pueda saber de antemano, así que el efecto es puro azar ---
            double desviacionFirmada = monoplaza.getPresionAire() - Monoplaza.PRESION_OPTIMA;
            int direccionFavorable = direccionPresionFavorable(neumatico, climaReal, modo, carga);
            if (direccionFavorable == 0) {
                if (desviacionFirmada != 0) {
                    tiempo *= 1.0 + random.nextGaussian() * ESCALA_EFECTO_PRESION * Math.abs(desviacionFirmada);
                }
            } else {
                double alineacion = direccionFavorable * desviacionFirmada; // >0 si acertó la dirección
                tiempo *= 1.0 - alineacion * ESCALA_EFECTO_PRESION_DIRIGIDA;
                if (desviacionFirmada != 0) {
                    // Un poco de azar residual: acertar la dirección ayuda en promedio, pero no es infalible.
                    tiempo *= 1.0 + random.nextGaussian() * ESCALA_EFECTO_PRESION * 0.3 * Math.abs(desviacionFirmada);
                }
            }
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

    /**
     * Determina si, para esta combinación de compuesto, clima real, modo de conducción y carga
     * aerodinámica, conviene presión alta (+1), baja (-1), o no hay una dirección clara (0):
     *  - Blando + Agresivo + Seco -> alta (controla el calor y desgaste extremos de esa combinación)
     *  - Duro + Ahorro -> baja (ayuda a poner en temperatura un compuesto que ya cuesta calentar)
     *  - Carga aerodinámica Baja -> alta (setup de velocidad punta: menos resistencia a la rodadura)
     *  - Carga aerodinámica Alta -> baja (setup de curvas técnicas: más agarre mecánico)
     *  - Neumático de lluvia (intermedio o lluvia) en pista mojada -> baja (más agarre mecánico)
     * Cuando varias combinaciones aplican a la vez, se suman; si no aplica ninguna (ej. Medio +
     * Normal + Seco), el resultado es 0 y no hay dirección favorable.
     */
    private int direccionPresionFavorable(TipoNeumatico neumatico, Clima climaReal, ModoConduccion modo, CargaAerodinamica carga) {
        boolean climaMojado = climaReal == Clima.LLUVIOSO || climaReal == Clima.EXTREMO;
        int puntaje = 0;

        if (neumatico == TipoNeumatico.BLANDO && modo == ModoConduccion.AGRESIVO && !climaMojado) {
            puntaje += 1;
        }
        if (neumatico == TipoNeumatico.DURO && modo == ModoConduccion.AHORRO) {
            puntaje -= 1;
        }
        if (carga == CargaAerodinamica.BAJA) {
            puntaje += 1;
        }
        if (carga == CargaAerodinamica.ALTA) {
            puntaje -= 1;
        }
        if (neumatico != null && neumatico.isParaLluvia() && climaMojado) {
            puntaje -= 1;
        }

        return Integer.signum(puntaje);
    }

    // A partir de qué nivel de desgaste (cercano al máximo de 100) el piloto entra a boxes a cambiar neumáticos.
    private static final double UMBRAL_DESGASTE_PIT = 95.0;
    // Tiempo perdido por una parada en boxes (calle de pits + cambio de neumáticos), en segundos.
    private static final double TIEMPO_PERDIDO_EN_PIT = 22.0;
    // Desgaste a partir del cual, mientras recorre ese tramo, el riesgo de choque es un poco mayor.
    private static final double UMBRAL_DESGASTE_RIESGOSO = 95.0;
    private static final double MULTIPLICADOR_RIESGO_DESGASTE = 1.6;

    // Temperaturas de referencia (°C) en condiciones normales, antes de aplicar compuesto/modo/clima/desgaste.
    private static final double TEMP_LLANTA_BASE = 90.0;
    private static final double TEMP_MOTOR_BASE = 95.0;

    /** Resultado de simular vuelta por vuelta a un piloto: tiempos, desgaste, paradas y temperaturas. */
    private record VueltasSimuladas(List<Double> tiempos, List<Double> desgaste, List<Integer> vueltasDePit,
                                     List<Double> temperaturaLlantas, List<Double> temperaturaMotor) {
    }

    /** Cuánto más caliente (o frío) corre cada compuesto, comparado con la temperatura base de neumático. */
    private double factorTemperaturaCompuesto(TipoNeumatico neumatico) {
        if (neumatico == null) {
            return 0.0;
        }
        return switch (neumatico) {
            case BLANDO -> 8.0;
            case MEDIO -> 3.0;
            case DURO -> -4.0;
            case INTERMEDIO -> -3.0;
            case LLUVIA -> -6.0;
        };
    }

    /**
     * Simula la carrera de un piloto vuelta por vuelta: el ritmo base varía un poco entre
     * vueltas, y el desgaste de neumáticos (que se acelera si el compuesto no corresponde al
     * clima real) penaliza progresivamente el tiempo. Al alcanzar un desgaste alto, el piloto
     * entra a boxes: pierde tiempo en la parada, cambia de neumáticos (a uno adecuado para el
     * clima si el actual no lo era) y el desgaste vuelve a 0.
     */
    private VueltasSimuladas simularVueltas(Monoplaza monoplaza, Clima climaReal, int vueltas, double tiempoPromedioPorVuelta) {
        List<Double> tiempos = new ArrayList<>();
        List<Double> desgaste = new ArrayList<>();
        List<Integer> vueltasDePit = new ArrayList<>();
        List<Double> temperaturaLlantas = new ArrayList<>();
        List<Double> temperaturaMotor = new ArrayList<>();

        TipoNeumatico neumaticoActual = monoplaza != null ? monoplaza.getTipoNeumatico() : null;
        boolean climaMojado = climaReal == Clima.LLUVIOSO || climaReal == Clima.EXTREMO;
        double acumuladoDesgaste = 0;

        // Cada piloto/auto tiene su propio ritmo de desgaste (no todos son iguales): depende del
        // modo de conducción configurado (agresivo desgasta más rápido, ahorro más lento) y de
        // una variación propia del auto/piloto, fija para toda la carrera (no cambia vuelta a vuelta).
        double factorModo = monoplaza != null ? monoplaza.getModoConduccion().getFactorDesgasteNeumatico() : 1.0;
        double factorIndividual = 0.85 + random.nextDouble() * 0.3; // entre 0.85 y 1.15, distinto por piloto

        for (int vuelta = 1; vuelta <= vueltas; vuelta++) {
            double variacionRitmo = 1 + random.nextGaussian() * 0.02; // ±2% entre vueltas
            double tiempoVuelta = tiempoPromedioPorVuelta * variacionRitmo;

            if (neumaticoActual != null) {
                double factorDesgaste = 1.0 + (acumuladoDesgaste / 100.0) * PENALIZACION_MAX_POR_DESGASTE;
                tiempoVuelta *= factorDesgaste;

                double ritmoDesgaste = neumaticoActual.getDesgastePorVuelta() * factorModo * factorIndividual;
                if (neumaticoActual.isParaLluvia() != climaMojado) {
                    ritmoDesgaste *= MULTIPLICADOR_DESGASTE_INADECUADO;
                }
                // Incremento parejo dentro de la propia carrera de este piloto (sin ruido vuelta a
                // vuelta): empieza en 0 y sube de forma predecible, a SU ritmo particular.
                acumuladoDesgaste = Math.min(100, acumuladoDesgaste + ritmoDesgaste);
            }

            // Se guarda el desgaste alcanzado AL RECORRER esta vuelta (antes de resetear por el
            // pit, si corresponde), para que quede registrado que se condujo con el neumático al
            // límite justo en esta vuelta.
            tiempos.add(tiempoVuelta);
            desgaste.add(acumuladoDesgaste);

            // Temperaturas: suben con compuestos más blandos, conducción más agresiva, más
            // desgaste acumulado, y bajan con clima mojado (refrigeración del agua). Con algo de
            // variación vuelta a vuelta para que se vea que van cambiando, no un número fijo.
            double tempLlanta = TEMP_LLANTA_BASE
                    + factorTemperaturaCompuesto(neumaticoActual)
                    + (factorModo - 1.0) * 10.0
                    + (acumuladoDesgaste / 100.0) * 8.0
                    + (climaMojado ? -10.0 : 0.0)
                    + random.nextGaussian() * 2.5;
            temperaturaLlantas.add(tempLlanta);

            double tempMotor = TEMP_MOTOR_BASE
                    + (factorModo - 1.0) * 15.0
                    + random.nextGaussian() * 3.0;
            temperaturaMotor.add(tempMotor);

            if (neumaticoActual != null && acumuladoDesgaste >= UMBRAL_DESGASTE_PIT && vuelta < vueltas) {
                tiempos.set(tiempos.size() - 1, tiempos.get(tiempos.size() - 1) + TIEMPO_PERDIDO_EN_PIT);
                vueltasDePit.add(vuelta);
                neumaticoActual = elegirNeumaticoParaPit(neumaticoActual, climaMojado);
                acumuladoDesgaste = 0;
            }
        }

        return new VueltasSimuladas(tiempos, desgaste, vueltasDePit, temperaturaLlantas, temperaturaMotor);
    }

    /** Al entrar a boxes: si el compuesto actual ya era el correcto para el clima, se pone un juego nuevo del mismo; si no, se cambia a uno adecuado. */
    private TipoNeumatico elegirNeumaticoParaPit(TipoNeumatico actual, boolean climaMojado) {
        if (actual.isParaLluvia() == climaMojado) {
            return actual;
        }
        return climaMojado ? TipoNeumatico.LLUVIA : TipoNeumatico.MEDIO;
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
