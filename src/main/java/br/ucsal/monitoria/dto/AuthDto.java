package br.ucsal.monitoria.dto;

import br.ucsal.monitoria.entity.Perfil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;

public final class AuthDto {

    private AuthDto() {
    }

    public record LoginRequest(@NotBlank String login, @NotBlank String senha) {
    }

    public record LoginResponse(String token, String tipo, Instant expiraEm, UsuarioAtual usuario) {
    }

    /** Dados do usuario logado. docenteId/discenteId vem preenchidos conforme o cadastro. */
    public record UsuarioAtual(Long id, String login, String nome, Set<Perfil> perfis, Long docenteId, Long discenteId) {
    }

    public record TrocarSenhaRequest(@NotBlank String senhaAtual,
                                     @NotBlank @Size(min = 6, message = "deve ter no minimo 6 caracteres") String novaSenha) {
    }
}
