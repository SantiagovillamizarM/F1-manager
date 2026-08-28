//Define los compuestos de neumático usados en la F1
//(Blando, Medio, Duro, Intermedio, Lluvia), cuánto afecta cada uno al ritmo
//y a qué ritmo se desgasta por vuelta en condiciones normales.
package com.f1manager.dominio.modelo;
public enum TipoNeumatico {
    BLANDO("Blando", 0.985, false, 1.4),
    MEDIO("Medio", 1.000, false, 1.0),
    DURO("Duro", 1.015, false, 0.6),
    INTERMEDIO("Intermedio", 1.000, true, 0.9),
    LLUVIA("Lluvia", 1.005, true, 0.8);

    private final String etiqueta;
    private final double factorRitmo;
    private final boolean paraLluvia;
    private final double desgastePorVuelta;

    TipoNeumatico(String etiqueta, double factorRitmo, boolean paraLluvia, double desgastePorVuelta) {
        this.etiqueta = etiqueta;
        this.factorRitmo = factorRitmo;
        this.paraLluvia = paraLluvia;
        this.desgastePorVuelta = desgastePorVuelta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Multiplicador base del tiempo por vuelta según el compuesto (menor = más rápido). */
    public double getFactorRitmo() {
        return factorRitmo;
    }

    /** Si es un compuesto pensado para pista mojada (intermedio o lluvia). */
    public boolean isParaLluvia() {
        return paraLluvia;
    }

    /** Puntos de desgaste (sobre 100) que suma cada vuelta en condiciones adecuadas para el compuesto. */
    public double getDesgastePorVuelta() {
        return desgastePorVuelta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
