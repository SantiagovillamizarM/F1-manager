//Define las dos opciones posibles de rol de un piloto 
//(Líder o Escudero).

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;
public enum RolPiloto {
    LIDER("Líder"),
    ESCUDERO("Escudero");

    private final String etiqueta;

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
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
