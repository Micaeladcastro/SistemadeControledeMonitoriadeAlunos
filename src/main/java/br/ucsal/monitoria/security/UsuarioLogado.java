package br.ucsal.monitoria.security;

import br.ucsal.monitoria.entity.Discente;
import br.ucsal.monitoria.entity.Docente;
import br.ucsal.monitoria.entity.Perfil;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.entity.Usuario;
import br.ucsal.monitoria.exception.AcessoNegadoException;
import br.ucsal.monitoria.repository.DiscenteRepository;
import br.ucsal.monitoria.repository.DocenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Atalhos para descobrir quem esta fazendo a requisicao. */
@Component
@RequiredArgsConstructor
public class UsuarioLogado {

    private final DocenteRepository docenteRepository;
    private final DiscenteRepository discenteRepository;

    public Usuario usuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            throw new AcessoNegadoException("Usuario nao autenticado");
        }
        return usuario;
    }

    /** Login do usuario atual, ou null se a requisicao for anonima. */
    public static String loginAtualOuNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof Usuario u ? u.getLogin() : null;
    }

    public boolean isAdmin() {
        return usuario().getPerfis().contains(Perfil.ADMINISTRADOR);
    }

    /** Docente vinculado ao usuario logado (perfil COLABORADOR). */
    public Docente docente() {
        Docente docente = docenteRepository.findByUsuarioLogin(usuario().getLogin())
                .orElseThrow(() -> new AcessoNegadoException("O usuario logado nao possui cadastro de docente"));
        if (docente.getStatus() != Status.ATIVO) {
            throw new AcessoNegadoException("Cadastro de docente inativo");
        }
        return docente;
    }

    /** Discente vinculado ao usuario logado (perfil USUARIO). */
    public Discente discente() {
        Discente discente = discenteRepository.findByUsuarioLogin(usuario().getLogin())
                .orElseThrow(() -> new AcessoNegadoException("O usuario logado nao possui cadastro de discente"));
        if (discente.getStatus() != Status.ATIVO) {
            throw new AcessoNegadoException("Cadastro de discente inativo");
        }
        return discente;
    }

    /** Docente vinculado ao usuario logado, se houver (sem lancar erro). */
    public Optional<Docente> docenteOpcional() {
        return docenteRepository.findByUsuarioLogin(usuario().getLogin());
    }

    /** Discente vinculado ao usuario logado, se houver (sem lancar erro). */
    public Optional<Discente> discenteOpcional() {
        return discenteRepository.findByUsuarioLogin(usuario().getLogin());
    }
}
