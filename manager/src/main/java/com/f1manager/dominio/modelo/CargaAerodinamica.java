//Define las tres opciones de carga aerodinámica 
//(Baja, Media, Alta) y cuánto afecta cada una al tiempo de vuelta.

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;

//un enum publico llamado "CargaAerodinamica"
//Si algo es de tipo enum significa que solo tiene los valores que le demos de opcion, NO PUEDE HABER MAS VALORES
public enum CargaAerodinamica {
    //Esta es la CargaAerodinamica baja la cual afecta en la velocidad punta por 1.010 y en el agarre por 0.995
    BAJA("Baja", 1.010, 0.995),
    //Esta es la CargaAerodinamica media la cual afecta en la velocidad punta por 1.000 y en el agarre por 1.00
    MEDIA("Media", 1.000, 1.000),
    //Esta es la CargaAerodinamica baja la cual afecta en la velocidad punta por 0.990 y en el agarre por 1.012
    ALTA("Alta", 0.990, 1.012);

    //atributo final del tipo String (Acepta cualquier tipo de caracter) llamado etiqueta
    private final String etiqueta;
    //atributo final del tipo double(Tipo de dato con muchos caracteres para numeros) llamado factorVelocidadPunta
    private final double factorVelocidadPunta;
    //atributo final double llamado factorAgarre
    private final double factorAgarre;

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
    CargaAerodinamica(String etiqueta, double factorVelocidadPunta, double factorAgarre) {
        this.etiqueta = etiqueta;
        this.factorVelocidadPunta = factorVelocidadPunta;
        this.factorAgarre = factorAgarre;
    }

    //Getter
    public String getEtiqueta() {
        return etiqueta;
    }

    //Getter
    public double getFactorVelocidadPunta() {
        return factorVelocidadPunta;
    }

    //Getter
    public double getFactorAgarre() {
        return factorAgarre;
    }

    //Devuelve el texto de la etiqueta ("Baja", "Media" o "Alta") cuando el objeto se imprime o se convierte a texto.
    @Override
    public String toString() {
        return etiqueta;
    }
}
