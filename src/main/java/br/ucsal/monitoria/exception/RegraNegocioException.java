package br.ucsal.monitoria.exception;

/** Violacao de uma regra de negocio (ex.: monitoria com mais de 50 minutos). Retorna HTTP 422. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
