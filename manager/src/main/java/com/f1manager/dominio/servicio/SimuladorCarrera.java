//Calcula el resultado de una carrera combinando piloto, vehículo, clima y configuración, 
//con una fórmula matemática más una variación aleatoria controlada.
package com.f1manager.dominio.servicio;
import com.f1manager.dominio.modelo.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

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


    public ResultadoSimulacion simular(Circuito circuito, Clima climaElegido, List<Piloto> pilotos,
                                        java.util.function.Function<Piloto, Monoplaza> monoplazaDe) {

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

        resultados.sort(Comparator.comparingDouble(ResultadoCarrera::getTiempoSegundos));
        for (int i = 0; i < resultados.size(); i++) {
            resultados.get(i).setPosicion(i + 1);
        }

        return new ResultadoSimulacion(climaReal, resultados);
    }

    private double calcularTiempoPiloto(double tiempoBase, Piloto piloto, Monoplaza monoplaza, Clima climaReal) {
        double tiempo = tiempoBase;

        // --- Habilidad del piloto: hasta ±10% según qué tan lejos esté de 80 ---
        double factorHabilidad = 1.0 + ((80 - piloto.getHabilidad()) / 100.0) * 0.20;
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
