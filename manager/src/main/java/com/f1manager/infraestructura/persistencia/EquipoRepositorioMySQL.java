//Este es un "adaptador" (en arquitectura hexagonal): la implementación real, con MySQL de por
//medio, del puerto EquipoRepositorio. Se encarga de leer y escribir los equipos directamente en la
//tabla MySQL "equipos" (osea que hace de puente entre los objetos "Equipo" de Java y las filas de la
//base de datos). La capa de aplicación (DataStore) nunca usa esta clase directamente: solo conoce la
//interfaz EquipoRepositorio, y es el compositor (Main) quien decide entregarle justo esta
//implementación de MySQL.

//Esta es la ruta que usa este .java
package com.f1manager.infraestructura.persistencia;

//Trae la clase Equipo del dominio, para poder armar objetos Equipo con lo que se lee de la base de datos
import com.f1manager.dominio.modelo.Equipo;
//Trae el puerto (la interfaz) que esta clase promete cumplir, definido del lado del dominio
import com.f1manager.dominio.repositorio.EquipoRepositorio;

//Trae la clase Connection, que representa la conexión abierta hacia MySQL
import java.sql.Connection;
//Trae PreparedStatement, la herramienta para preparar consultas SQL de forma segura (con los "?" en vez de meter el texto a mano, evitando inyección SQL)
import java.sql.PreparedStatement;
//Trae ResultSet, que es como una tabla con los resultados que devuelve una consulta SELECT, y que se recorre fila por fila
import java.sql.ResultSet;
//Trae SQLException, el error que lanza Java cuando algo sale mal hablando con la base de datos
import java.sql.SQLException;
//Importa la clase de ArrayList para poder usar listas (Son las que tienen <>)
import java.util.ArrayList;
//Importa la interfaz List, que define el comportamiento general de una lista en Java (sirve como plantilla para clases como ArrayList)
import java.util.List;

//Una clase publica y final (no se puede heredar) llamada "EquipoRepositorioMySQL" que implementa
//el puerto EquipoRepositorio (por eso tiene que tener, sí o sí, los 3 métodos que pide esa interfaz)
public final class EquipoRepositorioMySQL implements EquipoRepositorio {

    //Este método trae TODOS los equipos guardados en la tabla "equipos" de MySQL y los devuelve como una lista de objetos Equipo
    //@Override avisa que este método viene de la interfaz EquipoRepositorio (y que Java revise que la firma coincida)
    @Override
    public List<Equipo> listarTodos() {
        //El texto de la consulta SQL: le pide a MySQL las columnas nombre, pais, motor e imagen_url de la tabla equipos
        String sql = "SELECT nombre, pais, motor, imagen_url FROM equipos";
        //Lista vacía donde se van a ir guardando los equipos que se lean de la base de datos
        List<Equipo> equipos = new ArrayList<>();
        //try-with-resources: abre la conexión, prepara la consulta y ejecuta el SELECT, y al terminar (o si algo falla) cierra las tres cosas solo, sin que toque hacerlo a mano
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            //rs.next() mueve el cursor del ResultSet a la siguiente fila y devuelve false cuando ya no quedan filas, por eso funciona como condición del while
            while (rs.next()) {
                //Arma un objeto Equipo nuevo con los datos de la fila actual (rs.getString busca el valor de esa columna en esa fila)
                Equipo equipo = new Equipo(rs.getString("nombre"), rs.getString("pais"), rs.getString("motor"));
                //Le asigna aparte la imagen (puede venir nula si el equipo no tiene logo propio)
                equipo.setImagenUrl(rs.getString("imagen_url"));
                //Agrega el equipo recién armado a la lista de resultados
                equipos.add(equipo);
            }
        } catch (SQLException ex) {
            //Si algo sale mal (por ejemplo MySQL apagado), se convierte el error en uno más simple de entender y se relanza
            throw new RuntimeException("No se pudieron cargar los equipos desde MySQL: " + ex.getMessage(), ex);
        }
        //Devuelve la lista ya llena con todos los equipos leídos
        return equipos;
    }

    //Este método guarda (INSERT) un equipo nuevo dentro de la tabla "equipos" de MySQL
    @Override
    public void insertar(Equipo equipo) {
        //La consulta con 4 signos de interrogación "?" que son los espacios reservados donde luego se ponen los valores reales de forma segura
        String sql = "INSERT INTO equipos (nombre, pais, motor, imagen_url) VALUES (?, ?, ?, ?)";
        //Abre la conexión y prepara la consulta; try-with-resources se encarga de cerrarlas al terminar
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            //Rellena cada "?" de la consulta, en orden, con el dato correspondiente del equipo
            stmt.setString(1, equipo.getNombre());
            stmt.setString(2, equipo.getPais());
            stmt.setString(3, equipo.getMotor());
            stmt.setString(4, equipo.getImagenUrl());
            //executeUpdate() ejecuta el INSERT de verdad contra la base de datos (se usa en vez de executeQuery porque este no devuelve una tabla de resultados)
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo guardar el equipo en MySQL: " + ex.getMessage(), ex);
        }
    }

    //Este método borra (DELETE) de MySQL el equipo que tenga el nombre indicado
    @Override
    public void eliminar(String nombre) {
        //Consulta con un solo "?" que se rellena con el nombre a borrar
        String sql = "DELETE FROM equipos WHERE nombre = ?";
        try (Connection conexion = ConexionMySQL.obtener();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            //Ejecuta el DELETE contra la base de datos
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo eliminar el equipo en MySQL: " + ex.getMessage(), ex);
        }
    }
}
