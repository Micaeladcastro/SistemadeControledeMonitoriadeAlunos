package br.ucsal.monitoria.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato padrao de erro devolvido ao front-end. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(LocalDateTime timestamp, int status, String erro, String mensagem, Map<String, String> campos) {

    public static ErroResponse of(int status, String erro, String mensagem) {
        return new ErroResponse(LocalDateTime.now(), status, erro, mensagem, null);
    }
}
