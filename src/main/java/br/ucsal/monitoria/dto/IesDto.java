package br.ucsal.monitoria.dto;

import br.ucsal.monitoria.entity.Ies;
import br.ucsal.monitoria.entity.ProReitoria;
import br.ucsal.monitoria.entity.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class IesDto {

    private IesDto() {
    }

    public record ProReitoriaDto(@NotBlank String nome, @NotBlank String nomeado, LocalDate dataNomeacao) {

        static ProReitoriaDto from(ProReitoria p) {
            return new ProReitoriaDto(p.getNome(), p.getNomeado(), p.getDataNomeacao());
        }
    }

    public record Request(
            @NotBlank @Size(max = 20) String sigla,
            @NotBlank @Size(max = 200) String nome,
            @NotBlank @Email String email,
            @NotBlank @Size(max = 30) String telefone,
            @NotBlank(message = "informe o reitor") String reitor,
            List<@Valid ProReitoriaDto> proReitorias) {
    }

    public record Response(Long id, String sigla, String nome, String email, String telefone, String reitor,
                           List<ProReitoriaDto> proReitorias, LocalDateTime dataCadastro, Status status) {

        public static Response from(Ies i) {
            return new Response(i.getId(), i.getSigla(), i.getNome(), i.getEmail(), i.getTelefone(), i.getReitor(),
                    i.getProReitorias().stream().map(ProReitoriaDto::from).toList(), i.getDataCadastro(), i.getStatus());
        }
    }
}
