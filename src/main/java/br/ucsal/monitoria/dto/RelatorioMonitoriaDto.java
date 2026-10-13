package br.ucsal.monitoria.dto;

import br.ucsal.monitoria.entity.RegistroFrequencia;
import br.ucsal.monitoria.entity.RelatorioMonitoria;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.entity.TipoMonitoria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class RelatorioMonitoriaDto {

    private RelatorioMonitoriaDto() {
    }

    public record FrequenciaDto(@NotNull LocalDate data, @NotBlank String alunoNome, String alunoMatricula,
                                boolean presente) {

        public RegistroFrequencia toEntity() {
            return new RegistroFrequencia(data, alunoNome, alunoMatricula, presente);
        }

        static FrequenciaDto from(RegistroFrequencia r) {
            return new FrequenciaDto(r.getData(), r.getAlunoNome(), r.getAlunoMatricula(), r.isPresente());
        }
    }

    /** Preenchido pelo discente (monitor). monitoriaId e ignorado na edicao. */
    public record Request(
            @NotNull(message = "informe a monitoria") Long monitoriaId,
            @NotNull(message = "informe o tipo: PRESENCIAL ou REMOTO") TipoMonitoria tipo,
            @NotBlank(message = "informe o local de atuacao") @Size(max = 200) String localAtuacao,
            String ocorrencias,
            List<@Valid FrequenciaDto> frequencia,
            String consideracoesFinais) {
    }

    public record Response(Long id, MonitoriaDto.Response monitoria, TipoMonitoria tipo, String localAtuacao,
                           String ocorrencias, List<FrequenciaDto> frequencia, String consideracoesFinais,
                           LocalDateTime dataCadastro, Status status) {

        public static Response from(RelatorioMonitoria r) {
            return new Response(r.getId(), MonitoriaDto.Response.from(r.getMonitoria()), r.getTipo(), r.getLocalAtuacao(),
                    r.getOcorrencias(), r.getFrequencia().stream().map(FrequenciaDto::from).toList(),
                    r.getConsideracoesFinais(), r.getDataCadastro(), r.getStatus());
        }
    }
}
