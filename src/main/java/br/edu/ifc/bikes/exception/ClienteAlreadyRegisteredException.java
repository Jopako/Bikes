package br.edu.ifc.bikes.exception;

public class ClienteAlreadyRegisteredException extends RuntimeException {
    public ClienteAlreadyRegisteredException(String message) {
        super(message);
    }
}
