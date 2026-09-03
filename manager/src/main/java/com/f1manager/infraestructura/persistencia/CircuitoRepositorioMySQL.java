//Se encarga de leer y escribir los circuitos directamente en la tabla MySQL "circuitos"
//(osea que hace de puente entre los objetos "Circuito" de Java y las filas de la base de datos).

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.persistencia;

//Trae la clase Circuito del dominio, para poder armar objetos Circuito con lo que se lee de la base de datos
import com.f1manager.dominio.modelo.Circuito;

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

//Una clase final (no se puede heredar) y sin "public" (osea que solo se puede usar dentro de este mismo paquete) llamada "CircuitoRepositorioMySQL"
final class CircuitoRepositorioMySQL {

    //Constructor privado y vacío: todos los métodos de aquí son static, entonces no hace falta crear objetos de esta clase
    private CircuitoRepositorioMySQL() {
    }

    //Este método trae TODOS los circuitos guardados en la tabla "circuitos" de MySQL y los devuelve como una lista de objetos Circuito
    static List<Circuito> listarTodos() {
        //El texto de la consulta SQL: le pide a MySQL las columnas de la tabla circuitos
        String sql = "SELECT id, nombre, pais, longitud_km, vueltas, descripcion FROM circuitos";
        //Lista vacía donde se van a ir guardando los circuitos que se lean de la base de datos
        List<Circuito> circuitos = new ArrayList<>();
        //try-with-resources: abre la conexión, prepara la consulta y ejecuta el SELECT, y al terminar (o si algo falla) cierra las tres cosas solo
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            //rs.next() mueve el cursor del ResultSet a la siguiente fila y devuelve false cuando ya no quedan filas, por eso funciona como condición del while
            while (rs.next()) {
                //Arma un objeto Circuito con los datos de la fila actual y lo agrega directo a la lista
                circuitos.add(new Circuito(rs.getInt("id"), rs.getString("nombre"), rs.getString("pais"),
                        rs.getDouble("longitud_km"), rs.getInt("vueltas"), rs.getString("descripcion")));
            }
        } catch (SQLException ex) {
            //Si algo sale mal, se convierte el error en uno más simple de entender y se relanza
            throw new RuntimeException("No se pudieron cargar los circuitos desde MySQL: " + ex.getMessage(), ex);
        }
        //Devuelve la lista ya llena con todos los circuitos leídos
        return circuitos;
    }

    //Inserta un circuito nuevo en MySQL y devuelve el id que MySQL le asignó automáticamente (autoincremental, osea que la base de datos lo pone solita, no lo elige Java)
    static int insertarYObtenerId(String nombre, String pais, double longitudKm, int vueltas, String descripcion) {
        //Consulta con 5 "?" que son los espacios reservados donde luego se ponen los valores reales de forma segura
        String sql = "INSERT INTO circuitos (nombre, pais, longitud_km, vueltas, descripcion) VALUES (?, ?, ?, ?, ?)";
        //Statement.RETURN_GENERATED_KEYS le avisa a MySQL que después vamos a pedirle el id que generó para esta fila nueva
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            //Rellena cada "?" de la consulta, en orden, con el dato correspondiente
            stmt.setString(1, nombre);
            stmt.setString(2, pais);
            stmt.setDouble(3, longitudKm);
            stmt.setInt(4, vueltas);
            stmt.setString(5, descripcion);
            //Ejecuta el INSERT de verdad contra la base de datos
            stmt.executeUpdate();
            //try-with-resources otra vez: abre un ResultSet chiquito solo con la(s) clave(s) generadas y lo cierra solo al terminar
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                //Mueve el cursor a la única fila que trae este ResultSet (el id nuevo)
                claves.next();
                //Devuelve ese id (la primera y única columna, por eso el índice 1)
                return claves.getInt(1);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo guardar el circuito en MySQL: " + ex.getMessage(), ex);
        }
    }

    //Guarda en MySQL los datos actuales (ya modificados en memoria) de un circuito que ya existía, buscándolo por su id
    static void actualizar(Circuito circuito) {
        //Consulta UPDATE: cambia todas las columnas menos el id, y usa el id solo para saber cuál fila tocar (WHERE id = ?)
        String sql = "UPDATE circuitos SET nombre = ?, pais = ?, longitud_km = ?, vueltas = ?, "
                + "descripcion = ? WHERE id = ?";
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            //Rellena cada "?" con el dato actual del objeto circuito que llega por parámetro
            stmt.setString(1, circuito.getNombre());
            stmt.setString(2, circuito.getPais());
            stmt.setDouble(3, circuito.getLongitudKm());
            stmt.setInt(4, circuito.getVueltas());
            stmt.setString(5, circuito.getDescripcion());
            //Este último "?" es el del WHERE, para que MySQL sepa exactamente qué fila actualizar
            stmt.setInt(6, circuito.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo actualizar el circuito en MySQL: " + ex.getMessage(), ex);
        }
    }

    //Borra (DELETE) de MySQL el circuito que tenga el id indicado
    static void eliminar(int id) {
        String sql = "DELETE FROM circuitos WHERE id = ?";
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo eliminar el circuito en MySQL: " + ex.getMessage(), ex);
        }
    }
}
