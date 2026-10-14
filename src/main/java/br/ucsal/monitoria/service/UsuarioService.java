package br.ucsal.monitoria.service;

import br.ucsal.monitoria.dto.UsuarioDto;
import br.ucsal.monitoria.entity.Perfil;
import br.ucsal.monitoria.entity.Usuario;
import br.ucsal.monitoria.exception.NaoEncontradoException;
import br.ucsal.monitoria.exception.RegraNegocioException;
import br.ucsal.monitoria.repository.UsuarioRepository;
import br.ucsal.monitoria.security.UsuarioLogado;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioLogado usuarioLogado;

    /** Cria o login de um docente/discente, ou reaproveita o usuario existente acumulando o perfil. */
    @Transactional
    public Usuario criarOuAcumularPerfil(String login, String nome, String senha, Perfil perfil) {
        return repository.findByLogin(login)
                .map(existente -> {
                    existente.getPerfis().add(perfil);
                    return existente;
                })
                .orElseGet(() -> repository.save(new Usuario(login, passwordEncoder.encode(senha), nome, Set.of(perfil))));
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto.Response> listar() {
        return repository.findAll().stream().map(UsuarioDto.Response::from).toList();
    }

    @Transactional
    public UsuarioDto.Response criar(UsuarioDto.Request req) {
        if (repository.existsByLogin(req.login())) {
            throw new RegraNegocioException("Ja existe um usuario com o login " + req.login());
        }
        Usuario u = new Usuario(req.login().trim(), passwordEncoder.encode(req.senha()), req.nome().trim(), req.perfis());
        return UsuarioDto.Response.from(repository.save(u));
    }

    @Transactional
    public UsuarioDto.Response alterarPerfis(Long id, Set<Perfil> perfis) {
        Usuario u = repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Usuario", id));
        if (u.getLogin().equals(usuarioLogado.usuario().getLogin()) && !perfis.contains(Perfil.ADMINISTRADOR)) {
            throw new RegraNegocioException("Voce nao pode remover o seu proprio perfil de ADMINISTRADOR");
        }
        u.setPerfis(new HashSet<>(perfis));
        return UsuarioDto.Response.from(u);
    }

    @Transactional
    public UsuarioDto.Response alterarAtivo(Long id, boolean ativo) {
        Usuario u = repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Usuario", id));
        if (!ativo && u.getLogin().equals(usuarioLogado.usuario().getLogin())) {
            throw new RegraNegocioException("Voce nao pode inativar o seu proprio usuario");
        }
        u.setAtivo(ativo);
        return UsuarioDto.Response.from(u);
    }

    @Transactional
    public void trocarSenha(String senhaAtual, String novaSenha) {
        Usuario u = repository.findByLogin(usuarioLogado.usuario().getLogin()).orElseThrow();
        if (!passwordEncoder.matches(senhaAtual, u.getSenha())) {
            throw new RegraNegocioException("Senha atual incorreta");
        }
        u.setSenha(passwordEncoder.encode(novaSenha));
    }

    /**
     * Ao inativar um docente/discente, o login tambem e bloqueado, a menos que o usuario
     * acumule outro perfil (ex.: um docente que tambem e administrador).
     */
    public void sincronizarAtivo(Usuario u, Perfil perfilDoCadastro, boolean cadastroAtivo) {
        boolean temOutroPerfil = u.getPerfis().stream().anyMatch(p -> p != perfilDoCadastro);
        if (!temOutroPerfil) {
            u.setAtivo(cadastroAtivo);
        }
    }
}
