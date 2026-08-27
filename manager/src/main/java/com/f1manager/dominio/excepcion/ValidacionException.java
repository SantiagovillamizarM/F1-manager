//Define un tipo de "error controlado" que se usa para avisar cuando el usuario 
//ingresó datos inválidos, sin que el programa se cierre de golpe.
package com.f1manager.dominio.excepcion;
public class ValidacionException extends Exception {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
