//Define las condiciones climáticas posibles 
//(Seco, Lluvioso, Extremo, Aleatorio) y cuánto afectan a la carrera.
package com.f1manager.dominio.modelo;

import java.util.Random;
public enum Clima {
    SECO("Seco", 1.000, 1.00),
    LLUVIOSO("Lluvioso", 1.150, 1.80),
    EXTREMO("Extremo", 1.320, 2.60),
    ALEATORIO("Aleatorio", 0, 0);

    private final String etiqueta;
    private final double factorTiempo;
    private final double factorVariabilidad;

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

    @Override
    public String toString() {
        return etiqueta;
    }
}

