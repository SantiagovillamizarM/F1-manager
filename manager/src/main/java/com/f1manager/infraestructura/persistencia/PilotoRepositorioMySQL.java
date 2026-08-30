//Lee y escribe pilotos en la tabla MySQL "pilotos".
package com.f1manager.infraestructura.persistencia;

import com.f1manager.dominio.modelo.Piloto;
import com.f1manager.dominio.modelo.RolPiloto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

final class PilotoRepositorioMySQL {

    private PilotoRepositorioMySQL() {
    }

    static List<Piloto> listarTodos() {
        String sql = "SELECT id, nombre, equipo, rol, experiencia_anios, habilidad_curva, "
                + "habilidad_adelantamiento, habilidad_recta, habilidad_lluvia, habilidad_seco, "
                + "habilidad_extremo, imagen_url FROM pilotos";
        List<Piloto> pilotos = new ArrayList<>();
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Piloto piloto = new Piloto(rs.getInt("id"), rs.getString("nombre"), rs.getString("equipo"),
                        RolPiloto.valueOf(rs.getString("rol")), rs.getInt("experiencia_anios"),
                        rs.getInt("habilidad_curva"), rs.getInt("habilidad_adelantamiento"),
                        rs.getInt("habilidad_recta"), rs.getInt("habilidad_lluvia"),
                        rs.getInt("habilidad_seco"), rs.getInt("habilidad_extremo"));
                piloto.setImagenUrl(resolverImagen(rs.getString("imagen_url")));
                pilotos.add(piloto);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudieron cargar los pilotos desde MySQL: " + ex.getMessage(), ex);
        }
        return pilotos;
    }

    /**
     * Si lo guardado ya es una URL completa (foto subida por el usuario, o un avatar elegido al
     * registrarlo) se usa tal cual. Si es solo un nombre de archivo (foto real de un piloto
     * predeterminado, ej. "lewis.jpg"), se resuelve contra /imagenes/corredores predeterminados —
     * así la base de datos guarda un nombre portable, no una ruta absoluta de esta máquina.
     */
    private static String resolverImagen(String valorGuardado) {
        if (valorGuardado == null || valorGuardado.isBlank()) {
            return null;
        }
        if (valorGuardado.contains("://")) {
            return valorGuardado;
        }
        var recurso = PilotoRepositorioMySQL.class.getResource("/imagenes/corredores predeterminados/" + valorGuardado);
        return recurso != null ? recurso.toExternalForm() : null;
    }

    /** Inserta el piloto y devuelve el id que le asignó MySQL (autoincremental). */
    static int insertarYObtenerId(String nombre, String equipo, RolPiloto rol, int experiencia,
                                   int curva, int adelantamiento, int recta, int lluvia, int seco, int extremo,
                                   String imagenUrl) {
        String sql = "INSERT INTO pilotos (nombre, equipo, rol, experiencia_anios, habilidad_curva, "
                + "habilidad_adelantamiento, habilidad_recta, habilidad_lluvia, habilidad_seco, "
                + "habilidad_extremo, imagen_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.setString(2, equipo);
            stmt.setString(3, rol.name());
            stmt.setInt(4, experiencia);
            stmt.setInt(5, curva);
            stmt.setInt(6, adelantamiento);
            stmt.setInt(7, recta);
            stmt.setInt(8, lluvia);
            stmt.setInt(9, seco);
            stmt.setInt(10, extremo);
            stmt.setString(11, imagenUrl);
            stmt.executeUpdate();
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo guardar el piloto en MySQL: " + ex.getMessage(), ex);
        }
    }

    static void eliminar(int id) {
        String sql = "DELETE FROM pilotos WHERE id = ?";
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo eliminar el piloto en MySQL: " + ex.getMessage(), ex);
        }
    }
}
