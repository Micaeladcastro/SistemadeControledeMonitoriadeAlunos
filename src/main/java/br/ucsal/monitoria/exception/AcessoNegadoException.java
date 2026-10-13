package br.ucsal.monitoria.exception;

/** O usuario esta logado, mas o registro nao pertence a ele. Retorna HTTP 403. */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
