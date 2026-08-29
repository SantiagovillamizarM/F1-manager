//Guarda la información de un piloto
//(nombre, equipo, rol, experiencia, habilidades específicas por curva/adelantamiento/recta/clima).
package com.f1manager.dominio.modelo;

import java.util.Objects;

public class Piloto {

    private final int id;
    private String nombre;
    private String equipo;
    private RolPiloto rol;
    private int experienciaAnios;
    private int habilidadCurva; // 1 a 100
    private int habilidadAdelantamiento; // 1 a 100
    private int habilidadRecta; // 1 a 100
    private int habilidadLluvia; // 1 a 100
    private int habilidadSeco; // 1 a 100
    private int habilidadExtremo; // 1 a 100
    private String imagenUrl; // URL lista para cargar en un Image (foto subida o avatar predeterminado); null = sin foto

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

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public RolPiloto getRol() {
        return rol;
    }

    public void setRol(RolPiloto rol) {
        this.rol = rol;
    }

    public int getExperienciaAnios() {
        return experienciaAnios;
    }

    public void setExperienciaAnios(int experienciaAnios) {
        this.experienciaAnios = experienciaAnios;
    }

    public int getHabilidadCurva() {
        return habilidadCurva;
    }

    public void setHabilidadCurva(int habilidadCurva) {
        this.habilidadCurva = habilidadCurva;
    }

    public int getHabilidadAdelantamiento() {
        return habilidadAdelantamiento;
    }

    public void setHabilidadAdelantamiento(int habilidadAdelantamiento) {
        this.habilidadAdelantamiento = habilidadAdelantamiento;
    }

    public int getHabilidadRecta() {
        return habilidadRecta;
    }

    public void setHabilidadRecta(int habilidadRecta) {
        this.habilidadRecta = habilidadRecta;
    }

    public int getHabilidadLluvia() {
        return habilidadLluvia;
    }

    public void setHabilidadLluvia(int habilidadLluvia) {
        this.habilidadLluvia = habilidadLluvia;
    }

    public int getHabilidadSeco() {
        return habilidadSeco;
    }

    public void setHabilidadSeco(int habilidadSeco) {
        this.habilidadSeco = habilidadSeco;
    }

    public int getHabilidadExtremo() {
        return habilidadExtremo;
    }

    public void setHabilidadExtremo(int habilidadExtremo) {
        this.habilidadExtremo = habilidadExtremo;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    /** Promedio de las 6 habilidades específicas, usado como resumen general (no se almacena). */
    public double getHabilidadPromedio() {
        return (habilidadCurva + habilidadAdelantamiento + habilidadRecta
                + habilidadLluvia + habilidadSeco + habilidadExtremo) / 6.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Piloto)) return false;
        Piloto piloto = (Piloto) o;
        return id == piloto.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre + " - " + equipo;
    }
}
