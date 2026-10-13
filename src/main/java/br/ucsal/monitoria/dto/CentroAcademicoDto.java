package br.ucsal.monitoria.dto;

import br.ucsal.monitoria.entity.CentroAcademico;
import br.ucsal.monitoria.entity.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class CentroAcademicoDto {

    private CentroAcademicoDto() {
    }

    public record Request(
            @NotBlank @Size(max = 20) String sigla,
            @NotBlank @Size(max = 200) String nome,
            @NotBlank @Email String email,
            @NotBlank @Size(max = 30) String telefone,
            @NotBlank(message = "informe o coordenador") String coordenador,
            @NotNull(message = "informe a IES") Long iesId) {
    }

    public record Response(Long id, String sigla, String nome, String email, String telefone, String coordenador,
                           Long iesId, String iesSigla, LocalDateTime dataCadastro, Status status) {

        public static Response from(CentroAcademico c) {
            return new Response(c.getId(), c.getSigla(), c.getNome(), c.getEmail(), c.getTelefone(), c.getCoordenador(),
                    c.getIes().getId(), c.getIes().getSigla(), c.getDataCadastro(), c.getStatus());
        }
    }
}
