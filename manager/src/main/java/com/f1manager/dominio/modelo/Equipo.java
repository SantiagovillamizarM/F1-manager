//Guarda la información de un equipo/escudería 
//(nombre, país, motor).

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;
//trae una clase de herramientas de Java que sirve para hacer verificaciones de forma segura con objetos (especialmente para evitar que el programa falle por valores nulos).
import java.util.Objects;
//Una clase publica llamada equipo
public class Equipo {
    //Una variable privada del tipo string llamada "nombre"
    private String nombre;
    //Una variable privada del tipo string llamada "pais"
    private String pais;
    //Una variable privada del tipo string llamada "motor"
    private String motor;
    //Una variable privada del tipo string llamada "imageUrl"
    private String imagenUrl; // URL lista para cargar en un Image (logo subido); null = sin imagen propia

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
    public Equipo(String nombre, String pais, String motor) {
        this.nombre = nombre;
        this.pais = pais;
        this.motor = motor;
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
    public String getMotor() {
        return motor;
    }
    //Setter
    public void setMotor(String motor) {
        this.motor = motor;
    }
    //Getter
    public String getImagenUrl() {
        return imagenUrl;
    }
    //Setter
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    // Override de equals(): Define que dos equipos son el mismo si tienen exactamente el mismo nombre (ignorando mayúsculas/minúsculas)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equipo)) return false;
        Equipo equipo = (Equipo) o;
        return nombre.equalsIgnoreCase(equipo.nombre);
    }
    // Override de hashCode(): Genera una ID numérica única basada en el nombre en minúsculas; es obligatorio al usar equals() para que
    // estructuras como HashSet o HashMap puedan identificar, comparar y buscar objetos Equipo de forma rápida y sin duplicados.
    @Override
    public int hashCode() {
        return Objects.hash(nombre.toLowerCase());
    }

    @Override
    public String toString() {
        return nombre;
    }
}
