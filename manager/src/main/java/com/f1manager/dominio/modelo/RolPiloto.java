//Define las dos opciones posibles de rol de un piloto 
//(Líder o Escudero).
package com.f1manager.dominio.modelo;
public enum RolPiloto {
    LIDER("Líder"),
    ESCUDERO("Escudero");

    private final String etiqueta;

    RolPiloto(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
