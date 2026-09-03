//Este es un "puerto" (en arquitectura hexagonal): una interfaz que define QUÉ se puede hacer con los
//vehículos guardados (listar, insertar, actualizar), pero sin decir CÓMO se hace ni dónde se guardan
//de verdad. La capa de aplicación (DataStore) solo conoce este contrato; quien lo cumple de verdad es
//un adaptador (por ejemplo VehiculoRepositorioMySQL, que guarda todo en MySQL). Así, si el día de
//mañana se quisiera cambiar de base de datos, solo habría que escribir OTRO adaptador que implemente
//esta misma interfaz, sin tocar nada de la capa de aplicación ni de la UI.

//Esta es la ruta que usa este .java
package com.f1manager.dominio.repositorio;

//Trae la clase Monoplaza del dominio, porque este puerto trabaja con objetos Monoplaza
import com.f1manager.dominio.modelo.Monoplaza;
//Importa la interfaz List, que define el comportamiento general de una lista en Java (sirve como plantilla para clases como ArrayList)
import java.util.List;

//Una interfaz pública llamada "VehiculoRepositorio": cualquier clase que "implements" esto queda
//obligada a tener estos 3 métodos con esta misma firma (nombre, parámetros y tipo de retorno)
public interface VehiculoRepositorio {

    //Debe devolver la lista completa de vehículos guardados (de donde sea que los guarde el adaptador)
    List<Monoplaza> listarTodos();

    //Debe guardar un vehículo nuevo con estos datos (la configuración de carrera arranca en los
    //valores por defecto) y devolver el id que le haya asignado el almacenamiento
    int insertarYObtenerId(String modelo, String equipo, String motor, double velocidadMaxKmh, double aceleracion0a100);

    //Debe guardar la configuración actual (carga, modo, neumático, presión) del vehículo recibido
    //(ya lo busca por su id, que el vehículo ya trae)
    void actualizar(Monoplaza vehiculo);
}
