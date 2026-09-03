//Este es un "puerto" (en arquitectura hexagonal): una interfaz que define QUÉ se puede hacer con los
//equipos guardados (listar, insertar, eliminar), pero sin decir CÓMO se hace ni dónde se guardan de
//verdad. La capa de aplicación (DataStore) solo conoce este contrato; quien lo cumple de verdad es un
//adaptador (por ejemplo EquipoRepositorioMySQL, que guarda todo en MySQL). Así, si el día de mañana
//se quisiera cambiar de base de datos, solo habría que escribir OTRO adaptador que implemente esta
//misma interfaz, sin tocar nada de la capa de aplicación ni de la UI.

//Esta es la ruta que usa este .java
package com.f1manager.dominio.repositorio;

//Trae la clase Equipo del dominio, porque este puerto trabaja con objetos Equipo
import com.f1manager.dominio.modelo.Equipo;
//Importa la interfaz List, que define el comportamiento general de una lista en Java (sirve como plantilla para clases como ArrayList)
import java.util.List;

//Una interfaz pública llamada "EquipoRepositorio": cualquier clase que "implements" esto queda
//obligada a tener estos 3 métodos con esta misma firma (nombre, parámetros y tipo de retorno)
public interface EquipoRepositorio {

    //Debe devolver la lista completa de equipos guardados (de donde sea que los guarde el adaptador)
    List<Equipo> listarTodos();

    //Debe guardar este equipo nuevo (a diferencia de circuitos/pilotos/vehículos, el equipo no
    //necesita que le devuelvan un id: se identifica por su nombre, que ya es único)
    void insertar(Equipo equipo);

    //Debe borrar el equipo que tenga el nombre indicado
    void eliminar(String nombre);
}
