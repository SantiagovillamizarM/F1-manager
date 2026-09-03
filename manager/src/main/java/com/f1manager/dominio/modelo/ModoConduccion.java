//Define los tres modos de conducción
//(Normal, Agresivo, Ahorro) y cuánto afecta cada uno al ritmo, al riesgo y al desgaste de neumáticos.

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;
//Un enum publico llamado "ModoConduccion"
//Si algo es de tipo enum significa que solo tiene los valores que le demos de opcion, NO PUEDE HABER MAS VALORES   
public enum ModoConduccion {
    //Modo normal, no cambia nada: factorRitmo en 1.000 (ni mas rapido ni mas lento), factorVariabilidad en 1.00 (vueltas parejas)
    //y factorDesgasteNeumatico en 1.00 (desgaste normal de las llantas)
    NORMAL("Normal", 1.000, 1.00, 1.00),
    //Modo agresivo, el factorRitmo de 0.975 es menor a 1 entonces va mas rapido, pero el factorVariabilidad de 1.60 hace que
    //las vueltas sean mas inconsistentes (arriesgas mas) y el factorDesgasteNeumatico de 1.35 gasta las llantas mas rapido
    AGRESIVO("Agresivo", 0.975, 1.60, 1.35),
    //Modo ahorro, el factorRitmo de 1.020 es mayor a 1 entonces va mas lento, pero el factorVariabilidad de 0.65 hace que
    //las vueltas sean mas parejas/estables y el factorDesgasteNeumatico de 0.70 cuida mas las llantas
    AHORRO("Ahorro", 1.020, 0.65, 0.70);

    //Variable privada final inmodificable (final) del tipo String llamada "etiqueta"
    private final String etiqueta;
    //Variable privada final inmodificable (final) del tipo double llamada "factorRitmo"
    private final double factorRitmo;
    //Variable privada final inmodificable (final) del tipo double llamada "factorVariabilidad"
    private final double factorVariabilidad;
    //Variable privada final inmodificable (final) del tipo double llamada "factorDesgasteNeumatico"
    private final double factorDesgasteNeumatico;

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
    ModoConduccion(String etiqueta, double factorRitmo, double factorVariabilidad, double factorDesgasteNeumatico) {
        this.etiqueta = etiqueta;
        this.factorRitmo = factorRitmo;
        this.factorVariabilidad = factorVariabilidad;
        this.factorDesgasteNeumatico = factorDesgasteNeumatico;
    }

    //Getter
    public String getEtiqueta() {
        return etiqueta;
    }

    //Getter, este numero multiplica el tiempo de vuelta base: entre mas chico, mas rapido corre el piloto
    public double getFactorRitmo() {
        return factorRitmo;
    }

    //Getter, este numero multiplica que tan erratica/inconsistente es la vuelta (el "ruido" aleatorio de la simulacion)
    public double getFactorVariabilidad() {
        return factorVariabilidad;
    }

    //Getter, este numero multiplica que tan rapido se gastan las llantas: entre mas grande, mas rapido se gastan
    public double getFactorDesgasteNeumatico() {
        return factorDesgasteNeumatico;
    }

    //Un string publico que personaliza como se ve el texto (Osea las etiquetas de arriba)
    @Override
    public String toString() {
        return etiqueta;
    }
}
