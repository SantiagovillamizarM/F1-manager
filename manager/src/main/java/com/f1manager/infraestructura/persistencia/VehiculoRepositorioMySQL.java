//Lee y escribe monoplazas en la tabla MySQL "vehiculos".
package com.f1manager.infraestructura.persistencia;

import com.f1manager.dominio.modelo.CargaAerodinamica;
import com.f1manager.dominio.modelo.ModoConduccion;
import com.f1manager.dominio.modelo.Monoplaza;
import com.f1manager.dominio.modelo.TipoNeumatico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

final class VehiculoRepositorioMySQL {

    private VehiculoRepositorioMySQL() {
    }

    static List<Monoplaza> listarTodos() {
        String sql = "SELECT id, modelo, equipo, motor, velocidad_max_kmh, aceleracion_0_100, "
                + "carga_aerodinamica, modo_conduccion, tipo_neumatico, presion_aire FROM vehiculos";
        List<Monoplaza> vehiculos = new ArrayList<>();
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Monoplaza vehiculo = new Monoplaza(rs.getInt("id"), rs.getString("modelo"), rs.getString("equipo"),
                        rs.getString("motor"), rs.getDouble("velocidad_max_kmh"), rs.getDouble("aceleracion_0_100"));
                vehiculo.setCargaAerodinamica(CargaAerodinamica.valueOf(rs.getString("carga_aerodinamica")));
                vehiculo.setModoConduccion(ModoConduccion.valueOf(rs.getString("modo_conduccion")));
                vehiculo.setTipoNeumatico(TipoNeumatico.valueOf(rs.getString("tipo_neumatico")));
                vehiculo.setPresionAire(rs.getDouble("presion_aire"));
                vehiculos.add(vehiculo);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudieron cargar los vehículos desde MySQL: " + ex.getMessage(), ex);
        }
        return vehiculos;
    }

    /** Persiste la configuración actual (carga, modo, neumático, presión) de un vehículo ya existente. */
    static void actualizar(Monoplaza vehiculo) {
        String sql = "UPDATE vehiculos SET carga_aerodinamica = ?, modo_conduccion = ?, "
                + "tipo_neumatico = ?, presion_aire = ? WHERE id = ?";
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, vehiculo.getCargaAerodinamica().name());
            stmt.setString(2, vehiculo.getModoConduccion().name());
            stmt.setString(3, vehiculo.getTipoNeumatico().name());
            stmt.setDouble(4, vehiculo.getPresionAire());
            stmt.setInt(5, vehiculo.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo actualizar el vehículo en MySQL: " + ex.getMessage(), ex);
        }
    }
}
