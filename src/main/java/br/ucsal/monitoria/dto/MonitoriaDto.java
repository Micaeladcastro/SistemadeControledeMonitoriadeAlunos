package br.ucsal.monitoria.dto;

import br.ucsal.monitoria.entity.Monitoria;
import br.ucsal.monitoria.entity.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.TreeSet;

public final class MonitoriaDto {

    private MonitoriaDto() {
    }

    public enum Situacao { NAO_INICIADA, EM_CURSO, FINALIZADA, CANCELADA }

    /**
     * O docente e preenchido automaticamente com o usuario logado.
     * diasSemana: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY.
     * horaInicio/horaFim no formato "HH:mm" (maximo de 50 minutos).
     */
    public record Request(
            @NotNull(message = "informe o discente") Long discenteId,
            @NotNull(message = "informe a disciplina") Long disciplinaId,
            @NotEmpty(message = "informe ao menos um dia da semana") Set<DayOfWeek> diasSemana,
            @NotNull @JsonFormat(pattern = "HH:mm") LocalTime horaInicio,
            @NotNull @JsonFormat(pattern = "HH:mm") LocalTime horaFim,
            @NotNull LocalDate dataInicio,
            @NotNull LocalDate dataFim) {
    }

    public record Response(Long id, Long docenteId, String docenteNome, Long discenteId, String discenteNome,
                           String discenteMatricula, Long disciplinaId, String disciplinaSigla,
                           String disciplinaDescricao, String semestre, Set<DayOfWeek> diasSemana,
                           @JsonFormat(pattern = "HH:mm") LocalTime horaInicio,
                           @JsonFormat(pattern = "HH:mm") LocalTime horaFim,
                           LocalDate dataInicio, LocalDate dataFim, LocalDateTime dataCadastro, Status status,
                           Situacao situacao) {

        public static Response from(Monitoria m) {
            return new Response(m.getId(), m.getDocente().getId(), m.getDocente().getNome(), m.getDiscente().getId(),
                    m.getDiscente().getNome(), m.getDiscente().getMatricula(), m.getDisciplina().getId(),
                    m.getDisciplina().getSigla(), m.getDisciplina().getDescricao(), m.getSemestre(),
                    new TreeSet<>(m.getDiasSemana()), m.getHoraInicio(), m.getHoraFim(), m.getDataInicio(),
                    m.getDataFim(), m.getDataCadastro(), m.getStatus(), situacao(m));
        }

        public static Situacao situacao(Monitoria m) {
            if (m.getStatus() == Status.INATIVO) return Situacao.CANCELADA;
            if (m.isFinalizada()) return Situacao.FINALIZADA;
            if (m.getDataInicio().isAfter(LocalDate.now())) return Situacao.NAO_INICIADA;
            return Situacao.EM_CURSO;
        }
    }
}
