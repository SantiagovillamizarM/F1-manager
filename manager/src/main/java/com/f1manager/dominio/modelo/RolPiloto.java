//Define las dos opciones posibles de rol de un piloto
//(Líder o Escudero).

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;

//un enum publico llamado "RolPiloto"
//Si algo es de tipo enum significa que solo tiene los valores que le demos de opcion, NO PUEDE HABER MAS VALORES
public enum RolPiloto {
    //El rol de lider, es el piloto prioritario del equipo
    LIDER("Líder"),
    //El rol de escudero, es el piloto que apoya al lider
    ESCUDERO("Escudero");

    //Variable privada final inmodificable (final) del tipo String llamada "etiqueta"
    private final String etiqueta;

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
    RolPiloto(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    //Getter
    public String getEtiqueta() {
        return etiqueta;
    }

    //Un string publico que personaliza como se ve el texto (Osea las etiquetas de arriba)
    @Override
    public String toString() {
        return etiqueta;
    }
}
