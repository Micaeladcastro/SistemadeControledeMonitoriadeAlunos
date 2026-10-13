package br.ucsal.monitoria.exception;

public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String recurso, Object id) {
        super(recurso + " nao encontrado(a): " + id);
    }
}
