package br.ucsal.monitoria.dto;

import br.ucsal.monitoria.entity.Docente;
import br.ucsal.monitoria.entity.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class DocenteDto {

    private DocenteDto() {
    }

    /**
     * Ao cadastrar, e criado um usuario com login = matricula e perfil COLABORADOR.
     * senhaInicial e opcional (padrao: a propria matricula) e e ignorada na edicao.
     */
    public record Request(
            @NotBlank @Size(max = 30) String matricula,
            @NotBlank @Size(max = 150) String nome,
            @NotBlank @Email String email,
            @NotBlank @Size(max = 30) String telefone,
            @NotNull(message = "informe o centro academico") Long centroAcademicoId,
            @Size(min = 6, message = "deve ter no minimo 6 caracteres") String senhaInicial) {
    }

    public record Response(Long id, String matricula, String nome, String email, String telefone,
                           Long centroAcademicoId, String centroAcademicoSigla, String login,
                           LocalDateTime dataCadastro, Status status) {

        public static Response from(Docente d) {
            return new Response(d.getId(), d.getMatricula(), d.getNome(), d.getEmail(), d.getTelefone(),
                    d.getCentroAcademico().getId(), d.getCentroAcademico().getSigla(), d.getUsuario().getLogin(),
                    d.getDataCadastro(), d.getStatus());
        }
    }
}
