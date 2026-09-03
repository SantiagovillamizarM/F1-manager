//Guarda la información de un piloto
//(nombre, equipo, rol, experiencia, habilidades específicas por curva/adelantamiento/recta/clima).

//Esta es la ruta que usa este .java
package com.f1manager.dominio.modelo;

//trae una clase de herramientas de Java que sirve para hacer verificaciones de forma segura con objetos (especialmente para evitar que el programa falle por valores nulos).
import java.util.Objects;

//Una clase publica llamada Piloto
public class Piloto {

    //atributo final del tipo int(Acepta numeros y puede hacer calculos matematicos con ellos) llamado id
    private final int id;
    //Una variable privada del tipo String llamada nombre
    private String nombre;
    //Una variable privada del tipo String llamada equipo
    private String equipo;
    //Una variable privada del tipo RolPiloto (el enum de Lider/Escudero) llamada rol
    private RolPiloto rol;
    //Una variable privada del tipo int llamada experienciaAnios, cuantos años de experiencia tiene el piloto
    private int experienciaAnios;
    //Una variable privada del tipo int llamada habilidadCurva, va de 1 a 100 y mide que tan bueno es en curvas
    private int habilidadCurva; // 1 a 100
    //Una variable privada del tipo int llamada habilidadAdelantamiento, va de 1 a 100 y mide que tan bueno es adelantando
    private int habilidadAdelantamiento; // 1 a 100
    //Una variable privada del tipo int llamada habilidadRecta, va de 1 a 100 y mide que tan bueno es en las rectas
    private int habilidadRecta; // 1 a 100
    //Una variable privada del tipo int llamada habilidadLluvia, va de 1 a 100 y mide que tan bueno es con clima lluvioso
    private int habilidadLluvia; // 1 a 100
    //Una variable privada del tipo int llamada habilidadSeco, va de 1 a 100 y mide que tan bueno es con clima seco
    private int habilidadSeco; // 1 a 100
    //Una variable privada del tipo int llamada habilidadExtremo, va de 1 a 100 y mide que tan bueno es con clima extremo
    private int habilidadExtremo; // 1 a 100
    //Una variable privada del tipo String llamada imagenUrl, guarda la ruta lista para cargar la foto del piloto (o el avatar por defecto); si es null es porque no tiene foto propia
    private String imagenUrl; // URL lista para cargar en un Image (foto subida o avatar predeterminado); null = sin foto

    //Constructor
    //Inicializa y asigna los datos iniciales a un objeto cuando es creado.
    public Piloto(int id, String nombre, String equipo, RolPiloto rol, int experienciaAnios,
                  int habilidadCurva, int habilidadAdelantamiento, int habilidadRecta,
                  int habilidadLluvia, int habilidadSeco, int habilidadExtremo) {
        this.id = id;
        this.nombre = nombre;
        this.equipo = equipo;
        this.rol = rol;
        this.experienciaAnios = experienciaAnios;
        this.habilidadCurva = habilidadCurva;
        this.habilidadAdelantamiento = habilidadAdelantamiento;
        this.habilidadRecta = habilidadRecta;
        this.habilidadLluvia = habilidadLluvia;
        this.habilidadSeco = habilidadSeco;
        this.habilidadExtremo = habilidadExtremo;
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
    public String getEquipo() {
        return equipo;
    }

    //Setter
    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    //Getter
    public RolPiloto getRol() {
        return rol;
    }

    //Setter
    public void setRol(RolPiloto rol) {
        this.rol = rol;
    }

    //Getter
    public int getExperienciaAnios() {
        return experienciaAnios;
    }

    //Setter
    public void setExperienciaAnios(int experienciaAnios) {
        this.experienciaAnios = experienciaAnios;
    }

    //Getter
    public int getHabilidadCurva() {
        return habilidadCurva;
    }

    //Setter
    public void setHabilidadCurva(int habilidadCurva) {
        this.habilidadCurva = habilidadCurva;
    }

    //Getter
    public int getHabilidadAdelantamiento() {
        return habilidadAdelantamiento;
    }

    //Setter
    public void setHabilidadAdelantamiento(int habilidadAdelantamiento) {
        this.habilidadAdelantamiento = habilidadAdelantamiento;
    }

    //Getter
    public int getHabilidadRecta() {
        return habilidadRecta;
    }

    //Setter
    public void setHabilidadRecta(int habilidadRecta) {
        this.habilidadRecta = habilidadRecta;
    }

    //Getter
    public int getHabilidadLluvia() {
        return habilidadLluvia;
    }

    //Setter
    public void setHabilidadLluvia(int habilidadLluvia) {
        this.habilidadLluvia = habilidadLluvia;
    }

    //Getter
    public int getHabilidadSeco() {
        return habilidadSeco;
    }

    //Setter
    public void setHabilidadSeco(int habilidadSeco) {
        this.habilidadSeco = habilidadSeco;
    }

    //Getter
    public int getHabilidadExtremo() {
        return habilidadExtremo;
    }

    //Setter
    public void setHabilidadExtremo(int habilidadExtremo) {
        this.habilidadExtremo = habilidadExtremo;
    }

    //Getter
    public String getImagenUrl() {
        return imagenUrl;
    }

    //Setter
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    //Este metodo del tipo double calcula y devuelve el promedio de las 6 habilidades especificas del piloto
    //(suma las 6 y divide entre 6.0), se usa nada mas como un resumen general y no se guarda en ningun lado
    public double getHabilidadPromedio() {
        return (habilidadCurva + habilidadAdelantamiento + habilidadRecta
                + habilidadLluvia + habilidadSeco + habilidadExtremo) / 6.0;
    }

    //Evalúa si este piloto es idéntico a otro comparando su tipo y su ID único.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Si apuntan a la misma memoria, son iguales
        if (!(o instanceof Piloto)) return false; // Si no es un Piloto, no son iguales
        Piloto piloto = (Piloto) o;
        return id == piloto.id; // Son iguales si comparten el mismo id
    }

    //Genera un código numérico único basado en el ID para almacenar el piloto en colecciones optimizadas.
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    //Devuelve el nombre del piloto junto con su equipo como representación en texto.
    @Override
    public String toString() {
        return nombre + " - " + equipo;
    }
}
