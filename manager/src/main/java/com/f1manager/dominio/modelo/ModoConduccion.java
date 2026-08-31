//Define los tres modos de conducción
//(Normal, Agresivo, Ahorro) y cuánto afecta cada uno al ritmo, al riesgo y al desgaste de neumáticos.

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;
public enum ModoConduccion {
    NORMAL("Normal", 1.000, 1.00, 1.00),
    AGRESIVO("Agresivo", 0.975, 1.60, 1.35),
    AHORRO("Ahorro", 1.020, 0.65, 0.70);

    private final String etiqueta;
    private final double factorRitmo;
    private final double factorVariabilidad;
    private final double factorDesgasteNeumatico;

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
    ModoConduccion(String etiqueta, double factorRitmo, double factorVariabilidad, double factorDesgasteNeumatico) {
        this.etiqueta = etiqueta;
        this.factorRitmo = factorRitmo;
        this.factorVariabilidad = factorVariabilidad;
        this.factorDesgasteNeumatico = factorDesgasteNeumatico;
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

    /** Multiplicador del ritmo de desgaste de neumáticos (mayor = se gastan más rápido). */
    public double getFactorDesgasteNeumatico() {
        return factorDesgasteNeumatico;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
