//Define los tres modos de conducción 
//(Normal, Agresivo, Ahorro) y cuánto afecta cada uno al ritmo y al riesgo.
package com.f1manager.dominio.modelo;
public enum ModoConduccion {
    NORMAL("Normal", 1.000, 1.00),
    AGRESIVO("Agresivo", 0.975, 1.60),
    AHORRO("Ahorro", 1.020, 0.65);

    private final String etiqueta;
    private final double factorRitmo;
    private final double factorVariabilidad;

    ModoConduccion(String etiqueta, double factorRitmo, double factorVariabilidad) {
        this.etiqueta = etiqueta;
        this.factorRitmo = factorRitmo;
        this.factorVariabilidad = factorVariabilidad;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Multiplicador base del tiempo por vuelta (menor = más rápido). */
    public double getFactorRitmo() {
        return factorRitmo;
    }

    /** Multiplicador de la variabilidad aleatoria aplicada durante la simulación. */
    public double getFactorVariabilidad() {
        return factorVariabilidad;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
