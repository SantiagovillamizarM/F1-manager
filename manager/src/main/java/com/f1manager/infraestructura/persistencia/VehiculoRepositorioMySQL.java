//Este es un "adaptador" (en arquitectura hexagonal): la implementación real, con MySQL de por
//medio, del puerto VehiculoRepositorio. Se encarga de leer y escribir los monoplazas (los autos)
//directamente en la tabla MySQL "vehiculos" (osea que hace de puente entre los objetos "Monoplaza"
//de Java y las filas de la base de datos). La capa de aplicación (DataStore) nunca usa esta clase
//directamente: solo conoce la interfaz VehiculoRepositorio, y es el compositor (Main) quien decide
//entregarle justo esta implementación de MySQL.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.persistencia;

//Trae CargaAerodinamica, el enum con las opciones de carga (Baja, Media, Alta) del monoplaza
import com.f1manager.dominio.modelo.CargaAerodinamica;
//Trae el puerto (la interfaz) que esta clase promete cumplir, definido del lado del dominio
import com.f1manager.dominio.repositorio.VehiculoRepositorio;
//Trae ModoConduccion, el enum con los modos de manejo (Normal, Agresivo, Ahorro) del monoplaza
import com.f1manager.dominio.modelo.ModoConduccion;
//Trae la clase Monoplaza del dominio, para poder armar objetos Monoplaza con lo que se lee de la base de datos
import com.f1manager.dominio.modelo.Monoplaza;
//Trae TipoNeumatico, el enum con los tipos de neumático que puede llevar el monoplaza
import com.f1manager.dominio.modelo.TipoNeumatico;

//Trae la clase Connection, que representa la conexión abierta hacia MySQL
import java.sql.Connection;
//Trae PreparedStatement, la herramienta para preparar consultas SQL de forma segura (con los "?" en vez de meter el texto a mano, evitando inyección SQL)
import java.sql.PreparedStatement;
//Trae ResultSet, que es como una tabla con los resultados que devuelve una consulta SELECT, y que se recorre fila por fila
import java.sql.ResultSet;
//Trae SQLException, el error que lanza Java cuando algo sale mal hablando con la base de datos
import java.sql.SQLException;
//Trae Statement, que aquí se usa solo para pedirle a MySQL que nos devuelva el id autoincremental que le puso a la fila recién insertada
import java.sql.Statement;
//Importa la clase de ArrayList para poder usar listas (Son las que tienen <>)
import java.util.ArrayList;
//Importa la interfaz List, que define el comportamiento general de una lista en Java (sirve como plantilla para clases como ArrayList)
import java.util.List;

//Una clase publica y final (no se puede heredar) llamada "VehiculoRepositorioMySQL" que implementa
//el puerto VehiculoRepositorio (por eso tiene que tener, sí o sí, los 3 métodos que pide esa interfaz)
public final class VehiculoRepositorioMySQL implements VehiculoRepositorio {

    //Este método trae TODOS los vehículos guardados en la tabla "vehiculos" de MySQL y los devuelve como una lista de objetos Monoplaza
    //@Override avisa que este método viene de la interfaz VehiculoRepositorio (y que Java revise que la firma coincida)
    @Override
    public List<Monoplaza> listarTodos() {
        //El texto de la consulta SQL: le pide a MySQL todas las columnas de la tabla vehiculos
        String sql = "SELECT id, modelo, equipo, motor, velocidad_max_kmh, aceleracion_0_100, "
                + "carga_aerodinamica, modo_conduccion, tipo_neumatico, presion_aire FROM vehiculos";
        //Lista vacía donde se van a ir guardando los vehículos que se lean de la base de datos
        List<Monoplaza> vehiculos = new ArrayList<>();
        //try-with-resources: abre la conexión, prepara la consulta y ejecuta el SELECT, y al terminar (o si algo falla) cierra las tres cosas solo
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            //rs.next() mueve el cursor del ResultSet a la siguiente fila y devuelve false cuando ya no quedan filas, por eso funciona como condición del while
            while (rs.next()) {
                //Arma el Monoplaza con los datos base de la fila actual
                Monoplaza vehiculo = new Monoplaza(rs.getInt("id"), rs.getString("modelo"), rs.getString("equipo"),
                        rs.getString("motor"), rs.getDouble("velocidad_max_kmh"), rs.getDouble("aceleracion_0_100"));
                //valueOf() convierte el texto guardado en la base de datos (ej. "MEDIA") de vuelta al valor real del enum CargaAerodinamica
                vehiculo.setCargaAerodinamica(CargaAerodinamica.valueOf(rs.getString("carga_aerodinamica")));
                //Lo mismo pero para el modo de conducción
                vehiculo.setModoConduccion(ModoConduccion.valueOf(rs.getString("modo_conduccion")));
                //Lo mismo pero para el tipo de neumático
                vehiculo.setTipoNeumatico(TipoNeumatico.valueOf(rs.getString("tipo_neumatico")));
                vehiculo.setPresionAire(rs.getDouble("presion_aire"));
                //Agrega el vehículo recién armado a la lista de resultados
                vehiculos.add(vehiculo);
            }
        } catch (SQLException ex) {
            //Si algo sale mal, se convierte el error en uno más simple de entender y se relanza
            throw new RuntimeException("No se pudieron cargar los vehículos desde MySQL: " + ex.getMessage(), ex);
        }
        //Devuelve la lista ya llena con todos los vehículos leídos
        return vehiculos;
    }

    //Inserta un vehículo nuevo en MySQL, dejando su configuración (carga, modo, neumático, presión) en los valores
    //por defecto que trae Monoplaza, y devuelve el id que MySQL le asignó automáticamente (autoincremental).
    @Override
    public int insertarYObtenerId(String modelo, String equipo, String motor,
                                   double velocidadMaxKmh, double aceleracion0a100) {
        //Consulta con 9 "?" que son los espacios reservados donde luego se ponen los valores reales de forma segura
        String sql = "INSERT INTO vehiculos (modelo, equipo, motor, velocidad_max_kmh, aceleracion_0_100, "
                + "carga_aerodinamica, modo_conduccion, tipo_neumatico, presion_aire) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        //Statement.RETURN_GENERATED_KEYS le avisa a MySQL que después vamos a pedirle el id que generó para esta fila nueva
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            //Rellena los primeros 5 "?" con los datos que llegan por parámetro
            stmt.setString(1, modelo);
            stmt.setString(2, equipo);
            stmt.setString(3, motor);
            stmt.setDouble(4, velocidadMaxKmh);
            stmt.setDouble(5, aceleracion0a100);
            //Los últimos 4 "?" no vienen del usuario, se dejan fijos con la configuración por defecto (.name() convierte el valor del enum a texto para guardarlo)
            stmt.setString(6, CargaAerodinamica.MEDIA.name());
            stmt.setString(7, ModoConduccion.NORMAL.name());
            stmt.setString(8, TipoNeumatico.MEDIO.name());
            stmt.setDouble(9, Monoplaza.PRESION_OPTIMA);
            //Ejecuta el INSERT de verdad contra la base de datos
            stmt.executeUpdate();
            //try-with-resources otra vez: abre un ResultSet chiquito solo con la clave generada y lo cierra solo al terminar
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                //Mueve el cursor a la única fila que trae este ResultSet (el id nuevo)
                claves.next();
                //Devuelve ese id (la primera y única columna, por eso el índice 1)
                return claves.getInt(1);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo guardar el vehículo en MySQL: " + ex.getMessage(), ex);
        }
    }

    //Guarda en MySQL la configuración actual (carga, modo, neumático, presión) de un vehículo que ya existía, buscándolo por su id
    @Override
    public void actualizar(Monoplaza vehiculo) {
        //Consulta UPDATE: solo toca las columnas de configuración, no el modelo/equipo/motor, y usa el id para saber cuál fila actualizar
        String sql = "UPDATE vehiculos SET carga_aerodinamica = ?, modo_conduccion = ?, "
                + "tipo_neumatico = ?, presion_aire = ? WHERE id = ?";
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            //.name() convierte cada enum a su texto (ej. ALTA -> "ALTA") para poder guardarlo como String en MySQL
            stmt.setString(1, vehiculo.getCargaAerodinamica().name());
            stmt.setString(2, vehiculo.getModoConduccion().name());
            stmt.setString(3, vehiculo.getTipoNeumatico().name());
            stmt.setDouble(4, vehiculo.getPresionAire());
            //Este último "?" es el del WHERE, para que MySQL sepa exactamente qué fila actualizar
            stmt.setInt(5, vehiculo.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo actualizar el vehículo en MySQL: " + ex.getMessage(), ex);
        }
    }
}
