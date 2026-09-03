//Guarda la información de un vehículo
//(modelo, equipo, motor, velocidad, aceleración, y su configuración actual).

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;

//trae una clase de herramientas de Java que sirve para hacer verificaciones de forma segura con objetos (especialmente para evitar que el programa falle por valores nulos).
import java.util.Objects;
//Una clase publica llamada Monoplaza
public class Monoplaza {

    // Rango seguro de presión de neumáticos (PSI): fuera de él no se puede configurar.
    // Dentro del rango, alejarse del valor óptimo (el centro) hace que su efecto en carrera
    // sea cada vez más impredecible: a veces ayuda, a veces perjudica.
    //Constante publica fija (static final) del tipo double llamada PRESION_MINIMA, es el limite de abajo permitido
    public static final double PRESION_MINIMA = 18.0;
    //Constante publica fija (static final) del tipo double llamada PRESION_MAXIMA, es el limite de arriba permitido
    public static final double PRESION_MAXIMA = 26.0;
    //Constante publica fija del tipo double llamada PRESION_OPTIMA, calcula el punto justo en la mitad entre la minima y la maxima
    public static final double PRESION_OPTIMA = (PRESION_MINIMA + PRESION_MAXIMA) / 2.0;

    // Rango realista para un monoplaza de F1 actual (reglamento técnico de la FIA): la potencia,
    // aerodinámica y neumáticos permitidos limitan tanto la velocidad punta como la aceleración
    // a valores conocidos; fuera de este rango no se deja registrar un vehículo.
    //Constante publica fija del tipo double llamada VELOCIDAD_MINIMA_KMH
    public static final double VELOCIDAD_MINIMA_KMH = 300.0;
    //Constante publica fija del tipo double llamada VELOCIDAD_MAXIMA_KMH
    public static final double VELOCIDAD_MAXIMA_KMH = 380.0;
    //Constante publica fija del tipo double llamada ACELERACION_MINIMA_S
    public static final double ACELERACION_MINIMA_S = 2.0;
    //Constante publica fija del tipo double llamada ACELERACION_MAXIMA_S
    public static final double ACELERACION_MAXIMA_S = 4.0;

    //atributo final del tipo int(Acepta numeros y puede hacer calculos matematicos con ellos) llamado id
    private final int id;
    //Una variable privada del tipo String llamada modelo
    private String modelo;
    //Una variable privada del tipo String llamada equipo
    private String equipo;
    //Una variable privada del tipo String llamada motor
    private String motor;
    //Una variable privada del tipo double llamada velocidadMaxKmh
    private double velocidadMaxKmh;
    //Una variable privada del tipo double llamada aceleracion0a100, entre mas chico el numero mejor (tarda menos en llegar a 100)
    private double aceleracion0a100; // segundos, menor es mejor
    //Una variable privada del tipo CargaAerodinamica (el enum de Baja/Media/Alta) llamada cargaAerodinamica
    private CargaAerodinamica cargaAerodinamica;
    //Una variable privada del tipo ModoConduccion (el enum de Normal/Agresivo/Ahorro) llamada modoConduccion
    private ModoConduccion modoConduccion;
    //Una variable privada del tipo TipoNeumatico (el enum de los compuestos de llanta) llamada tipoNeumatico
    private TipoNeumatico tipoNeumatico;
    //Una variable privada del tipo double llamada presionAire, guarda la presion de las llantas en PSI
    private double presionAire; // PSI

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado, y deja unos valores por defecto
    //(carga media, modo normal, neumatico medio y presion optima) para que el monoplaza arranque configurado de forma neutra.
    public Monoplaza(int id, String modelo, String equipo, String motor,
                      double velocidadMaxKmh, double aceleracion0a100) {
        this.id = id;
        this.modelo = modelo;
        this.equipo = equipo;
        this.motor = motor;
        this.velocidadMaxKmh = velocidadMaxKmh;
        this.aceleracion0a100 = aceleracion0a100;
        this.cargaAerodinamica = CargaAerodinamica.MEDIA;
        this.modoConduccion = ModoConduccion.NORMAL;
        this.tipoNeumatico = TipoNeumatico.MEDIO;
        this.presionAire = PRESION_OPTIMA;
    }

    //Getter
    public int getId() {
        return id;
    }

    //Getter
    public String getModelo() {
        return modelo;
    }

    //Setter
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    //Getter
    public String getEquipo() {
        return equipo;
    }

    //Setter
    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    //Getter
    public String getMotor() {
        return motor;
    }

    //Setter
    public void setMotor(String motor) {
        this.motor = motor;
    }

    //Getter
    public double getVelocidadMaxKmh() {
        return velocidadMaxKmh;
    }

    //Setter
    public void setVelocidadMaxKmh(double velocidadMaxKmh) {
        this.velocidadMaxKmh = velocidadMaxKmh;
    }

    //Getter
    public double getAceleracion0a100() {
        return aceleracion0a100;
    }

    //Setter
    public void setAceleracion0a100(double aceleracion0a100) {
        this.aceleracion0a100 = aceleracion0a100;
    }

    //Getter
    public CargaAerodinamica getCargaAerodinamica() {
        return cargaAerodinamica;
    }

    //Setter
    public void setCargaAerodinamica(CargaAerodinamica cargaAerodinamica) {
        this.cargaAerodinamica = cargaAerodinamica;
    }

    //Getter
    public ModoConduccion getModoConduccion() {
        return modoConduccion;
    }

    //Setter
    public void setModoConduccion(ModoConduccion modoConduccion) {
        this.modoConduccion = modoConduccion;
    }

    //Getter
    public TipoNeumatico getTipoNeumatico() {
        return tipoNeumatico;
    }

    //Setter
    public void setTipoNeumatico(TipoNeumatico tipoNeumatico) {
        this.tipoNeumatico = tipoNeumatico;
    }

    //Getter
    public double getPresionAire() {
        return presionAire;
    }

    //Setter
    public void setPresionAire(double presionAire) {
        this.presionAire = presionAire;
    }

    //Evalúa si este monoplaza es idéntico a otro comparando su tipo y su ID único.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Si apuntan a la misma memoria, son iguales
        if (!(o instanceof Monoplaza)) return false; // Si no es un Monoplaza, no son iguales
        Monoplaza that = (Monoplaza) o;
        return id == that.id; // Son iguales si comparten el mismo id
    }

    //Genera un código numérico único basado en el ID para almacenar el monoplaza en colecciones optimizadas.
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    //Devuelve el modelo del monoplaza junto con su equipo entre paréntesis como representación en texto.
    @Override
    public String toString() {
        return modelo + " (" + equipo + ")";
    }
}
