package br.ucsal.monitoria.dto;

import br.ucsal.monitoria.entity.Disciplina;
import br.ucsal.monitoria.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class DisciplinaDto {

    private DisciplinaDto() {
    }

    /** O docente e preenchido automaticamente com o usuario logado. */
    public record Request(
            @NotBlank @Size(max = 20) String sigla,
            @NotBlank @Size(max = 200) String descricao,
            @NotNull(message = "informe o curso") Long cursoId,
            @NotNull @Positive Integer cargaHoraria) {
    }

    public record Response(Long id, String sigla, String descricao, Long cursoId, String cursoSigla,
                           Integer cargaHoraria, Long docenteId, String docenteNome,
                           LocalDateTime dataCadastro, Status status) {

        public static Response from(Disciplina d) {
            return new Response(d.getId(), d.getSigla(), d.getDescricao(), d.getCurso().getId(), d.getCurso().getSigla(),
                    d.getCargaHoraria(), d.getDocente().getId(), d.getDocente().getNome(), d.getDataCadastro(), d.getStatus());
        }
    }
}
