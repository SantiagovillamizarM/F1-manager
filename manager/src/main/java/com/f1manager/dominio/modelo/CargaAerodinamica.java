//Define las tres opciones de carga aerodinámica 
//(Baja, Media, Alta) y cuánto afecta cada una al tiempo de vuelta.
package com.f1manager.dominio.modelo;
public enum CargaAerodinamica {
    BAJA("Baja", 1.010, 0.995),
    MEDIA("Media", 1.000, 1.000),
    ALTA("Alta", 0.990, 1.012);

    private final String etiqueta;
    private final double factorVelocidadPunta;
    private final double factorAgarre;

    CargaAerodinamica(String etiqueta, double factorVelocidadPunta, double factorAgarre) {
        this.etiqueta = etiqueta;
        this.factorVelocidadPunta = factorVelocidadPunta;
        this.factorAgarre = factorAgarre;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Multiplicador aplicado al tiempo por vuelta relacionado con velocidad punta (curvas rápidas). */
    public double getFactorVelocidadPunta() {
        return factorVelocidadPunta;
    }

    /** Multiplicador aplicado al tiempo por vuelta relacionado con agarre en curvas técnicas. */
    public double getFactorAgarre() {
        return factorAgarre;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
