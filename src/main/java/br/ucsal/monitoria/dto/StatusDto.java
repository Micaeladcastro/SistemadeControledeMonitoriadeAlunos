package br.ucsal.monitoria.dto;

import br.ucsal.monitoria.entity.HistoricoStatus;
import br.ucsal.monitoria.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class StatusDto {

    private StatusDto() {
    }

    /** Corpo das requisicoes PATCH .../inativar e .../ativar. */
    public record MotivoRequest(@NotBlank(message = "informe o motivo") @Size(max = 500) String motivo) {
    }

    public record HistoricoResponse(Long id, String entidade, Long entidadeId, Status statusAnterior,
                                    Status statusNovo, String motivo, String usuarioLogin, LocalDateTime dataHora) {

        public static HistoricoResponse from(HistoricoStatus h) {
            return new HistoricoResponse(h.getId(), h.getEntidade(), h.getEntidadeId(), h.getStatusAnterior(),
                    h.getStatusNovo(), h.getMotivo(), h.getUsuarioLogin(), h.getDataHora());
        }
    }
}
