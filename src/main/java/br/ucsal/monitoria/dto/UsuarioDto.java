package br.ucsal.monitoria.dto;

import br.ucsal.monitoria.entity.Perfil;
import br.ucsal.monitoria.entity.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

public final class UsuarioDto {

    private UsuarioDto() {
    }

    /** Criacao de usuario avulso (ex.: outro administrador). Docentes e discentes tem cadastro proprio. */
    public record Request(@NotBlank @Size(max = 60) String login,
                          @NotBlank @Size(max = 150) String nome,
                          @NotBlank @Size(min = 6, message = "deve ter no minimo 6 caracteres") String senha,
                          @NotEmpty Set<Perfil> perfis) {
    }

    public record PerfisRequest(@NotEmpty(message = "informe ao menos um perfil") Set<Perfil> perfis) {
    }

    public record Response(Long id, String login, String nome, boolean ativo, Set<Perfil> perfis,
                           LocalDateTime dataCadastro) {

        public static Response from(Usuario u) {
            return new Response(u.getId(), u.getLogin(), u.getNome(), u.isAtivo(), Set.copyOf(u.getPerfis()),
                    u.getDataCadastro());
        }
    }
}
