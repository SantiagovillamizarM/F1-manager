//Define las condiciones climáticas posibles 
//(Seco, Lluvioso, Extremo, Aleatorio) y cuánto afectan a la carrera.

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;

import java.util.Map;
import java.util.Random;
public enum Clima {
    SECO("Seco", 1.000, 1.00),
    LLUVIOSO("Lluvioso", 1.150, 1.80),
    EXTREMO("Extremo", 1.320, 2.60),
    ALEATORIO("Aleatorio", 0, 0);

    private final String etiqueta;
    private final double factorTiempo;
    private final double factorVariabilidad;

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
    Clima(String etiqueta, double factorTiempo, double factorVariabilidad) {
        this.etiqueta = etiqueta;
        this.factorTiempo = factorTiempo;
        this.factorVariabilidad = factorVariabilidad;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public double getFactorTiempo() {
        return factorTiempo;
    }

    public double getFactorVariabilidad() {
        return factorVariabilidad;
    }

    /**
     * Resuelve el clima real a utilizar en la simulación. Si es ALEATORIO,
     * sortea entre SECO, LLUVIOSO y EXTREMO con una distribución ponderada
     * (el clima seco es el más probable, el extremo el menos probable).
     */
    public Clima resolver(Random random) {
        if (this != ALEATORIO) {
            return this;
        }
        double valor = random.nextDouble();
        if (valor < 0.55) {
            return SECO;
        } else if (valor < 0.90) {
            return LLUVIOSO;
        } else {
            return EXTREMO;
        }
    }

    // Distribución pareja usada por resolver() y como respaldo de resolverDinamico() cuando el
    // país no coincide con ninguno de la tabla de abajo: {peso Seco, peso Lluvioso, peso Extremo}.
    private static final double[] PESOS_PREDETERMINADOS = {0.55, 0.35, 0.10};

    // Pesos por país (Seco, Lluvioso, Extremo) para el clima dinámico del modo Campeonato: se
    // busca por coincidencia de una palabra clave del país (en minúsculas); si no coincide con
    // ninguno, se usa PESOS_PREDETERMINADOS. Los climas históricamente más lluviosos (Bélgica,
    // Reino Unido, Japón, Brasil) pesan más hacia Lluvioso/Extremo; los desérticos (Baréin,
    // Arabia Saudita, Catar, Emiratos) casi siempre Seco.
    private static final Map<String, double[]> PESOS_POR_PAIS = Map.ofEntries(
            Map.entry("bélgica", new double[]{0.35, 0.45, 0.20}),
            Map.entry("reino unido", new double[]{0.40, 0.45, 0.15}),
            Map.entry("japón", new double[]{0.45, 0.40, 0.15}),
            Map.entry("brasil", new double[]{0.40, 0.40, 0.20}),
            Map.entry("singapur", new double[]{0.45, 0.40, 0.15}),
            Map.entry("países bajos", new double[]{0.45, 0.40, 0.15}),
            Map.entry("austria", new double[]{0.50, 0.38, 0.12}),
            Map.entry("italia", new double[]{0.65, 0.28, 0.07}),
            Map.entry("españa", new double[]{0.70, 0.25, 0.05}),
            Map.entry("méxico", new double[]{0.75, 0.20, 0.05}),
            Map.entry("hungría", new double[]{0.70, 0.24, 0.06}),
            Map.entry("baréin", new double[]{0.85, 0.12, 0.03}),
            Map.entry("arabia saudita", new double[]{0.85, 0.12, 0.03}),
            Map.entry("catar", new double[]{0.85, 0.12, 0.03}),
            Map.entry("emiratos árabes", new double[]{0.85, 0.12, 0.03})
    );

    /**
     * Como {@link #resolver}, pero pondera las probabilidades según el país del circuito (ej.
     * Bélgica o Reino Unido son más propensos a lluvia que Baréin), pensado para el clima
     * dinámico del modo Campeonato (ahí el jugador no elige clima: se decide solo por carrera).
     * Si el país no coincide con ninguno de la tabla, usa la misma distribución pareja de
     * {@link #resolver}.
     */
    public Clima resolverDinamico(Random random, String pais) {
        if (this != ALEATORIO) {
            return this;
        }
        double[] pesos = pesosParaPais(pais);
        double valor = random.nextDouble();
        if (valor < pesos[0]) {
            return SECO;
        } else if (valor < pesos[0] + pesos[1]) {
            return LLUVIOSO;
        } else {
            return EXTREMO;
        }
    }

    private static double[] pesosParaPais(String pais) {
        String normalizado = pais == null ? "" : pais.toLowerCase();
        for (var entrada : PESOS_POR_PAIS.entrySet()) {
            if (normalizado.contains(entrada.getKey())) {
                return entrada.getValue();
            }
        }
        return PESOS_PREDETERMINADOS;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}

