package br.ucsal.monitoria.dto;

import br.ucsal.monitoria.entity.Curso;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.entity.Turno;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

public final class CursoDto {

    private CursoDto() {
    }

    public record Request(
            @NotBlank @Size(max = 20) String sigla,
            @NotBlank @Size(max = 200) String descricao,
            @NotNull(message = "informe o centro academico") Long centroAcademicoId,
            @NotEmpty(message = "informe ao menos um turno") Set<Turno> turnos,
            @NotBlank(message = "informe o coordenador") String coordenador) {
    }

    public record Response(Long id, String sigla, String descricao, Long centroAcademicoId, String centroAcademicoSigla,
                           String iesSigla, Set<Turno> turnos, String coordenador, LocalDateTime dataCadastro,
                           Status status) {

        public static Response from(Curso c) {
            return new Response(c.getId(), c.getSigla(), c.getDescricao(), c.getCentroAcademico().getId(),
                    c.getCentroAcademico().getSigla(), c.getCentroAcademico().getIes().getSigla(), Set.copyOf(c.getTurnos()),
                    c.getCoordenador(), c.getDataCadastro(), c.getStatus());
        }
    }
}
