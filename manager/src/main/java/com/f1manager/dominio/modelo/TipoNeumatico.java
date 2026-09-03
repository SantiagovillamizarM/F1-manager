//Define los compuestos de neumático usados en la F1
//(Blando, Medio, Duro, Intermedio, Lluvia), cuánto afecta cada uno al ritmo
//y a qué ritmo se desgasta por vuelta en condiciones normales.

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;

//un enum publico llamado "TipoNeumatico"
//Si algo es de tipo enum significa que solo tiene los valores que le demos de opcion, NO PUEDE HABER MAS VALORES
public enum TipoNeumatico {
    //Compuesto blando, factorRitmo 0.985 (menor a 1 osea mas rapido), no es para lluvia (false) y se desgasta 4.6 por vuelta (el que mas rapido se gasta)
    BLANDO("Blando", 0.985, false, 4.6),
    //Compuesto medio, factorRitmo 1.000 (ni mas rapido ni mas lento), no es para lluvia (false) y se desgasta 3.3 por vuelta
    MEDIO("Medio", 1.000, false, 3.3),
    //Compuesto duro, factorRitmo 1.015 (mayor a 1 osea mas lento), no es para lluvia (false) y se desgasta 2.0 por vuelta (el que menos se gasta)
    DURO("Duro", 1.015, false, 2.0),
    //Compuesto intermedio, factorRitmo 1.000, SI es para lluvia (true) y se desgasta 3.0 por vuelta
    INTERMEDIO("Intermedio", 1.000, true, 3.0),
    //Compuesto de lluvia, factorRitmo 1.005, SI es para lluvia (true) y se desgasta 2.6 por vuelta
    LLUVIA("Lluvia", 1.005, true, 2.6);

    //Variable privada final inmodificable (final) del tipo String llamada "etiqueta"
    private final String etiqueta;
    //Variable privada final inmodificable (final) del tipo double(Tipo de dato con muchos caracteres para numeros) llamada "factorRitmo"
    private final double factorRitmo;
    //Variable privada final inmodificable (final) del tipo boolean (osea que solo puede valer true o false) llamada "paraLluvia"
    private final boolean paraLluvia;
    //Variable privada final inmodificable (final) del tipo double llamada "desgastePorVuelta"
    private final double desgastePorVuelta;

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
    TipoNeumatico(String etiqueta, double factorRitmo, boolean paraLluvia, double desgastePorVuelta) {
        this.etiqueta = etiqueta;
        this.factorRitmo = factorRitmo;
        this.paraLluvia = paraLluvia;
        this.desgastePorVuelta = desgastePorVuelta;
    }

    //Getter
    public String getEtiqueta() {
        return etiqueta;
    }

    //Getter, este numero multiplica el tiempo base de la vuelta segun el compuesto: entre mas chico, mas rapido corre
    public double getFactorRitmo() {
        return factorRitmo;
    }

    //Getter, dice si este compuesto (intermedio o lluvia) esta pensado para cuando la pista esta mojada
    public boolean isParaLluvia() {
        return paraLluvia;
    }

    //Getter, son los puntos de desgaste (sobre 100) que se le suman a la llanta en cada vuelta si las condiciones son las adecuadas para el compuesto
    public double getDesgastePorVuelta() {
        return desgastePorVuelta;
    }

    //Un string publico que personaliza como se ve el texto (Osea las etiquetas de arriba)
    @Override
    public String toString() {
        return etiqueta;
    }
}
