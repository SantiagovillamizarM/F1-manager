//Como lo dice su nombre, guarda la información de un circuito 
//(nombre, país, longitud, vueltas, descripción).

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;
//trae una clase de herramientas de Java que sirve para hacer verificaciones de forma segura con objetos (especialmente para evitar que el programa falle por valores nulos).
import java.util.Objects;
//Una clase publica llamada Circuito
public class Circuito {
    //atributo final del tipo int(Acepta numeros y puede hacer calculos matematicos con ellos) llamada id
    private final int id;
    //atributo final del tipo String (Acepta cualquier tipo de caracter) llamado nombre
    private String nombre;
    //atributo final del tipo String (Acepta cualquier tipo de caracter) llamado pais
    private String pais;
    //atributo final del tipo double(Tipo de dato con muchos caracteres para numeros) llamado longitudKm
    private double longitudKm;
    //atributo final del tipo int(Acepta numeros y puede hacer calculos matematicos con ellos) llamada vueltas
    private int vueltas;
    //atributo final del tipo String (Acepta cualquier tipo de caracter) llamado descripcion
    private String descripcion;

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
    public Circuito(int id, String nombre, String pais, double longitudKm, int vueltas, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.pais = pais;
        this.longitudKm = longitudKm;
        this.vueltas = vueltas;
        this.descripcion = descripcion;
    }

    //Getter
    public int getId() {
        return id;
    }

    //Getter
    public String getNombre() {
        return nombre;
    }

    //Setter
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    //Getter
    public String getPais() {
        return pais;
    }

    //Setter
    public void setPais(String pais) {
        this.pais = pais;
    }

    //Getter
    public double getLongitudKm() {
        return longitudKm;
    }

    //Setter
    public void setLongitudKm(double longitudKm) {
        this.longitudKm = longitudKm;
    }

    //Getter
    public int getVueltas() {
        return vueltas;
    }

    //Setter
    public void setVueltas(int vueltas) {
        this.vueltas = vueltas;
    }

    //Getter
    public String getDescripcion() {
        return descripcion;
    }

    //Setter
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    //Getter
    public double getDistanciaTotalKm() {
        return longitudKm * vueltas;
    }

// Evalúa si este circuito es idéntico a otro comparando su tipo y su ID único.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Si apuntan a la misma memoria, son iguales
        if (!(o instanceof Circuito)) return false; // Si no es un Circuito, no son iguales
        Circuito circuito = (Circuito) o;
        return id == circuito.id; // Son iguales si comparten el mismo id
    }

    // Genera un código numérico único basado en el ID para almacenar el circuito en colecciones optimizadas.
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Devuelve el nombre del circuito junto con su país entre paréntesis como representación en texto.
    @Override
    public String toString() {
        return nombre + " (" + pais + ")";
    }
}
