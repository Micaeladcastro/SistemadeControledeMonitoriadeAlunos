package br.ucsal.monitoria.dto;

import br.ucsal.monitoria.entity.LogAuditoria;

import java.time.LocalDateTime;

public record LogAuditoriaDto(Long id, String usuarioLogin, String metodo, String uri, String acao,
                              Integer statusHttp, String ip, LocalDateTime dataHora) {

    public static LogAuditoriaDto from(LogAuditoria l) {
        return new LogAuditoriaDto(l.getId(), l.getUsuarioLogin(), l.getMetodo(), l.getUri(), l.getAcao(),
                l.getStatusHttp(), l.getIp(), l.getDataHora());
    }
}
